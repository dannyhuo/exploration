package crm.digit.mkting

import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat

import crm.digit.mkting.df.{DfsUtil, Rdd2DFUtil}
import crm.digit.mkting.sql.TablesSchema
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.fs.PathIOException
import org.apache.spark.{SparkConf, SparkContext, SparkException}
import org.apache.spark.sql.{AnalysisException, DataFrame, SparkSession}
import java.util.HashMap
import java.util.Date

import com.caucho.hessian.client.HessianRuntimeException
import com.lvmama.crm.enumerate.UpModelJobEnum
import com.lvmama.crm.up.service.UpModelJobHessianServiceProxy
import crm.digit.mkting.mem.query.constant.Constant



/**
  * Created by huoqiang on 17/7/2017.
  */
object SqlMemQuery {

  /**
    * hbase表配置
    */
  private val tab_conf = "/home/hadoop/upload/conf/table_configs.xml"

  /**
    * 执行过的模型
    */
  private var execedModels = new HashMap[String, ExecutedModel]


  /**
    * dfs配置
    */
  private val dfsConf = new Configuration()
  dfsConf.addResource("/home/hadoop/app/hadoop/etc/hadoop/hdfs-site.xml")

  /**
    * fs
    */
  private val fs = FileSystem.get(dfsConf)

  //任务进程相关参数
  private val basePath = "/spark/app-spark-sql-memory-query"
  private val processHeart = new Path(basePath + "/heart.process")
  private val processHeartBeat = new Path(basePath + "/process-sql-mem-query-heart-beat")
  private val HEART_BEAT_INTERVAL_MS = 1000 * 60

  //任务池相关参数
  private val crmDataModelsPool = new Path("/tmp/crm/digitized-marketing/spark-sql-memory-query/crm-data-models-pool")
  private val crmDataModelsOutputPath = new Path("/tmp/crm/digitized-marketing/spark-sql-memory-query/crm-data-model-output")
  //private val crmDataModelsHistory = "/home/hadoop/crm/digitized-marketing/spark-sql-memory-query/crm-data-models-history"
  private val crmDataModelExecedFailedDir = "/tmp/crm/digitized-marketing/spark-sql-memory-query/crm-data-models-history/execed_failed/"
  private val crmDataModelExecedOkayDir = "/tmp/crm/digitized-marketing/spark-sql-memory-query/crm-data-models-history/execed_okay/"
  private val crmDataModelErrorDir = "/tmp/crm/digitized-marketing/spark-sql-memory-query/crm-data-models-history/model_error/"

  /**
    * 执行过的模型序列化的地方
    */
  private val execedModelSerializePath = "/tmp/crm/digitized-marketing/spark-sql-memory-query/crm-data-models-history/execed-models-serialized"

  //crm回调接口相关
  private val CRM_HESSIAN_URL = "http://10.112.3.98:8080/crm-server/hessian/upModelJobHessianService"
  private val upModelJobHessianServiceProxy = new UpModelJobHessianServiceProxy(CRM_HESSIAN_URL)

  private val dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

  private var dfWrite = false

  def prepare() : Unit = {
    val workDir = new Path(basePath)
    if(!fs.exists(workDir)){
      fs.mkdirs(workDir)
    }
    if(!fs.exists(crmDataModelsPool)){
      fs.mkdirs(crmDataModelsPool)
    }
    if(!fs.exists(crmDataModelsOutputPath)){
      fs.mkdirs(crmDataModelsOutputPath)
    }
    val crmDataModelExecedFailedPath = new Path(crmDataModelExecedFailedDir)
    if(!fs.exists(crmDataModelExecedFailedPath)){
      fs.mkdirs(crmDataModelExecedFailedPath)
    }
    val crmDataModelExecedOkayDirPath = new Path(crmDataModelExecedOkayDir)
    if(!fs.exists(crmDataModelExecedOkayDirPath)){
      fs.mkdirs(crmDataModelExecedOkayDirPath)
    }
    val crmDataModelErrorDirPath = new Path(crmDataModelErrorDir)
    if(!fs.exists(crmDataModelErrorDirPath)){
      fs.mkdirs(crmDataModelErrorDirPath)
    }
  }

