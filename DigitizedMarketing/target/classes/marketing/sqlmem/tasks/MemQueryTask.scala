package marketing.sqlmem.tasks

import java.util.concurrent.Callable

import com.caucho.hessian.client.HessianRuntimeException
import com.lvmama.crm.enumerate.UpModelJobEnum
import marketing.sqlmem.model.{SqlMemHeart, ExecutingModel}
import marketing.sqlmem.constant.Constant
import marketing.sqlmem.util.DfsUtil
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.SparkException
import org.apache.spark.sql.{AnalysisException, DataFrame, SparkSession}

/**
  * Created by huoqiang on 24/7/2017.
  */
abstract class MemQueryTask(fs : FileSystem, heart : SqlMemHeart) extends Callable[Any] with Serializable{

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
      println("Start to try " + executingModel.getNo + " for " + executingModel.getTriedTimes() + " times.")
      resultId = beforeExeTask(executingModel.getModelId().toLong)
      if(resultId > -1){
        try{
          println("Start to execute the sql : '" + executingModel.getSqlText() + "'.")
          val res = sparkSession.sql(executingModel.getSqlText())

          //输出结果
          println("Execute the sql successfully, then out put the result.")
          outputResult(res, executingModel)

          //执行成功后，回调crm通知
          println("Out put the result successfully, will infirm the crm system task exec okay.")
          afterExeTask(executingModel, Constant.EXEC_OKAY, resultId)
        }catch{
          case ex : AnalysisException => {
            //sql语法解析错误
            errorMsg = ex.getMessage()
            executingModel.addErrMsg(ex.getMessage())
            println("The sql found AnalysisException : " + errorMsg + ". It["+executingModel.getNo+"] will be moved to the model error history directory.")
            ex.printStackTrace()
            afterExeTask(executingModel, Constant.MODEL_ERROR, resultId)
          }
          case ex : SparkException => {
            //spark exception
            errorMsg = ex.getMessage()
            executingModel.addErrMsg(errorMsg)
            println("Execute the sql found SparkException : " + errorMsg + ". Will retry it["+executingModel.getNo+"] for a moment.")
            ex.printStackTrace()
          }
          case ex : NullPointerException => {
            errorMsg = ex.getMessage()
            executingModel.addErrMsg(errorMsg)
            afterExeTask(executingModel, Constant.MODEL_ERROR, resultId)
            println("Execute the sql found NullPointerException : " + errorMsg + ". Will retry it["+executingModel.getNo+"] for a moment.")
            ex.printStackTrace()
          }
          case ex : Exception => {
            errorMsg = ex.getMessage
            executingModel.addErrMsg(errorMsg)
            println("Execute the sql found NullPointerException : " + errorMsg + ". Will retry it["+executingModel.getNo+"] for a moment.")
            ex.printStackTrace()
          }
          case ex : RuntimeException => {
            errorMsg = ex.getMessage
            executingModel.addErrMsg(errorMsg)
            println("Execute the sql found RuntimeException : " + errorMsg + ". Will retry it["+executingModel.getNo+"] for a moment.")
            ex.printStackTrace()
          }
        }
      }else{
        errorMsg = "Before start task["+executingModel.getNo+"] , inform crm sys failed, task don't start, retry for a moment!"
        println(errorMsg)
        executingModel.addErrMsg(errorMsg)
        //任务未开始，抵消一次重试次数
        executingModel.compensateRetriedTimes()
      }
    }else if(preTry == Constant.MODEL_ERROR){
      //模型异常
      errorMsg = "The model of '" + executingModel.getModel() + "' is invalid, will be moved to the model error history directory!"
      println(errorMsg)
      executingModel.addErrMsg(errorMsg)
      afterExeTask(executingModel, preTry, resultId)
    }else if(preTry == Constant.MAX_TRIED){
      //重试达到最大次数
      errorMsg = "The model of '"+executingModel.getModel()+"' retry for '" + executingModel.getTriedTimes() + "' times till error, will be moved to the model failed history directory!"
      println(errorMsg)
      executingModel.addErrMsg(errorMsg)
      afterExeTask(executingModel, preTry, resultId)
    }else{
      //任务未开始，抵消一次重试次数
      executingModel.compensateRetriedTimes()
    }
  }


  /**
    * 任务执行之前通知crm系统
    */
  def beforeExeTask(modelId : Long) : Long = {
    println("Before task["+modelId+"] start, to inform the crm sys.")
    var result = -1L
    try{
      result = heart.upModelJobHessianServiceProxy.doEvent(modelId, null,
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
    println("Do afterExeTask() : resultId = " + resultId + ", event = " + event + ", No = " + executingModel.getNo)
    //1、通知crm系统
    if(resultId > 0){
      //大于0，表示启动任务前告知crm系统成功,则需要通知crm任务停止
      var crmEvent = UpModelJobEnum.EXE_EVENT.emergencyStop
      if(event == Constant.EXEC_OKAY){
        crmEvent = UpModelJobEnum.EXE_EVENT.runSucessful
      }
      var res = -1L
      try{
        res = heart.upModelJobHessianServiceProxy.doEvent(executingModel.getModelId().toLong, resultId,
          UpModelJobEnum.EXE_STATUS.normal, crmEvent, executingModel.getErrMsg())
      }catch{
        case ex : HessianRuntimeException => {
          println("Call crm hessian interface found error : " + ex.getMessage)
          ex.printStackTrace()
        }
      }
      if(res != 0){
        println("Inform the crm system failed, the return is " + res)
      }
    }

    //2、释放模型资源
    val notifyFlag = notifyTaskModel(executingModel, event)
    if(notifyFlag){
      println("Notify the task model["+executingModel.getNo+"] failed.")
    }
    return notifyFlag
  }

  /**
    * 释放模型
    * @param executingModel
    * @param event
    * @return
    */
  def notifyTaskModel(executingModel: ExecutingModel, event : Int) : Boolean = {
    println("Notify the task model, the event is " + event)
    val modelPath = executingModel.getModelPath()
    var toPath : Path = null
    if(event == Constant.NOTIFY_CRM_FAILED){
      //暂不做处理，稍后重试
    }else if(event == Constant.MAX_TRIED){
      //println("fs.rename(modelPath, new Path(crmDataModelExecedFailedDir + modelPath.getName)), modelPath " + modelPath + " => " + conf.crmDataModelExecedFailedDir + modelPath.getName)
      toPath = new Path(heart.crmDataModelExecedFailedDir + modelPath.getName)
      return DfsUtil.mv(fs, modelPath, toPath)
    }else if(event == Constant.EXEC_OKAY){
      toPath = new Path(heart.crmDataModelExecedOkayDir + modelPath.getName)
      return DfsUtil.mv(fs, modelPath, toPath)
    }else if(event == Constant.MODEL_ERROR){
      toPath = new Path(heart.crmDataModelErrorDir + modelPath.getName)
      return DfsUtil.mv(fs, modelPath, toPath)
    }else{
      println("Unknown event " + event)
    }
    return true
  }

  /**
    * 输出结果
    * @param df
    * @param executingModel
    */
  def outputResult(df : DataFrame, executingModel : ExecutingModel) : Unit

}