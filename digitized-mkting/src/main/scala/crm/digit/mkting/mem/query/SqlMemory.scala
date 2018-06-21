package crm.digit.mkting.mem.query

import java.io.{FileNotFoundException, IOException}
import java.text.SimpleDateFormat
import java.util.concurrent._
import java.util.{Date, HashMap, Properties}

import com.caucho.hessian.client.HessianRuntimeException
import com.lvmama.crm.enumerate.UpModelJobEnum
import com.lvmama.crm.up.service.UpModelJobHessianServiceProxy
import crm.digit.mkting.{ExecutedModel, HeartBeat}
import crm.digit.mkting.df.{DfsUtil, Rdd2DFUtil}
import crm.digit.mkting.mem.query.constant.Constant
import crm.digit.mkting.sql.TablesSchema
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path, PathIOException}
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.spark.{SparkConf, SparkContext, SparkException}
import org.apache.spark.sql.{AnalysisException, DataFrame, SparkSession}

/**
  * Created by huoqiang on 24/7/2017.
  */
object SqlMemory {
  private val properties = new Properties()
  private val confStream = SqlMemory.getClass.
    getClassLoader.getResourceAsStream("local/zoubing.properties")
  properties.load(confStream)
  confStream.close()

  /**
    * hbase表配置
    */
  private val tab_conf = properties.getProperty("crm.hbase.table.conf.path") //"/home/hadoop/upload/conf/table_configs.xml"

  /**
    * 执行过的模型
    */
  private var execedModels = new HashMap[String, ExecutingModel]

  /**
    * dfs配置
    */
  private val dfsConf = new Configuration()
  dfsConf.addResource(properties.getProperty("crm.hdfs.site.xml.path"))

  /**
    * fs
    */
  private val fs = FileSystem.get(dfsConf)

  //任务进程相关参数
  private val processHeart = new Path(properties.getProperty("process.heart"))
  private val processHeartBeat = new Path(properties.getProperty("process.heart.beat"))
  private val HEART_BEAT_INTERVAL_MS = properties.getProperty("process.heart.beat.interval.ms").toInt

  //任务池相关参数
  private val crmDataModelsPool = new Path(properties.getProperty("crm.data.models.pool"))
  private val crmDataModelsOutputPath = new Path(properties.getProperty("crm.data.models.output"))
  private val crmDataModelExecedFailedDir = properties.getProperty("crm.data.model.history.failed")
  private val crmDataModelExecedOkayDir = properties.getProperty("crm.data.model.history.okay")
  private val crmDataModelErrorDir = properties.getProperty("crm.data.model.history.modelerr")

  //crm回调接口相关
  private val upModelJobHessianServiceProxy = new UpModelJobHessianServiceProxy(properties.getProperty("crm.up.model.job.hessian.url"))

  private val dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
  private val timeFormat = new SimpleDateFormat("HH:mm")

  private var dfWrite = (properties.getProperty("crm.df.write.style") == "df")

  private val pool : ExecutorService = Executors.newFixedThreadPool(properties.getProperty("crm.model.concurrency.num").toInt)

  private val futures = new ConcurrentHashMap[String, Future[_]]

  private val dataSource = new HashMap[String, DataFrame]

  /**
    * 缓存的df的分区大小
    */
  private val dfPartitionSize = properties.getProperty("crm.model.df.partition.size").toInt

  /**
    * 数据源更新缓存时间
    */
  private val updateCacheTime = "02:00"

  /**
    * 检查配置路径，不存在则创建
    */
  private def prepare() : Unit = {
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

  /**
    * 判断是否已启动
    * @return
    */
  private def isRunning() : Boolean = {
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
      }
    }
    return false
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
    val t = new Thread(new MemQueryHeartBeat(fs, processHeartBeat, HEART_BEAT_INTERVAL_MS))
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

    //6、轮循监听任务池
    var flag = 0
    while(fs.exists(processHeart)){
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

    //7、结束任务
    destory(sparkSession, sc)
  }

