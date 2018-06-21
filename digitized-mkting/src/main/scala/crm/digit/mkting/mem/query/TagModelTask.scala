package crm.digit.mkting.mem.query

import java.util.{HashMap, Properties}

import com.lvmama.crm.up.service.UpModelJobHessianServiceProxy
import org.apache.hadoop.fs.FileSystem
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Created by huoqiang on 24/7/2017.
  */
class TagModelTask(sparkSession: SparkSession, executingModel : ExecutingModel,
                   fs : FileSystem, upModelJobHessianServiceProxy : UpModelJobHessianServiceProxy,
                   conf : Properties, userPortraitDf : DataFrame)
  extends MemQueryTask(fs = fs, upModelJobHessianServiceProxy = upModelJobHessianServiceProxy, conf = conf){

  override def call(): Any = {
    println("tag model task [" + executingModel.getModel() + "] will start ......")
    super.doExecutingModel(executingModel, sparkSession)
    println("tag model task [" + executingModel.getModelId() + "] finished")
  }

  override def outputResult(df : DataFrame, executingModel: ExecutingModel): Unit = {
    return
    //筛选出待改动的标签
    val map = new HashMap[String, Char]
    val rowKey = "user_id"

    df.foreach(r => {
      val userId = r.getAs(rowKey)
      println("............................" + userId)
      map.put(userId, 'Y')
    })

    userPortraitDf.foreach(r => {
      val value = r.getAs("tag_swload12")
      val userId = r.getAs(rowKey)
      if(null != value && value == 'Y'){
        if(null != map.get(userId)){
          map.remove(userId)//标签已存在，不做处理
        }
      }else{
        map.put(userId, 'N')//移除标签
      }
    })

    //2、打标签
    println("start to tag .......................................................")
    val iter = map.keySet().iterator()
    while(iter.hasNext){
      println(iter.next())
    }
  }

}
