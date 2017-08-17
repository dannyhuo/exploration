package marketing.sqlmem.model

import java.beans.Transient

import marketing.sqlmem.constant.Constant
import java.util.Date

import org.apache.hadoop.fs.Path

/**
  * Created by huoqiang on 24/7/2017.
  * @param model 格式如下：
  *              modelId|execNo|ModelType|TagName|Sql
  */
class ExecutingModel(model : String, modelUrl : String) extends Serializable{

  /**
    * 最大重试次数
    * 默认重试三次
    */
  private var maxRetryTimes = 2

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

  @Transient
  private var modelPath : Path = null

  private var modelArr : Array[String] = null

  private var execed_once = false

  private var modelValid = false

  private val errMsg = new StringBuilder


  def this(model : String, modelPath : String, maxRetryTimes : Int, minRetryInterval : Int){
    this(model, modelPath)
    if(maxRetryTimes > 0){
      this.maxRetryTimes = maxRetryTimes
    }
    if(minRetryInterval > 0){
      this.minRetryInterval = minRetryInterval
    }
  }

  /**
    * 初始化
    */
  this.init()

  private def init() : Unit = {
    modelValid = Constant.modelIsValid(model)
    if(modelValid){
      modelArr = model.split("\\|")
    }
  }

  def canTry() : Boolean = {
    //模型错误
    if(!modelValid){
      return false
    }

    //重试达到最大次数
    if(retriedTimes == maxRetryTimes){
      return false
    }

    return true
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
    * 模型类型
    * 标签模型，运营模型
    * @return
    */
  def getModelType() : Int = {
    return modelArr(2).toInt
  }

  /**
    * 标签名称
    * @return
    */
  def getTagName() : String = {
    return modelArr(3)
  }

  /**
    * sql
    * @return
    */
  def getSqlText() : String = {
    return modelArr(4)
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

  /**
    * 获取模型池中该模型的路径
    * @return
    */
  def getModelPath() : Path = {
    if(null == modelPath){
      modelPath = new Path(modelUrl)
    }
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

  /**
    * 验证提交过来的模型是否正确
    * @return
    */
  def isModelValid() : Boolean = {
    return  modelValid
  }

  /**
    * 编号
    * 模型ID+批次号
    * @return
    */
  def getNo : String = {
    return getModelId() + "_" + getExecNo
  }
}
