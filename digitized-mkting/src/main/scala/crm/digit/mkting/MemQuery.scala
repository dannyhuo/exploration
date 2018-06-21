package crm.digit.mkting

import java.io._
import java.text.SimpleDateFormat

import crm.digit.mkting.df.{DfsUtil, Rdd2DFUtil}
import crm.digit.mkting.sql.TablesSchema
import crm.up.service.UpModelJobHessianServiceProxy
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FSDataInputStream, FSDataOutputStream, FileSystem, Path}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SparkSession}
import java.util.HashMap
import java.util.Date

import com.lvmama.crm.enumerate.UpModelJobEnum
import com.lvmama.crm.up.service.{UpModelJobHessianService, UpModelJobHessianServiceProxy}
import org.apache.spark.rdd.RDD


/**
  * Created by huoqiang on 17/7/2017.
  */
object MemQuery {

  private val tab_conf = "/home/hadoop/upload/conf/table_configs.xml"

  private var execedModels = new HashMap[String, String]
  private val execedModelSerializePath = "/tmp/crm/digitized-mkting/spark-mem-query/execed-models-serialized"

  private val conf = new Configuration()
  conf.addResource("/home/hadoop/app/hadoop/etc/hadoop/hdfs-site.xml")

  private val fs = FileSystem.get(conf)
  private val basePath = "/tmp/crm/digitized-mkting/spark-mem-query"
  private val statePath = new Path(basePath + "/mem-query-state.info")
  private val heartBeatPath = new Path(basePath + "/meme-query-heart-beat.info")
  private val RUNNING = "running"
  private val DEATH = "death"

  private val submitedModelPoolPath = "/tmp/crm/digitized-mkting/spark-mem-query/model-queue"

  private val HEAT_BEAT_INTERVAL_MS = 1000 * 60
  private val dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

  private val output_path = "/tmp/crm/digitized-mkting/spark-mem-query/"

  private val CRM_HESSIAN_URL = "http://10.112.3.98:8080/crm-server/hessian/upModelJobHessianService"
  private val upModelJobHessianServiceProxy = new UpModelJobHessianServiceProxy(CRM_HESSIAN_URL)

  def main(args: Array[String]): Unit = {
    if(isRunning()){
      throw new Exception("this task is running, doesn't running repeat.")
    }

    writeState(RUNNING)

    //启动心跳监测线程
    val t = new Thread(new HeartBeat2(fs, heartBeatPath, HEAT_BEAT_INTERVAL_MS))
    t.start()

    //创建spark session
    var sparkSession : SparkSession = null
    var sc : SparkContext = null
    try{
      val sparkConf = new SparkConf().setMaster("yarn").setAppName("Spark sql in mem")
      sc = new SparkContext(sparkConf)
      sparkSession = SparkSession.builder().config(sparkConf).getOrCreate()
      cacheHbaseTable(sc, sparkSession)
    }catch{
      case ex : Exception => {
        println(ex)
        writeState(DEATH)
      }
    }

    //主线程循环监测提交过来的任务，并准备执行
    try{
      execedModels = DfsUtil.deserializeObject(fs, execedModelSerializePath)
    }catch{
      case ex : Exception => {
        execedModels = new HashMap[String, String]
      }
    }
    while(true){
      Thread.sleep(100)
      try{
        scanTaskPool(sparkSession)
      }catch{
        case ex : Exception => {
          ex.printStackTrace()
        }
      }
    }

    //结束任务
    sparkSession.sqlContext.clearCache()
    writeState(DEATH)
  }

  /**
    *扫描提交过来的任务
    */
  @Deprecated
  def scanSubmitedModels(queue: RDD[String], sparkSession: SparkSession) : Unit = {
    if(!fs.exists(new Path(submitedModelPoolPath))){
      println("The path of '" +submitedModelPoolPath + "' not exists.")
      return
    }
    var curModelInfo : Array[String] = null
    queue.collect().foreach(l => {
      curModelInfo = l.toString.split("\\|")
      if(curModelInfo.length == 2){
        if(null == execedModels.get(curModelInfo(0))){
          println("will exec the sql of '" + curModelInfo(1) + "'.")
          execedModels.put(curModelInfo(0), curModelInfo(1))
          val res = sparkSession.sqlContext.sql(curModelInfo(1))
          //输出结果
          //outputResult(res)
        }
      }else{
        println("This model of '" + l.toString + "' is error!")
      }
    })
  }

