package marketing.sqlmem

import java.beans.Transient
import java.io.{FileNotFoundException, IOException}
import java.text.SimpleDateFormat
import java.util.concurrent._
import java.util.{Date, HashMap}

import marketing.sqlmem.model.ExecutingModel
import marketing.sqlmem.tasks.{OperatorModelTask, TagModelTask}
import marketing.sqlmem.util.DfsUtil
import marketing.sqlmem.constant.Constant
import marketing.sqlmem.model.TablesSchema
import marketing.sqlmem.constant.Config
import org.apache.hadoop.fs.{Path, PathIOException}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by huoqiang on 24/7/2017.
  */
class SqlMemory(@Transient conf : Config) extends Serializable{

  private val executableModels = new HashMap[String, ExecutingModel]
  private val timeFormat = new SimpleDateFormat("HH:mm")
  private val threadPool : ExecutorService = Executors.newFixedThreadPool(conf.crmModelConcurrencyNum)
  private val futures = new ConcurrentHashMap[String, Future[_]]
  private var dataSource = new HashMap[String, DataFrame]
  private var schemas : TablesSchema = null

  /**
    * 入口
    */
  def start(master : String, appName : String): Unit = {
    //1、创建spark session
    var sparkSession : SparkSession = null
    var sc : SparkContext = null
    try{
      val sparkConf = new SparkConf().setMaster(master).setAppName(appName)
      sparkConf.set("es.nodes", "10.200.2.67")
      sparkConf.set("es.port", "9200")
      sparkConf.set("es.index.auto.create", "tree")
      sparkConf.set("es.nodes.wan.only", "true")
      sc = new SparkContext(sparkConf)
      sparkSession = SparkSession.builder().config(sparkConf).getOrCreate()
      this.schemas = new TablesSchema(conf.hbaseTableConf)
      dataSource = this.schemas.cacheHbaseTable(sc, sparkSession, conf.dfPartitionSize)
    }catch{
      case ex : Exception => {
        ex.printStackTrace()
        println(ex)
        destroy(sparkSession, sc)
      }
    }

    //2、轮循监听任务池
    var flag = 0
    while(conf.isAlive()){
      try{
        scanModelTaskPool(sparkSession)
        Thread.sleep(100)
        flag = flag + 1
        //释放线程资源
        notifyDoneFutures()
        //检查缓存
        if(flag == 500){
          checkCacheUpdate(sc, sparkSession)
          flag = 0
        }
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
    * 检查缓存
    */
  private def checkCacheUpdate(sc : SparkContext, sparkSession : SparkSession) : Unit = {
    val time = timeFormat.format(new Date(System.currentTimeMillis()))
    if(time == conf.updateCacheTime){
      println("update the cache by cacheHbaseTable(sc, sparkSession, conf.dfPartitionSize)")
      this.schemas.cacheHbaseTable(sc, sparkSession, conf.dfPartitionSize)
    }
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
    val models = conf.fs.listStatus(conf.crmDataModelsPool)
    models.foreach(f = f => {
      val filePath = f.getPath
      var execedModel = executableModels.get(filePath.getName)
      if (null == execedModel) {
        var model: String = null
        try {
          model = DfsUtil.readLine(conf.fs, filePath)
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
          execedModel = new ExecutingModel(model, filePath)
          executableModels.put(filePath.getName, execedModel)
        }else if(null != model){
          println("The model '" + model + "' is not match the regexp '" + Constant.modelRegExp + "', will be moved to the model error history directory.")
          //不合法的模型，直接移除掉
          val toPath = new Path(conf.crmDataModelErrorDir + filePath.getName)
          if(!conf.fs.exists(toPath)){
            conf.fs.rename(filePath, toPath)
          }else{
            conf.fs.delete(filePath, false)
          }
        }else{
          println("The model file '" + filePath + "' is busy or empty, retry for a moment!")
        }
      }

      if (null != execedModel) {
        if(null == futures.get(filePath.getName)){
          var future : Future[_] = null
          if(execedModel.getModelType() == Constant.MODEL_OPERATOR){
            println("Submit operator model task : '" + execedModel.getModelPath() + "'.")
            future = threadPool.submit(new OperatorModelTask(sparkSession, execedModel,
              conf.fs, conf))
          }else if(execedModel.getModelType() == Constant.MODEL_TAG){
            println("Submit tag model task : '" + execedModel.getModelPath() + "'.")
            future = threadPool.submit(new TagModelTask(sparkSession, execedModel,
              conf.fs, conf, dataSource.get("user_portrait")))
          }else if(execedModel.getModelType() == Constant.MODEL_TMP_SQL){
            println("Submit tmp sql model task : '" + execedModel.getModelPath() + "'.")
          }else{
            println("This is unknown model : '" + execedModel.getModelPath() + "'.")
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
    DfsUtil.remove(conf.fs, conf.processHeart)
    //移除心跳文件
    DfsUtil.remove(conf.fs, conf.processHeartBeat)

    //退出
    println("System.exit(0).")
    System.exit(0)
  }

}

