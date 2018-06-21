package crm.digit.mkting.mem.query

import java.text.SimpleDateFormat
import java.util.Properties
import java.util.concurrent.Callable

import com.caucho.hessian.client.HessianRuntimeException
import com.lvmama.crm.enumerate.UpModelJobEnum
import com.lvmama.crm.up.service.UpModelJobHessianServiceProxy
import crm.digit.mkting.mem.query.constant.Constant
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.SparkException
import org.apache.spark.sql.{AnalysisException, DataFrame, SparkSession}

/**
  * Created by huoqiang on 24/7/2017.
  */
abstract class MemQueryTask(fs : FileSystem, upModelJobHessianServiceProxy : UpModelJobHessianServiceProxy, conf : Properties) extends Callable[Any]{

  //任务池相关参数
  protected val crmDataModelsOutputPath = new Path(conf.getProperty("crm.data.models.output"))
  private val crmDataModelExecedFailedDir = conf.getProperty("crm.data.model.history.failed")
  private val crmDataModelExecedOkayDir = conf.getProperty("crm.data.model.history.okay")
  private val crmDataModelErrorDir = conf.getProperty("crm.data.model.history.modelerr")
  protected val dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

  protected var dfWrite = (conf.getProperty("crm.df.write.style") == "df")

  def outputResult(df : DataFrame, executingModel : ExecutingModel) : Unit

  /**
    * 处理每一个模型
    */
  protected def doExecutingModel(executingModel : ExecutingModel, sparkSession: SparkSession) : Any = {
    if(null != executingModel){
      while(executingModel.canTry()){
        doEachTry(executingModel, sparkSession)
      }
    }
  }

  /**
    * 处理每一个模型
    */
  protected def doEachTry(executingModel : ExecutingModel, sparkSession: SparkSession) : Any = {
    var errorMsg : String = null
    var resultId : Long = -1
    val preTry = executingModel.retry()
    if(preTry == Constant.CAN_RETRY){
      //任务执行前，通知crm系统任务开始
      println("start to try, pretry is " + preTry + " ...............................................")
      resultId = beforeExeTask(executingModel.getModelId().toLong)
      if(resultId > -1){
        try{
          val res = sparkSession.sqlContext.sql(executingModel.getSqlText())

          //输出结果
          println("start to output result .......................................................")
          outputResult(res, executingModel)

          //执行成功后，回调crm通知
          println("start to notify crm sys.......................................................")
          afterExeTask(executingModel, Constant.EXEC_OKAY, resultId)
        }catch{
          case ex : AnalysisException => {
            //sql语法解析错误
            ex.printStackTrace()
            executingModel.addErrMsg(ex.getMessage())
            afterExeTask(executingModel, Constant.MODEL_ERROR, resultId)
            println(errorMsg)
          }
          case ex : SparkException => {
            //spark exception
            ex.printStackTrace()
            executingModel.addErrMsg(ex.getMessage())
            println(errorMsg)
          }
          case ex : NullPointerException => {
            ex.printStackTrace()
            executingModel.addErrMsg(ex.getMessage())
            afterExeTask(executingModel, Constant.MODEL_ERROR, resultId)
            println(errorMsg)
          }
          case ex : Exception => {
            errorMsg = ex.getMessage
            executingModel.addErrMsg(errorMsg)
            ex.printStackTrace()
            println(errorMsg)
          }
          case ex : RuntimeException => {
            errorMsg = ex.getMessage
            executingModel.addErrMsg(errorMsg)
            ex.printStackTrace()
            println(errorMsg)
          }
        }
      }else{
        errorMsg = "任务["+executingModel.getModelId()+"_"+executingModel.getExecNo+"]开始前，通知crm系统失败, 任务未启动!"
        println(errorMsg)
        executingModel.addErrMsg(errorMsg)
        //任务未开始，抵消一次重试次数
        executingModel.compensateRetriedTimes()
      }
    }else if(preTry == Constant.MODEL_ERROR){
      //模型异常
      errorMsg = "The model of '" + executingModel.getModel() + "' error!"
      println(errorMsg)
      executingModel.addErrMsg(errorMsg)
      afterExeTask(executingModel, preTry, resultId)
    }else if(preTry == Constant.MAX_TRIED){
      //重试达到最大次数
      errorMsg = "The model of '"+executingModel.getModel()+"' retry for '" + executingModel.getTriedTimes() + "' times till error!"
      println(errorMsg)
      executingModel.addErrMsg(errorMsg)
      afterExeTask(executingModel, preTry, resultId)
    }else{
      //任务未开始，抵消一次重试次数
      //println("unknow event of " + preTry + ".....................................................................................")
      executingModel.compensateRetriedTimes()
    }
  }


  /**
    * 任务执行之前通知crm系统
    */
  def beforeExeTask(modelId : Long) : Long = {
    println("start to exec model of................................................................ " + modelId)
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
  def afterExeTask(executingModel: ExecutingModel, event : Int, resultId : Long) : Boolean = {
    println(".............................................................................after exe task : resultId = " + resultId + " event = " + event + " model = " + executingModel.getModel())
    //1、通知crm系统
    if(resultId > 0){
      //大于0，表示启动任务前告知crm系统成功,则需要通知crm任务停止
      var crmEvent = UpModelJobEnum.EXE_EVENT.emergencyStop
      if(event == Constant.EXEC_OKAY){
        crmEvent = UpModelJobEnum.EXE_EVENT.runSucessful
      }
      var res = -1L
      try{
        res = upModelJobHessianServiceProxy.doEvent(executingModel.getModelId().toLong, resultId,
          UpModelJobEnum.EXE_STATUS.normal, crmEvent, executingModel.getErrMsg())
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
    val modelPath = executingModel.getModelPath()
    var toPath : Path = null
    if(event == Constant.NOTIFY_CRM_FAILED){
      //暂不做处理，稍后重试
      println(".........................................NOTIFY_CRM_FAILED failed.")
    }else if(event == Constant.MAX_TRIED){
      println("fs.rename(modelPath, new Path(crmDataModelExecedFailedDir + modelPath.getName)), modelPath " + modelPath + " => " + crmDataModelExecedFailedDir + modelPath.getName)
      toPath = new Path(crmDataModelExecedFailedDir + modelPath.getName)
      return mv(modelPath, toPath)
    }else if(event == Constant.EXEC_OKAY){
      toPath = new Path(crmDataModelExecedOkayDir + modelPath.getName)
      return mv(modelPath, toPath)
    }else if(event == Constant.MODEL_ERROR){
      toPath = new Path(crmDataModelErrorDir + modelPath.getName)
      return mv(modelPath, toPath)
    }else{
      println(".............................................unknown event " + event)
    }
    return false
  }

  /**
    * 移动hdfs中的文件
    * @param sourcePath
    * @param desPath
    * @return
    */
  def mv(sourcePath : Path, desPath : Path) : Boolean = {
    println("..................................................will mv " + sourcePath + "to " + desPath)
    if(fs.exists(desPath)){
      fs.delete(sourcePath, false)
    }
    fs.rename(sourcePath, desPath)
    if(fs.exists(sourcePath)){
      println("..................................................failed to mv " + sourcePath + "to " + desPath)
      return false
    }else{
      return true
    }
  }

}
