package com.lvmama.marketing.sqlmem.tasks

import java.beans.Transient

import com.lvmama.marketing.sqlmem.model.{ExecutingModel, SqlMemHeart}
import org.apache.hadoop.fs.{Path}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.elasticsearch.spark.sql.EsSparkSQL

/**
  * Created by huoqiang on 24/7/2017.
  */
class OperatorModelTask (sparkSession: SparkSession,
                         executingModel : ExecutingModel, @Transient heart : SqlMemHeart)
  extends AbstactSqlMemTask(heart = heart){

  override def call(): Any = {
    println("operator model task [" + executingModel.getModel() + "] will start ......")
    super.doExecutingModel(executingModel, sparkSession)
    println("operator model task [" + executingModel.getNo + "] finished")
  }

  override def outputResult(df : DataFrame, executingModel: ExecutingModel): Boolean = {
    //1、结果输出到hdfs
    val outputUri = heart.crmDataModelsOutputPath + "/result_" + executingModel.getExecNo + "_" + executingModel.getModelId()
    println("Start to write to hdfs, path is " + outputUri)
    val outputPath = new Path(outputUri)
    if(heart.fs.exists(outputPath)){
      heart.fs.delete(outputPath, true)
    }
    if(heart.dfWrite){
      df.coalesce(1).write.text(outputUri)
    }else{
      val outDf = df.rdd.coalesce(1, shuffle = true)
      outDf.saveAsTextFile(outputUri)
    }

    //2、将结果输出到es
    println("Successfully output to hdfs then will save to es, the index is " + executingModel.getNo)
    EsSparkSQL.saveToEs(df.withColumnRenamed("user_id", "u"), executingModel.getNo + "/data")
    println("Successfully save to es.")
    return true
  }

}
