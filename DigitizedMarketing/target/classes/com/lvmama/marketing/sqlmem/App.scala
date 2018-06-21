package com.lvmama.marketing.sqlmem

import com.lvmama.marketing.sqlmem.constant.Constant
import com.lvmama.marketing.sqlmem.model.SqlMemHeart
import com.lvmama.marketing.sqlmem.tasks.SqlMemHeartBeat
import com.lvmama.marketing.sqlmem.util.DfsUtil

/**
  * Created by huoqiang on 27/7/2017.
  */
object App {

  private val confFile = "local/heart.properties"

  def main(args: Array[String]): Unit = {
    //1、判断任务是否是启动状态
    val heart = new SqlMemHeart(confFile)
    if(heart.isRunning()){
      throw new RuntimeException("It's running, doesn't run it repeat.")
    }
    println("=================================================================")
    println(Constant.sparkMemQueryFlag)

    //2、创建守护进程文件，删除后将结束进程
    println("Create the daemon flag file.")
    DfsUtil.touchEmpty(heart.fs, heart.processHeart)

    //3、启动心跳线程
    println("Start "+heart.appName+" heart beat thread.")
    val heartBeat = new SqlMemHeartBeat(heart.fs, heart.processHeartBeat, heart.heartBeatInterval)
    val t = new Thread(heartBeat)
    t.start()

    //4、启动sqlMemory
    println("Instance sql memory task and start it.")
    val sqlMemory = new SqlMemQuery(heart)
    sqlMemory.start(heart.startUpMOdel, heart.appName)
  }

}
