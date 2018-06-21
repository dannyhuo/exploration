package com.lvmama.marketing.sqlmem.model

import java.util.HashMap

import com.lvmama.marketing.sqlmem.constant.HbaseDataType
import com.lvmama.marketing.sqlmem.util.Rdd2DFUtil
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.types._
import org.apache.spark.storage.StorageLevel

import scala.xml.XML

/**
  * Created by huoqiang on 17/7/2017.
  */
class TablesSchema(heart : SqlMemHeart) {

  private val tableSchemas = new HashMap[String, (StructType, String)]

  /**
    * 获取schemas
    * @return
    */
  def getSchemas() : HashMap[String, (StructType, String)] = {
    tableSchemas
  }

  //入口
  initConfig()

  /**
    * 初始化表配置
    */
  private def initConfig() : Unit={
    val xmlFile = XML.loadFile(heart.hbaseTableConf)
    val tableNodes = xmlFile \ "table"
    tableNodes.foreach(tableNode => {
      val tabName = (tableNode \ "tableName").text.trim
      val cols = tableNode \ "col"
      var familyName : String = null
      val structFieldSeq = cols.map( colNode => {
        val name = (colNode \ "name").text.trim.toLowerCase()
        if(null == familyName){
          familyName = (colNode \ "family").text.trim.toLowerCase()
        }
        val dataType = (colNode \ "dataType").text.trim.toLowerCase()
        toStructField(name, dataType)
      })
      val value = (StructType(structFieldSeq), familyName)
      tableSchemas.put(tabName, value)
    })
  }

  /**
    * 构造StructField
    * @param colName
    * @param dataType
    * @return
    */
  private def toStructField(colName:String, dataType:String) : StructField = {
    var structField : StructField = null
    if(dataType == HbaseDataType.BOOL){
      structField = StructField(colName, BooleanType, nullable = true)
    } else if(dataType == HbaseDataType.DATE){
      structField = StructField(colName, DateType, nullable = true)
    } else if(dataType == HbaseDataType.DECIMAL){
      structField = StructField(colName, new DecimalType, nullable = true)
    } else if(dataType == HbaseDataType.DOUBLE){
      structField = StructField(colName, DoubleType, nullable = true)
    } else if(dataType == HbaseDataType.FLOAT){
      structField = StructField(colName, FloatType, nullable = true)
    } else if(dataType == HbaseDataType.INT){
      structField = StructField(colName, IntegerType, nullable = true)
    } else if(dataType == HbaseDataType.LONG){
      structField = StructField(colName, LongType, nullable = true)
    } else if(dataType == HbaseDataType.SHORT){
      structField = StructField(colName, ShortType, nullable = true)
    } else if(dataType == HbaseDataType.STRING){
      structField = StructField(colName, StringType, nullable = true)
    }else{
      structField = StructField(colName, StringType, nullable = true)
    }
    structField
  }

  /**
    * 缓存hbase的表
    * @param sc
    * @param sparkSession
    */
  def cacheHbaseTable(sc : SparkContext, sparkSession : SparkSession) :
  HashMap[String, DataFrame] = {
    //获取配置中所有的表
    val schemas = this.getSchemas()
    val iterator = schemas.keySet().iterator()
    val tables = new HashMap[String, DataFrame]
    println("The storage level is " + heart.storageLevel)
    while(iterator.hasNext){
      val table = iterator.next()
      val value = schemas.get(table)
      val hbaseOrderConf = HBaseConfiguration.create()
      hbaseOrderConf.set(TableInputFormat.INPUT_TABLE, table)
      val familyName = value._2
      val hbsRdd = sc.newAPIHadoopRDD(hbaseOrderConf,classOf[TableInputFormat],classOf[ImmutableBytesWritable],classOf[Result])
      //将rdd转换成为spark的DataFrame
      val df = Rdd2DFUtil.hbsRdd2DF(sparkSession, hbsRdd, value._1, familyName).repartition(heart.dfPartitionSize)
      //缓存表，方便后面查询更快
      df.persist(StorageLevel.fromString(heart.storageLevel))
      if(heart.initCacheData){
        val count = df.count()
        println("The table of " + table + " total count is " + count)
      }
      //创建表
      df.createOrReplaceTempView(table)
      println("create table view of " + table + " finished.")
      tables.put(table,df)
    }
    return tables
  }

}
