package com.lvmama.marketing.sqlmem.constant

/**
  * Created by huoqiang on 20/7/2017.
  */
object Constant {

  val CAN_RETRY = 0
  val MODEL_ERROR = 1
  val MAX_TRIED = 2
  val WAIT_RETRY = 3
  val NOTIFY_CRM_FAILED = 4
  val EXEC_OKAY = 5
  val EXEC_FAILED = 6

  /**模型类型－标签模型*/
  val MODEL_TAG = 1
  /**模型类型－运营模型*/
  val MODEL_OPERATOR = 2
  /**模型类型－临时查询的sql*/
  val MODEL_TMP_SQL = 3

  /**模型校验正则表达式*/
  val modelRegExp = "\\d+\\|\\d+\\|\\d\\|([A-Za-z_$]+[A-Za-z0-9_$]*)?\\|[ ]*select .+ from .+"
  /**模型校验*/
  def modelIsValid(modelText : String) : Boolean = {
    if(null == modelText){
      return false
    }
    return modelText.toLowerCase().matches(modelRegExp)
  }


  def main(args: Array[String]): Unit = {
    val model = "226|20170720|2|a1|select distinct user_portrait.user_id from user_portrait where  user_portrait.created_date<'2017-01-01 13:54:24' and user_portrait.created_date>'2016-06-01 13:54:42'"
    println(modelIsValid(model))
  }

  val sparkMemQueryFlag = """
      ____              __       __    __    _____
     / __/__  ___ _____/ /__    /  \  /  \  / __  \
    _\ \/ _ \/ _ `/ __/  '_/   / /\ \/ /\ \| |__| |
   /___/ .__/\_,_/_/ /_/\_\   /_/  \__/  \_\\_ _  |
      /_/                                       |_|
                        """



}