  /**
    * 扫描任务池
    * 任务池中的任务均是前台页面提交过来待执行的任务
    * @param sparkSession
    */
  def scanTaskPool(sparkSession : SparkSession) : Unit = {
    val models = fs.listStatus(new Path(submitedModelPoolPath))
    models.foreach(f => {
      val filePath = f.getPath
      if(null == execedModels.get(filePath.getName)){
        var model : String = null
        try{
          model = readLine(filePath)
        }catch{
          case ex : Exception => {
            ex.printStackTrace()
          }
        }
        if(null != model && model.trim != ""){
          val modelArr = model.trim.split("\\|")
          if(modelArr.length == 3){
            val modelId = modelArr(0).toLong
            val execNo = modelArr(1)
            val sqlText = modelArr(2)
            val resultId = beforeExeTask(modelId)//任务执行之前
            var exeOk = false
            var errorMsg : String = null
            if(resultId > -1){
              try{
                val res = sparkSession.sqlContext.sql(sqlText)
                //输出结果
                outputResult(res, modelArr(0), execNo)
                exeOk = true
              }catch{
                case ex : Exception => {
                  exeOk = false
                  errorMsg = ex.getMessage
                  ex.printStackTrace()
                }
              }
            }else{
              errorMsg = "回调crm的hession接口失败"
            }
            //执行成功后，回调crm通知，否则下一轮训继续重新执行
            if(exeOk && afterExeTask(modelId, resultId, exeOk, errorMsg) == 0L) {
              //任务执行之后
              execedModels.put(filePath.getName, model)
              DfsUtil.objectSerialize(fs, execedModels, execedModelSerializePath)
            }
          }else{
            println("This model of '" + model.toString + "' is error!")
          }
        }
      }
    })
  }

  /**
    * 任务执行之前
    */
  def beforeExeTask(modelId : Long) : Long = {
    upModelJobHessianServiceProxy.doEvent(modelId, null, UpModelJobEnum.EXE_STATUS.running, UpModelJobEnum.EXE_EVENT.running, null)
  }

  /**
    * 任务执行之后
    */
  def afterExeTask(modelId : Long, resultId : Long,  exeOk : Boolean, errMsg : String) : Long = {
    var event = UpModelJobEnum.EXE_EVENT.runSucessful
    if(!exeOk){
      event = UpModelJobEnum.EXE_EVENT.emergencyStop
    }
    upModelJobHessianServiceProxy.doEvent(modelId, resultId, UpModelJobEnum.EXE_STATUS.normal, event, errMsg)
  }

  /**
    * 从指定的Path读取一行
    * @param filePath
    * @return
    */
  def readLine(filePath : Path) : String = {
    var file: FSDataInputStream = null
    var reader : InputStreamReader = null
    var buffer : BufferedReader = null
    try{
      file = fs.open(filePath)
      reader = new InputStreamReader(file, "utf-8")
      buffer = new BufferedReader(reader)
      return buffer.readLine()
    }catch{
      case ex : Exception => {
        println(ex)
        if(null != buffer){
          buffer.close()
        }
        if(null != reader){
          reader.close()
        }
        throw new Exception(ex)
      }
    }finally{
      if(null != file){
        file.close()
      }
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
      //df.collect()
    }
  }

  /**
    * 判断是否已启动
    * @return
    */
  def isRunning() : Boolean = {
    if(fs.exists(heartBeatPath)){
      var file : FSDataInputStream = null
      var reader : InputStreamReader = null
      var buffer : BufferedReader = null
      try{
        file = fs.open(heartBeatPath)
        reader = new InputStreamReader(file, "utf-8")
        buffer = new BufferedReader(reader)
        val line = buffer.readLine()
        if(null == line || line.trim == ""){
          return false
        }
        val lastBeatDate = dateFormat.parse(line.trim)
        val interval = new Date().getTime - lastBeatDate.getTime
        if(interval <= HEAT_BEAT_INTERVAL_MS){
          true
        }else{
          false
        }
      }finally{
        if(null != buffer){
          buffer.close()
        }
        if(null != reader){
          reader.close()
        }
        if(null != file){
          file.close()
        }
      }
    }else{
      false
    }
  }

  /**
    * 写程序的运行状态
    * @param state
    */
  def writeState(state: String) : Unit = {
    var writer: OutputStreamWriter = null
    var file : FSDataOutputStream = null
    try{
      file = fs.create(statePath, true)
      writer = new OutputStreamWriter(file, "utf-8")
      writer.write(state)
    }finally{
      if(null != writer){
        writer.close()
      }
      if(null != file){
        file.close()
      }
    }
  }

  /**
    * 获取程序状态
    * running
    * dead
    */
  def getState() : String = {
    if(!fs.exists(statePath)){
      return null
    }
    var reader: InputStreamReader = null
    var buffer : BufferedReader = null
    var file : FSDataInputStream = null
    try{
      file = fs.open(statePath)
      reader = new InputStreamReader(file, "utf-8")
      buffer = new BufferedReader(reader)
      return buffer.readLine()
    }finally{
      if(null != buffer){
        buffer.close()
      }
      if(null != reader){
        reader.close()
      }
      if(null != file){
        file.close()
      }
    }
  }

  /**
    * 输出结果
    * @param df
    */
  def outputResult(df : DataFrame, modelId : String, execNo: String) : Unit = {
    df.show(1500)
    val outPath = output_path + "/result_" + modelId + "_" + execNo
    val outDf = df.coalesce(1)
    outDf.write.text(outPath)
  }

}

class HeartBeat2(fs : FileSystem, heartBeatPath : Path, heartBeatInterval : Int) extends Runnable{
  val dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

  override def run(): Unit = {
    while(true){
      Thread.sleep(heartBeatInterval)
      var writer: OutputStreamWriter = null
      try{
        val file = fs.create(heartBeatPath, true)
        writer = new OutputStreamWriter(file, "utf-8")
        writer.write(getNow())
      }finally{
        if(null != writer){
          writer.close()
        }
      }
    }
  }

  def getNow() : String = {
    dateFormat.format(new Date())
  }
}
