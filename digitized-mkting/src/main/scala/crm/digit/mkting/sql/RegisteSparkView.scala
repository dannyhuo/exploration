package crm.digit.mkting.sql

import java.util.{Map, Set}

import crm.digit.mkting.df.Rdd2DFUtil
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.StructType

/**
  * Created by huoqiang on 17/7/2017.
  */
class RegisteSparkView(sparkSession : SparkSession, sc : SparkContext) {


  def createOrReplaceView(schemas : Map[String, (StructType, Set[String])]) : Unit = {
    //生成临时表
    val iter = schemas.keySet().iterator()
    while(iter.hasNext){
      val table = iter.next()
      println("....................................................do table of " + table)
      val hbaseOrderConf = HBaseConfiguration.create()
      hbaseOrderConf.set(TableInputFormat.INPUT_TABLE, table)
      val sparkSchema = schemas.get(table)
      val cols = sparkSchema._2//构建hbase要查询的列
      val colsIter = cols.iterator()
      val sb = new StringBuilder()

      while(colsIter.hasNext){
        sb.append(colsIter.next())
        sb.append(" ")
      }
      //family:colname family:colname .....
      hbaseOrderConf.set(TableInputFormat.SCAN_COLUMNS, sb.toString())
      val familyName = sb.substring(0, sb.indexOf(":"))

      val userRdd = sc.newAPIHadoopRDD(hbaseOrderConf,classOf[TableInputFormat],classOf[ImmutableBytesWritable],classOf[Result])

      val df = Rdd2DFUtil.hbsRdd2DF(sparkSession, userRdd, sparkSchema._1, familyName)
      println("............................................create or replace temp view of " + table)
      df.createOrReplaceTempView(table)
      println("the table " + table + "'s schema as follows :")
      df.printSchema()
    }
  }

}
