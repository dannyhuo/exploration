package com.lvmama.marketing.sqlmem.model

import java.beans.Transient
import java.text.SimpleDateFormat
import java.util.concurrent.{ConcurrentHashMap, Future}
import java.util.{Date, HashMap, Properties}

import com.lvmama.crm.up.service.UpModelJobHessianServiceProxy
import com.lvmama.marketing.sqlmem.util.DfsUtil
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.DataFrame

/**
  * Created by huoqiang on 27/7/2017.
  */
class SqlMemHeart(confPath : String) extends Serializable{

  private val conf = new Properties()

  @Transient
  private var confStream = this.getClass.getClassLoader.getResourceAsStream("local/heart.properties")
  conf.load(confStream)
  confStream.close()
  confStream = null

  /**
    * hbase中表结构的配置
    */
  val hbaseTableConf = conf.getProperty("crm.hbase.table.conf.path")

  /**
    * dfs配置
    */
  private val dfsConf = new Configuration()
  dfsConf.addResource(conf.getProperty("crm.hdfs.site.xml.path"))
  val fs = FileSystem.get(dfsConf)

  //守护进程相关参数
  val processHeart = new Path(conf.getProperty("process.heart"))
  val processHeartBeat = new Path(conf.getProperty("process.heart.beat"))
  val heartBeatInterval : Int = conf.getProperty("process.heart.beat.interval.ms").toInt

  //任务池相关参数
  val crmDataModelsPool = new Path(conf.getProperty("crm.data.models.pool"))

  val crmDataModelsOutputPath = conf.getProperty("crm.data.models.output")
  val crmDataModelExecedFailedDir : String = conf.getProperty("crm.data.model.history.exefailed")
  val crmDataModelExecedOkayDir : String = conf.getProperty("crm.data.model.history.exeokay")
  val crmDataModelErrorDir : String = conf.getProperty("crm.data.model.history.model.error")
  val crmDataModelIgnoredDir : String = conf.getProperty("crm.data.model.history.model.ignored")

  val crmTagModelEnabled = (conf.getProperty("crm.tag.model.enabled") == "true")
  val appName = conf.getProperty("sql.mem.query.app.name")
  val startUpMOdel = conf.getProperty("sql.mem.query.app.startup.model")

  /**失败重试次数*/
  var failedRetryTimes : Int = 0
  if(null != conf.getProperty("sql.mem.query.failed.retry.times")){
    failedRetryTimes = conf.getProperty("sql.mem.query.failed.retry.times").toInt
  }
  /**失败重试间隔*/
  var failedRetryInterval : Int = 0
  if(null != conf.getProperty("sql.mem.query.failed.retry.interval.ms")){
    failedRetryInterval = conf.getProperty("sql.mem.query.failed.retry.interval.ms").toInt
  }
  /**监听任务池的时间频次*/
  val monitorHz = 100
  if(null != conf.getProperty("sql.mem.query.model.pool.monitor.hz")){
    failedRetryInterval = conf.getProperty("sql.mem.query.model.pool.monitor.hz").toInt
  }

  /**启动是否缓存数据*/
  var initCacheData = conf.getProperty("sql.mem.query.datasource.init.cache") == "true"
  /**缓存级别*/
  var storageLevel = "MEMORY_AND_DISK"
  if(null != conf.getProperty("sql.mem.query.datasource.storage.level")){
    storageLevel = conf.getProperty("sql.mem.query.datasource.storage.level")
  }

  /**crm回调接口相关*/
  val upModelJobHessianServiceProxy = new UpModelJobHessianServiceProxy(conf.getProperty("crm.up.model.job.hessian.url"))

  val dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
  val timeFormat = new SimpleDateFormat("HH:mm")

  /**df写的方式，df或rdd*/
  var dfWrite : Boolean = (conf.getProperty("crm.df.write.style") == "df")

  /**模型并行执行次数*/
  val crmModelConcurrencyNum = conf.getProperty("crm.model.concurrency.num").toInt

  val futures = new ConcurrentHashMap[String, Future[_]]

  val dataSource = new HashMap[String, DataFrame]

  //es相关配置
  val esNodes = conf.getProperty("es.nodes")
  val esPort = conf.getProperty("es.port")
  val esIndexAutoCreate = conf.getProperty("es.index.auto.create")
  val esNodesWanOnly = conf.getProperty("es.nodes.wan.only")

  /**
    * 缓存的df的分区大小
    */
  val dfPartitionSize = conf.getProperty("crm.model.df.partition.size").toInt

  /**
    * 预处理Path，不存在则创建目录及父目录
    */
  def preparePath() : Unit = {
    if(!fs.exists(processHeart.getParent)){
      fs.mkdirs(processHeart.getParent)
    }
    if(!fs.exists(processHeartBeat.getParent)){
      fs.mkdirs(processHeartBeat.getParent)
    }
    if(!fs.exists(crmDataModelsPool)){
      fs.mkdirs(crmDataModelsPool)
    }

    DfsUtil.dirPrepare(fs, crmDataModelsOutputPath)
    DfsUtil.dirPrepare(fs, crmDataModelExecedFailedDir)
    DfsUtil.dirPrepare(fs, crmDataModelExecedOkayDir)
    DfsUtil.dirPrepare(fs, crmDataModelErrorDir)
    DfsUtil.dirPrepare(fs, crmDataModelIgnoredDir)
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
      if(interval <= heartBeatInterval){
        return true
      }
    }
    return false
  }

  def isAlive() : Boolean = {
    return fs.exists(processHeart)
  }

  this.preparePath()
}
