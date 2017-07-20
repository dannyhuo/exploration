package crm.digit.mkting.sql

import java.util.{HashMap, Map}

import org.apache.spark.sql.types._

import scala.xml.XML

/**
  * Created by huoqiang on 17/7/2017.
  */
class TablesSchema(tableConfig : String) {

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
    val xmlFile = XML.loadFile(tableConfig)
    val tableNodes = xmlFile \ "table"
    tableNodes.foreach(tableNode => {
      val tabName = (tableNode \ "tableName").text
      val cols = tableNode \ "col"
      var familyName : String = null
      val structFieldSeq = cols.map( colNode => {
        val name = (colNode \ "name").text.toLowerCase()
        if(null == familyName){
          familyName = (colNode \ "family").text.toLowerCase()
        }
        val dataType = (colNode \ "dataType").text.toLowerCase()
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

}
