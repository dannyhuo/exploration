package com.lvmama.marketing.sqlmem.tasks

import java.beans.Transient
import java.util.HashMap

import com.lvmama.marketing.sqlmem.model.{ExecutingModel, SqlMemHeart}
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Created by huoqiang on 24/7/2017.
  */
class TagModelTask(sparkSession: SparkSession, executingModel : ExecutingModel,
                   @Transient heart : SqlMemHeart, userPortraitDf : DataFrame)
  extends AbstactSqlMemTask(heart = heart){

  override def call(): Any = {
    println("tag model task [" + executingModel.getModel() + "] will start ......")
    super.doExecutingModel(executingModel, sparkSession)
    println("tag model task [" + executingModel.getModelId() + "] finished")
  }

  override def outputResult(df : DataFrame, executingModel: ExecutingModel): Boolean = {
    //筛选出待改动的标签
    val map = new HashMap[String, Char]
    val rowKey = "user_id"

    df.foreach(r => {
      val userId = r.getAs(rowKey)
      map.put(userId, 'Y')
    })

    userPortraitDf.foreach(r => {
      val tagIndex = r.fieldIndex(executingModel.getTagName())
      val userId = r.getAs(rowKey)
      if(tagIndex > -1){
        val value = r.getAs(executingModel.getTagName())
        if(null != value && value == 'Y'){
          if(null != map.get(userId)){
            map.remove(userId)//标签已存在，不做处理
          }
        }else{
          map.put(userId, 'N')//待移除标签
        }
      }else{
        println("tag not exists, will add it")
        if(null == map.get(userId)){
          map.put(userId, 'N')
        }
      }
    })

    //2、打标签
    println("start to tag .......................................................")
    val iter = map.keySet().iterator()
    while(iter.hasNext){
      //待写回hbase
      println(iter.next())
    }

    return true
  }

}
