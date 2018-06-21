package com.lvmama.marketing.sqlmem

import java.beans.Transient
import java.io.{FileNotFoundException, IOException}
import java.util.concurrent._
import java.util.HashMap

import com.lvmama.marketing.sqlmem.constant.Constant
import com.lvmama.marketing.sqlmem.model.{ExecutingModel, SqlMemHeart, TablesSchema}
import com.lvmama.marketing.sqlmem.tasks.{OperatorModelTask, TagModelTask}
import com.lvmama.marketing.sqlmem.util.DfsUtil
import org.apache.hadoop.fs.PathIOException
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by huoqiang on 24/7/2017.
  */
class SqlMemQuery(@Transient heart : SqlMemHeart) extends Serializable{

  private val executableModels = new HashMap[String, ExecutingModel]
  private val threadPool : ExecutorService = Executors.newFixedThreadPool(heart.crmModelConcurrencyNum)
  private val futures = new ConcurrentHashMap[String, Future[_]]
  private var dataSource = new HashMap[String, DataFrame]
  private var schemas : TablesSchema = null

  /**
    * 入口
    */
  def start(master: String, appName : String): Unit = {
    //1、创建spark session
    var sparkSession : SparkSession = null
    var sc : SparkContext = null
    try{
      val sparkConf = new SparkConf().setMaster(master).setAppName(appName)
      sparkConf.set("es.nodes", heart.esNodes)
      sparkConf.set("es.port", heart.esPort)
      sparkConf.set("es.index.auto.create", heart.esIndexAutoCreate)
      sparkConf.set("es.nodes.wan.only", heart.esNodesWanOnly)
      sc = new SparkContext(sparkConf)
      sparkSession = SparkSession.builder().config(sparkConf).getOrCreate()
      this.schemas = new TablesSchema(heart)
      dataSource = this.schemas.cacheHbaseTable(sc, sparkSession)
    }catch{
      case ex : Exception => {
        ex.printStackTrace()
        println(ex)
        destroy(sparkSession, sc)
      }
    }

    //2、轮循监听任务池
    while(heart.isAlive()){
      try{
        scanModelTaskPool(sparkSession)
        Thread.sleep(heart.monitorHz)
        //释放线程资源
        notifyDoneFutures()
      }catch{
        case ex : Exception => {
          ex.printStackTrace()
        }
        case ex : RuntimeException => {
          ex.printStackTrace()
        }
        case ex : IllegalArgumentException => {
          ex.printStackTrace()
        }
        case ex : IndexOutOfBoundsException => {
          ex.printStackTrace()
        }
      }
    }

    //3、结束任务
    destroy(sparkSession, sc)
  }

  /**
    * 释放已执行完成的task
    */
  private def notifyDoneFutures() : Unit = {
    val iter = futures.keySet().iterator()
    while(iter.hasNext){
      val key = iter.next()
      val future = futures.get(key)
      if(null != future && future.isDone){
        val res = future.get(100L, TimeUnit.MICROSECONDS)
        println("Task finished , the result as follows: ")
        println(res)
        iter.remove()
        executableModels.remove(key)
      }
    }
  }


  /**
    * 扫描任务池
    * 任务池中的任务均是前台页面提交过来待执行的任务
    * @param sparkSession
    */
  private def scanModelTaskPool(sparkSession : SparkSession) : Unit = {
    val models = heart.fs.listStatus(heart.crmDataModelsPool)
    models.foreach(f = f => {
      val filePath = f.getPath
      var executableModel = executableModels.get(filePath.getName)
      if (null == executableModel) {
        var model: String = null
        try {
          model = DfsUtil.readLine(heart.fs, filePath)
        } catch {
          case ex: Exception => {
            println("read model of '" + filePath + "' found exception!")
            ex.printStackTrace()
          }
          case ex: FileNotFoundException => {
            println("read model of '" + filePath + "' found FileNotFoundException!")
            ex.printStackTrace()
          }
          case ex: IOException => {
            println("read model of '" + filePath + "' found IOException!")
            ex.printStackTrace()
          }
          case ex: PathIOException => {
            println("read model of '" + filePath + "' found PathIOException!")
            ex.printStackTrace()
          }
        }
        if (Constant.modelIsValid(model)) {
          executableModel = new ExecutingModel(model, filePath.toString, heart.failedRetryTimes, heart.failedRetryInterval)
          executableModels.put(filePath.getName, executableModel)
        }else if(null != model){
          println("The model '" + model + "' is not match the regexp '" + Constant.modelRegExp + "', will be moved to the model error history directory.")
          //不合法的模型，直接移除掉
          DfsUtil.mv(heart.fs, filePath, heart.crmDataModelErrorDir + filePath.getName, true)
        }else{
          println("The model file '" + filePath + "' is busy or empty, retry for a moment!")
        }
      }

      if (null != executableModel) {
        if(null == futures.get(filePath.getName)){
          var future : Future[_] = null
          if(executableModel.getModelType() == Constant.MODEL_OPERATOR){//运营模型
            println("Submit operator model task : '" + executableModel.getModelPath() + "'.")
            future = threadPool.submit(new OperatorModelTask(sparkSession, executableModel,
              heart))
          }else if(executableModel.getModelType() == Constant.MODEL_TAG){//标签模型
            if(heart.crmTagModelEnabled){
              println("Submit tag model task : '" + executableModel.getModelPath() + "'.")
              future = threadPool.submit(new TagModelTask(sparkSession, executableModel,
                heart, dataSource.get("user_portrait")))
            }else{
              println("Ignore tag model["+executableModel.getNo+"] from the config file. You can set crm.tag.model.enabled = true , then will do tag model.")
              DfsUtil.mv(heart.fs, executableModel.getModelPath(), heart.crmDataModelIgnoredDir)
              executableModels.remove(executableModel.getModelPath().getName)
            }
          }else if(executableModel.getModelType() == Constant.MODEL_TMP_SQL){
            println("Submit tmp sql model task : '" + executableModel.getModelPath() + "'.")
          }else{
            println("This is unknown model : '" + executableModel.getModelPath() + "'.")
          }

          if(null != future){
            futures.put(filePath.getName, future)
          }
        }
      }
    })
  }

  /**
    * 销毁
    */
  private def destroy(sparkSession: SparkSession, sc : SparkContext) : Unit = {
    println("Destroy the sparkSession and sparkContext.")
    if(null != sparkSession){
      println("SparkSession clear cache and stop.")
      sparkSession.sqlContext.clearCache()
      sparkSession.stop()
    }
    if(null != sc){
      sc.stop()
    }

    //移除守护进程文件
    println("Remove the daemon flag file and heart beat file.")
    DfsUtil.remove(heart.fs, heart.processHeart)
    //移除心跳文件
    DfsUtil.remove(heart.fs, heart.processHeartBeat)

    //退出
    println("System.exit(0).")
    System.exit(0)
  }

}