  /**
    * 检查缓存
    */
  private def checkCacheUpdate(sc : SparkContext, sparkSession : SparkSession) : Unit = {
    val time = timeFormat.format(new Date(System.currentTimeMillis()))
    if(time == updateCacheTime){
      println("update the cache...................................................................")
      cacheHbaseTable(sc, sparkSession)
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
        println("future.get() is : " + res)
        println("futures remove key is "+key+" ..........................Futures's size is " + futures.size())
        iter.remove()
        println("futures remove key is "+key+" ..........................Futures's size is " + futures.size())

        println("execedModels remove key is "+key+" .......................execedModels's size is " + execedModels.size())
        execedModels.remove(key)
        println("execedModels remove key is "+key+" .......................execedModels's size is " + execedModels.size())
      }
    }
  }


  /**
    * 扫描任务池
    * 任务池中的任务均是前台页面提交过来待执行的任务
    * @param sparkSession
    */
  private def scanModelTaskPool(sparkSession : SparkSession) : Unit = {
    val models = fs.listStatus(crmDataModelsPool)
    models.foreach(f = f => {
      val filePath = f.getPath
      var execedModel = execedModels.get(filePath.getName)
      if (null == execedModel) {
        var model: String = null
        try {
          model = DfsUtil.readLine(fs, filePath)
        } catch {
          case ex: Exception => {
            println("read model of " + filePath + " found exception!")
            ex.printStackTrace()
          }
          case ex: FileNotFoundException => {
            println("read model of " + filePath + " found FileNotFoundException!")
            ex.printStackTrace()
          }
          case ex: IOException => {
            println("read model of " + filePath + " found IOException!")
            ex.printStackTrace()
          }
          case ex: PathIOException => {
            println("read model of " + filePath + " found PathIOException!")
            ex.printStackTrace()
          }
        }
        if (Constant.modelIsValid(model)) {
          execedModel = new ExecutingModel(model, filePath)
          execedModels.put(filePath.getName, execedModel)
        }else{
          //不合法的模型，直接移除掉
          val toPath = new Path(crmDataModelErrorDir + filePath.getName)
          if(!fs.exists(toPath)){
            fs.rename(filePath, toPath)
          }else{
            fs.delete(filePath, false)
          }
        }
      }

      if (null != execedModel) {
        if(null == futures.get(filePath.getName)){
          var future : Future[_] = null
          if(execedModel.getModelType() == Constant.MODEL_OPERATOR){
            println("operator model of " + execedModel.getModelPath() + " .........................")
            future = pool.submit(new OperatorModelTask(sparkSession, execedModel,
              fs, upModelJobHessianServiceProxy, properties))
          }else if(execedModel.getModelType() == Constant.MODEL_TAG){
            println("tag model of " + execedModel.getModelPath() + " .........................")
            future = pool.submit(new TagModelTask(sparkSession, execedModel,
              fs, upModelJobHessianServiceProxy, properties, dataSource.get("user_portrait")))
          }else if(execedModel.getModelType() == Constant.MODEL_TMP_SQL){
            println("tmp sql model of " + execedModel.getModelPath() + " .........................")
          }else{
            println("unknown model of " + execedModel.getModelPath() + " .........................")
          }

          if(null != future){
            futures.put(filePath.getName, future)
          }
        }
      } else {
        println("Read the model of '" + filePath + "' get the null value!")
      }
    })
  }


  /**
    * 缓存hbase的表
    * @param sc
    * @param sparkSession
    */
  private def cacheHbaseTable(sc : SparkContext, sparkSession : SparkSession) : Unit = {
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
      val df = Rdd2DFUtil.hbsRdd2DF(sparkSession, hbsRdd, value._1, familyName).repartition(dfPartitionSize)
      //创建表
      df.createOrReplaceTempView(table)
      println("create table view of " + table + " finished.")
      //缓存表，方便后面查询更快
      df.cache()
      dataSource.put(table,df)
    }
  }


  /**
    * 销毁
    */
  private def destory(sparkSession: SparkSession, sc : SparkContext) : Unit = {
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

