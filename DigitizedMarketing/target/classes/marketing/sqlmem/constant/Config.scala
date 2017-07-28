package marketing.sqlmem.constant

import java.beans.Transient
import java.text.SimpleDateFormat
import java.util.{Date, HashMap, Properties}
import java.util.concurrent.{ConcurrentHashMap, Future}

import com.lvmama.crm.up.service.UpModelJobHessianServiceProxy
import marketing.sqlmem.util.DfsUtil
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.DataFrame

/**
  * Created by huoqiang on 27/7/2017.
  */
class Config (confPath : String) extends Serializable{

  @Transient private val conf = new Properties()

  @Transient private var confStream = this.getClass.getClassLoader.getResourceAsStream("local/sqlmemory-default.properties")
  conf.load(confStream)
  confStream.close()
  confStream = null

  val hbaseTableConf = conf.getProperty("crm.hbase.table.conf.path")

  /**
    * dfs配置
    */
  @Transient private val dfsConf = new Configuration()
  dfsConf.addResource(conf.getProperty("crm.hdfs.site.xml.path"))
  @Transient val fs = FileSystem.get(dfsConf)

  //任务进程相关参数
  @Transient val processHeart = new Path(conf.getProperty("process.heart"))

  @Transient val processHeartBeat = new Path(conf.getProperty("process.heart.beat"))
  val HEART_BEAT_INTERVAL_MS : Int = conf.getProperty("process.heart.beat.interval.ms").toInt

  //任务池相关参数
  @Transient val crmDataModelsPool = new Path(conf.getProperty("crm.data.models.pool"))
  val crmDataModelsOutputPath = conf.getProperty("crm.data.models.output")
  val crmDataModelExecedFailedDir : String = conf.getProperty("crm.data.model.history.failed")
  val crmDataModelExecedOkayDir : String = conf.getProperty("crm.data.model.history.okay")
  val crmDataModelErrorDir : String = conf.getProperty("crm.data.model.history.modelerr")

  //crm回调接口相关
  @Transient val upModelJobHessianServiceProxy = new UpModelJobHessianServiceProxy(conf.getProperty("crm.up.model.job.hessian.url"))

  val dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
  val timeFormat = new SimpleDateFormat("HH:mm")

  var dfWrite : Boolean = (conf.getProperty("crm.df.write.style") == "df")

  val crmModelConcurrencyNum = conf.getProperty("crm.model.concurrency.num").toInt

  val futures = new ConcurrentHashMap[String, Future[_]]

  val dataSource = new HashMap[String, DataFrame]

  /**
    * 缓存的df的分区大小
    */
  val dfPartitionSize = conf.getProperty("crm.model.df.partition.size").toInt

  /**
    * 数据源更新缓存时间
    */
  val updateCacheTime = "02:00"

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
      }
    }
    return false
  }

  def isAlive() : Boolean = {
    return fs.exists(processHeart)
  }

}
