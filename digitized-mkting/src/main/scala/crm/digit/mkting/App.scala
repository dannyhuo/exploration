package crm.digit.mkting

import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.client.Result
import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkConf

import crm.digit.mkting.df.Rdd2DFUtil
import crm.digit.mkting.sql.SqlParser


/**
 * Hello world!
 *
 */
object App {


  def test(sql : String) : Unit = {

    //1、解析sql
    println("...........................................Start parse sql :" + sql)
    val schemas = new SqlParser(sql).getSparkSchemas
    println("............................................Parse sql finished, there is " + schemas.size() + " schema in it.")

    //2、创建spark sql相关对象
    val sparkConf = new SparkConf().setMaster("yarn").setAppName("Spark sql basic test")
    val sc = new SparkContext(sparkConf)
    val sparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

    //生成临时表
    val iter = schemas.keySet().iterator()
    while(iter.hasNext){
      val table = iter.next()
      println("....................................................do table of " + table)
      val hbaseOrderConf = HBaseConfiguration.create()
      hbaseOrderConf.set(TableInputFormat.INPUT_TABLE, table)
      val cols = schemas.get(table)._2
      val colsIter = cols.iterator()
      val sb = new StringBuilder()
      while(colsIter.hasNext){
        sb.append(colsIter.next())
        sb.append(" ")

      }
      //family:colname family:colname .....
      hbaseOrderConf.set(TableInputFormat.SCAN_COLUMNS, sb.toString())
      val userRdd = sc.newAPIHadoopRDD(hbaseOrderConf,classOf[TableInputFormat],classOf[ImmutableBytesWritable],classOf[Result])

      val df = Rdd2DFUtil.hbsRdd2DF(sparkSession, userRdd, schemas.get(table)._1, "info")
      println("............................................create or replace temp view of " + table)
      df.createOrReplaceTempView(table)
      println("the table " + table + "'s schema as follows :")
      df.printSchema()
    }
    println("will exec the sql = " + sql)
    val result = sparkSession.sql(sql)
    result.show(1000)
  }

  def main(args: Array[String]): Unit = {
    if(args.size < 1){
      throw new Exception("you don't point the parameter of sql text or json ")
    }
    println(".............................................sql is =" + args(0))
    test(args(0))
  }
}
