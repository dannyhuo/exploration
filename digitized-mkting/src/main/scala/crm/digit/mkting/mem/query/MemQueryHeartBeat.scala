package crm.digit.mkting.mem.query

import java.text.SimpleDateFormat
import java.util.Date

import crm.digit.mkting.df.DfsUtil
import org.apache.hadoop.fs.{FileSystem, Path}

/**
  * Created by huoqiang on 24/7/2017.
  */
class MemQueryHeartBeat(fs : FileSystem, heartBeatPath : Path, heartBeatInterval : Int)  extends Runnable{

  private val dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

  override def run(): Unit = {
    while(true){
      DfsUtil.write(fs, heartBeatPath, getNow(), true)
      Thread.sleep(heartBeatInterval)
    }
  }

  def getNow() : String = {
    dateFormat.format(new Date())
  }

}
