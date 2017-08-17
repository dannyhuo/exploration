package marketing.sqlmem.util

import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by huoqiang on 7/7/2017.
  */
object Rdd2DFUtil {

  def hbsRdd2DF(sparkSession: SparkSession,
                    hbaseRdd : RDD[(ImmutableBytesWritable, Result)] ,
                    schema : StructType,
                    family : String) : DataFrame = {
    val rows = hbaseRdd.map( r => {
      val buffer = new ArrayBuffer[Any]
      schema.foreach(s => {
        val v = r._2.getValue(Bytes.toBytes(family),Bytes.toBytes(s.name))
        if(null == v){
          buffer += null
        }else if(s.dataType.isInstanceOf[StringType]){
          buffer += Bytes.toString(v)
        }else if(s.dataType.isInstanceOf[IntegerType]){
          buffer += Bytes.toString(v)
        }else if(s.dataType.isInstanceOf[ShortType]){
          buffer += Bytes.toString(v)
        }else if(s.dataType.isInstanceOf[LongType]){
          buffer += Bytes.toString(v)
        }else if(s.dataType.isInstanceOf[ByteType]){
          buffer += Bytes.toString(v)
        }else if(s.dataType.isInstanceOf[FloatType]){
          buffer += Bytes.toString(v)
        }else if(s.dataType.isInstanceOf[DoubleType]){
          buffer += Bytes.toString(v)
        }else if(s.dataType.isInstanceOf[BooleanType]){
          buffer += Bytes.toString(v)
        }else if(s.dataType.isInstanceOf[DateType]){
          buffer += Bytes.toString(v)
        }else if(s.dataType.isInstanceOf[DecimalType]){
          buffer += Bytes.toString(v)
        }else if(s.dataType.isInstanceOf[NullType]){
          buffer += ""
        }
      })
      Row.fromSeq(buffer.asInstanceOf[Seq[Any]])
    })
    sparkSession.createDataFrame(rows, schema)
  }
}
