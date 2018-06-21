package crm.digit.mkting.mem.query

import java.util.Properties

import com.lvmama.crm.up.service.UpModelJobHessianServiceProxy
import crm.digit.mkting.esutil.ESWrapper
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Created by huoqiang on 24/7/2017.
  */
class OperatorModelTask (sparkSession: SparkSession, executingModel : ExecutingModel,
                         fs : FileSystem, upModelJobHessianServiceProxy : UpModelJobHessianServiceProxy,
                        conf : Properties)
  extends MemQueryTask(fs = fs, upModelJobHessianServiceProxy = upModelJobHessianServiceProxy, conf = conf){

  override def call(): Any = {
    println("operator model task [" + executingModel.getModel() + "] will start ......")
    super.doExecutingModel(executingModel, sparkSession)
    println("operator model task [" + executingModel.getModelId() + "] finished")
  }

  override def outputResult(df : DataFrame, executingModel: ExecutingModel): Unit = {
    //1、结果输出到hdfs
    val outputUri = crmDataModelsOutputPath.toString + "/result_" + executingModel.getExecNo + "_" + executingModel.getModelId()
    val outputPath = new Path(outputUri)
    if(fs.exists(outputPath)){
      fs.delete(outputPath, true)
    }

    if(dfWrite){
      df.repartition(1).write.text(outputUri)
    }else{
      val outDf = df.rdd.coalesce(1, shuffle = true)
      outDf.saveAsTextFile(outputUri)
    }

    df.show(100)

    //2、将结果输出到es
   /* val key = executingModel.getModelId() + "_" + executingModel.getExecNo

    /*println("start to writeToEs...................")
    writeToEs(df, key)*/

    //val key = executingModel.getModelId() + "_" + executingModel.getExecNo
    import org.elasticsearch.spark._
    println("start to saveToEs " + "http://10.200.2.67:9300/" + key)
    df.rdd.saveToEs(key+"/docs")*/
  }

  /**
    * 结果输出到es
    * @param df
    * @param key
    */
  def writeToEs(df : DataFrame, key : String) : Unit = {
    val esClient = ESWrapper.getClient();
    var bulkRequest = esClient.prepareBulk();
    df.foreach(r => {
      val value = r.getAs("user_id")
      var count = 0
      if(count < 10000){
        bulkRequest.add(
          esClient.prepareIndex(
            key.toString(),"data",
            value.toString()
          ).setSource("{\"u\":\""+value.toString()+"\"}"));
        count = count + 1;
      }else{
        count = 0;
        val fail = bulkRequest.get().hasFailures();
        bulkRequest = esClient.prepareBulk();
        bulkRequest.add(
          esClient.prepareIndex(
            key.toString(),"data",
            value.toString()
          ).setSource("{\"u\":\""+value.toString()+"\"}"));
        if(fail){
          throw new RuntimeException("ES bulk Request has failures!");
        }
      }
    })
  }

}
