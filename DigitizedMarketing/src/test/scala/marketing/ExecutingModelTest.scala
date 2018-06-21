package marketing

import com.lvmama.marketing.sqlmem.model.{ExecutingModel, SqlMemHeart}
import com.lvmama.marketing.sqlmem.util.DfsUtil
/**
  * Created by huoqiang on 27/7/2017.
  */
object ExecutingModelTest {

  def main(args: Array[String]): Unit = {
    var model = "169|20170723|2|spark test1|select distinct user_portrait.user_id from user_portrait where  user_portrait.created_date>'2013-07-10 15:28:44'"
    model = "161|20170227|2||select distinct user_portrait.user_id from user_portrait,ord_order where  ord_order.category_id in ('28','19','29','32') and user_portrait.user_id = ord_order.user_id"
    val exeModel = new ExecutingModel(model, "/tmp")
    println(exeModel.isModelValid())

    val conf = new SqlMemHeart("local/heart.properties")
    DfsUtil.objectSerialize(conf, "D:\\Documents\\Downloads\\obj.ser")
  }

}