  def main(args: Array[String]): Unit = {
    //1、判断是否已启动任务
    if(isRunning()){
      throw new Exception("this task is running, doesn't running repeat.")
    }
    prepare()
    if(args.size > 0){
      dfWrite = (args(0).trim == "true")
    }

    //2、创建守护进程文件，删除后将结束进程
    DfsUtil.touchEmpty(fs, processHeart)

    //3、启动心跳线程
    val t = new Thread(new HeartBeat(fs, processHeartBeat, HEART_BEAT_INTERVAL_MS))
    t.start()

    //4、创建spark session
    var sparkSession : SparkSession = null
    var sc : SparkContext = null
    try{
      val sparkConf = new SparkConf().setMaster("yarn").setAppName("Spark sql in memory")
      sc = new SparkContext(sparkConf)
      sparkSession = SparkSession.builder().config(sparkConf).getOrCreate()
      cacheHbaseTable(sc, sparkSession)
    }catch{
      case ex : Exception => {
        ex.printStackTrace()
        println(ex)
        destory(sparkSession, sc)
      }
    }

    //5、主线程循环监测提交过来的任务，并准备执行
    /*try{
      execedModels = DfsUtil.deserializeObject(fs, execedModelSerializePath)
    }catch{
      case ex : Exception => {
        ex.printStackTrace()
        execedModels = new HashMap[String, ExecutedModel]
      }
    }*/

    //6、轮循监听任务池
    while(fs.exists(processHeart)){
      Thread.sleep(100)
      try{
        scanModelTaskPool(sparkSession)
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

    //7、结束任务
    destory(sparkSession, sc)
  }

  /**
    * 判断是否已启动
    * @return
    */
  def isRunning() : Boolean = {
    if(!fs.exists(processHeart)){
      return false
    }
    if(fs.exists(processHeartBeat)){
      val line = DfsUtil.readLine(fs, processHeartBeat)
      if(null == line || line.trim == ""){
        return false
      }
      val lastBeatDate = dateFormat.parse(line.trim)
      val interval = new Date().getTime - lastBeatDate.getTime
      if(interval <= HEART_BEAT_INTERVAL_MS){
        return true
      }else{
        return false
      }
    }else{
      false
    }
  }

  /**
    * 扫描任务池
    * 任务池中的任务均是前台页面提交过来待执行的任务
    * @param sparkSession
    */
  def scanModelTaskPool(sparkSession : SparkSession) : Unit = {
    val models = fs.listStatus(crmDataModelsPool)
    models.foreach(f => {
      val filePath = f.getPath
      var execedModel = execedModels.get(filePath.getName)
      if(null == execedModel){
        var model : String = null
        try{
          model = DfsUtil.readLine(fs, filePath)
        }catch{
          case ex : Exception => {
            println("read model of "+filePath+" found exception!")
            ex.printStackTrace()
          }
          case ex : FileNotFoundException => {
            println("read model of "+filePath+" found FileNotFoundException!")
            ex.printStackTrace()
          }
          case ex : IOException => {
            println("read model of "+filePath+" found IOException!")
            ex.printStackTrace()
          }
          case ex : PathIOException => {
            println("read model of "+filePath+" found PathIOException!")
            ex.printStackTrace()
          }
        }
        if(null != model && model.trim != ""){
          execedModel = new ExecutedModel(model, filePath)
          execedModels.put(filePath.getName, execedModel)
        }
      }

      if(null != execedModel){
        val preTry = execedModel.retry()
        var errorMsg : String = null
        var resultId : Long = -1
        if(preTry == Constant.CAN_RETRY){
          //任务执行前，通知crm系统任务开始
          resultId = beforeExeTask(execedModel.getModelId().toLong)
          if(resultId > -1){
            try{
              val res = sparkSession.sqlContext.sql(execedModel.getSqlText())
              //输出结果
              outputResult(res, execedModel.getModelId(), execedModel.getExecNo)
              //执行成功后，回调crm通知
              afterExeTask(execedModel, Constant.EXEC_OKAY, resultId)
            }catch{
              case ex : AnalysisException => {
                //sql语法解析错误
                ex.printStackTrace()
                execedModel.addErrMsg(ex.getMessage())
                afterExeTask(execedModel, Constant.MODEL_ERROR, resultId)
              }
              case ex : SparkException => {
                //spark exception
                ex.printStackTrace()
                execedModel.addErrMsg(ex.getMessage())
              }
              case ex : NullPointerException => {
                ex.printStackTrace()
                execedModel.addErrMsg(ex.getMessage())
                afterExeTask(execedModel, Constant.MODEL_ERROR, resultId)
              }
              case ex : Exception => {
                errorMsg = ex.getMessage
                execedModel.addErrMsg(errorMsg)
                ex.printStackTrace()
              }
            }
          }else{
            errorMsg = "任务["+execedModel.getModelId()+"_"+execedModel.getExecNo+"]开始前，通知crm系统失败, 任务未启动!"
            println(errorMsg)
            execedModel.addErrMsg(errorMsg)
            //任务未开始，抵消一次重试次数
            execedModel.compensateRetriedTimes()
          }
        }else if(preTry == Constant.MODEL_ERROR){
          //模型异常
          errorMsg = "The model of '" + execedModel.getModel() + "' error!"
          println(errorMsg)
          execedModel.addErrMsg(errorMsg)
          afterExeTask(execedModel, preTry, resultId)
        }else if(preTry == Constant.MAX_TRIED){
          //重试达到最大次数
          errorMsg = "The model of '"+execedModel.getModel()+"' retry for '" + execedModel.getTriedTimes() + "' times till error!"
          println(errorMsg)
          execedModel.addErrMsg(errorMsg)
          afterExeTask(execedModel, preTry, resultId)
        }else{
          //任务未开始，抵消一次重试次数
          execedModel.compensateRetriedTimes()
        }
      }else{
        println("Read the model of '" + filePath + "' found exception!")
      }
    })
  }

  /**
    * 任务执行之前通知crm系统
    */
  def beforeExeTask(modelId : Long) : Long = {
    var result = -1L
    try{
      result = upModelJobHessianServiceProxy.doEvent(modelId, null,
        UpModelJobEnum.EXE_STATUS.running, UpModelJobEnum.EXE_EVENT.running, null)
    }catch{
      case ex : HessianRuntimeException => {
        println("Call crm hessian interface found error : " + ex.getMessage)
        ex.printStackTrace()
      }
    }
    return result
  }

  /**
    * 任务执行之后通知crm系统及清理相关模型
    */
  def afterExeTask(executedModel: ExecutedModel, event : Int, resultId : Long) : Unit = {
    //1、通知crm系统
    if(resultId > 0){//大于0，表示启动任务前告知crm系统成功
      var crmEvent = UpModelJobEnum.EXE_EVENT.emergencyStop
      if(event == Constant.EXEC_OKAY){
        crmEvent = UpModelJobEnum.EXE_EVENT.runSucessful
      }
      var res = -1L
      try{
        res = upModelJobHessianServiceProxy.doEvent(executedModel.getModelId().toLong, resultId,
          UpModelJobEnum.EXE_STATUS.normal, crmEvent, executedModel.getErrMsg())
      }catch{
        case ex : HessianRuntimeException => {
          println("Call crm hessian interface found error : " + ex.getMessage)
          ex.printStackTrace()
        }
      }
      if(res != 0){
        println("任务执行结束回调crm系统失败")
      }
    }

    //2、释放模型资源
    val modelPath = executedModel.getModelPath()
    var toPath : Path = null
    if(event == Constant.NOTIFY_CRM_FAILED){
      //暂不做处理，稍后重试
    }else if(event == Constant.MAX_TRIED){
      println("fs.rename(modelPath, new Path(crmDataModelExecedFailedDir + modelPath.getName)), modelPath " + modelPath + " => " + crmDataModelExecedFailedDir + modelPath.getName)
      toPath = new Path(crmDataModelExecedFailedDir + modelPath.getName)
      if(!fs.exists(toPath)){
        fs.rename(modelPath, toPath)
      }else{
        fs.delete(modelPath, false)
      }
      execedModels.remove(modelPath.getName)
    }else if(event == Constant.EXEC_OKAY){
      toPath = new Path(crmDataModelExecedOkayDir + modelPath.getName)
      if(!fs.exists(toPath)){
        fs.rename(modelPath, toPath)
      }else{
        fs.delete(modelPath, false)
      }
      execedModels.remove(modelPath.getName)
    }else if(event == Constant.MODEL_ERROR){
      toPath = new Path(crmDataModelErrorDir + modelPath.getName)
      if(!fs.exists(toPath)){
        fs.rename(modelPath, toPath)
      }else{
        fs.delete(modelPath, false)
      }
      execedModels.remove(modelPath.getName)
    }
  }


  /**
    * 缓存hbase的表
    * @param sc
    * @param sparkSession
    */
  def cacheHbaseTable(sc : SparkContext, sparkSession : SparkSession) : Unit = {
    //获取配置中所有的表
    val schemas = new TablesSchema(tab_conf).getSchemas()
    val iterator = schemas.keySet().iterator()
    while(iterator.hasNext){
      val table = iterator.next()
      val value = schemas.get(table)

      val hbaseOrderConf = HBaseConfiguration.create()
      hbaseOrderConf.set(TableInputFormat.INPUT_TABLE, table)
      val familyName = value._2
      val hbsRdd = sc.newAPIHadoopRDD(hbaseOrderConf,classOf[TableInputFormat],classOf[ImmutableBytesWritable],classOf[Result])
      //将rdd转换成为spark的DataFrame
      val df = Rdd2DFUtil.hbsRdd2DF(sparkSession, hbsRdd, value._1, familyName)
      //创建表
      df.createOrReplaceTempView(table)
      println("create table view of " + table + " finished.")
      //缓存表，方便后面查询更快
      df.cache()
      //df.count()
      //df.collect()
    }
  }

  /**
    * 输出结果
    * @param df
    */
  def outputResult(df : DataFrame, modelId : String, execNo: String) : Unit = {
    //df.show(1500)
    val outputUri = crmDataModelsOutputPath + "/result_" + modelId + "_" + execNo

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
  }


  /**
    * 销毁
    */
  def destory(sparkSession: SparkSession, sc : SparkContext) : Unit = {
    if(null != sparkSession){
      //sparkSession.sqlContext.clearCache()
      sparkSession.stop()
    }
    if(null != sc){
      sc.stop()
    }
    //移除守护进程文件
    DfsUtil.remove(fs, processHeart)
    //移除心跳文件
    DfsUtil.remove(fs, processHeartBeat)
    //退出
    System.exit(0)
  }

}


/**
  * 次任务心跳线程
  * @param fs
  * @param heartBeatPath
  */
class HeartBeat(fs : FileSystem, heartBeatPath : Path, heartBeatInterval : Int) extends Runnable{

  private val dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

  override def run(): Unit = {
    while(true){
      DfsUtil.write(fs, heartBeatPath, getNow(), true)
      Thread.sleep(heartBeatInterval)
    }
  }

  def getNow() : String = {
    dateFormat.format(new Date())
  }

}



class ExecutedModel(model : String, modelPath : Path){

  /**
    * 最大重试次数
    * 默认重试三次
    */
  private var maxRetryTimes = 3

  /**
    * 重试最小间隔
    */
  private var minRetryInterval = 1000 * 60 * 1

  /**
    * 下次重试时间
    */
  private var nextRetryTime : Long = System.currentTimeMillis()

  /**
    * 已重试次数
    */
  private var retriedTimes = 0

  private var modelArr : Array[String] = null

  private var execed_once = false

  private var modelValid = false

  private val modelRegExp = "\\d+\\|\\d+\\|[ ]*select .+ from .+"

  private val errMsg = new StringBuilder

  def this(model : String, modelPath : Path, maxRetryTimes : Int, minRetryInterval : Int){
    this(model, modelPath)
    this.maxRetryTimes = maxRetryTimes
    this.minRetryInterval = minRetryInterval
  }

  /**
    * 初始化
    */
  this.init()

  private def init() : Unit = {
    if(null != model && model.trim != ""){
      modelArr = model.split("\\|")

      modelValid = model.toLowerCase().matches(modelRegExp)
    }else{
      modelValid = false
    }
  }

  def retry() : Int = {
    //模型错误
    if(!modelValid){
      return Constant.MODEL_ERROR
    }

    //重试时间未到,等待
    val curTime = System.currentTimeMillis()
    if(curTime < nextRetryTime){
      return Constant.WAIT_RETRY
    }

    //重试达到最大次数
    if(retriedTimes == maxRetryTimes){
      return Constant.MAX_TRIED
    }

    //可以重试
    if(execed_once){
      nextRetryTime = nextRetryTime + minRetryInterval
      retriedTimes = retriedTimes + 1
    }else{
      execed_once = true
    }
    Constant.CAN_RETRY
  }

  /**
    * sql
    * @return
    */
  def getSqlText() : String = {
    return modelArr(2)
  }

  /**
    * 模型id
    * @return
    */
  def getModelId() : String = {
    return modelArr(0)
  }

  /**
    * 批次号
    * @return
    */
  def getExecNo : String = {
    return modelArr(1)
  }

  /**
    * 是否是合法的模型
    * @return
    */
  def isValid() : Boolean = {
    true
  }

  def getModel() : String = {
    return model
  }

  def getTriedTimes() : Int = {
    return retriedTimes
  }

  /**
    * 补偿一次重试
    */
  def compensateRetriedTimes() : Unit = {
    this.retriedTimes = this.retriedTimes - 1
  }

  def getModelPath() : Path = {
    return modelPath
  }

  def addErrMsg(err : String): Unit ={
    this.errMsg.append("at time of '")
    this.errMsg.append(new Date().toString)
    this.errMsg.append("' found error : ")
    this.errMsg.append(err)
    this.errMsg.append("; ")
  }

  def getErrMsg(): String ={
    if(errMsg.size > 0){
      return errMsg.toString()
    }
    return null
  }

}
