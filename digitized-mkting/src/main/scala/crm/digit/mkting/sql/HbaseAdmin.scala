package crm.digit.mkting.sql

import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory}
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.util.Bytes

/**
  * Created by huoqiang on 19/7/2017.
  */
object HbaseAdmin {

  def getHbase(connect : Connection) : Unit = {
    val admin = connect.getAdmin

    val tables = admin.listTableNames()

    tables.foreach(t => {
      println("table => " + Bytes.toString(t.getName))
    })

    val tableName = TableName.valueOf("user_user")
    val descriptor = admin.getTableDescriptor(tableName)
    val columnFamilies = descriptor.getColumnFamilies
    val familes = descriptor.getFamilies
    val iter2 = familes.iterator()
    while(iter2.hasNext){
      val fam = iter2.next()
      println("iter2.next().getNameAsString => " + fam.getNameAsString)
    }

    columnFamilies.foreach( c => {
      val name = Bytes.toString(c.getName);
      println("column => " + name)
      val config = c.getConfiguration
      val iter = config.keySet().iterator()
      while(iter.hasNext){
        val key = iter.next()
        val v = config.get(key)
        println("c key => " + key + ", c value => " + v)
      }
    })

    val configuration = descriptor.getConfiguration
    val iter = configuration.keySet().iterator()
    while(iter.hasNext){
      val key = iter.next()
      val v = configuration.get(key)
      println("key => " + key + ", value => " + v)
    }


  }


  def main(args: Array[String]): Unit = {
    val conf = HBaseConfiguration.create
    conf.addResource("E\\git\\exploration\\digitized-mkting\\target\\hbase-site.xml")
    //conf.set("hbase.zookeeper.property.clientPort", "2181")
    //conf.set("zookeeper.znode.parent", "/hbase-unsecure")
    conf.set("hbase.zookeeper.quorum", "crm-master2,crm-slave1,crm-slave2")
    // conf.set("hbase.zookeeper.quorum", "hadoop1.snnu.edu.cn,hadoop3.snnu.edu.cn")

    //conf.set(TableInputFormat.INPUT_TABLE, tablename)

    val conn = ConnectionFactory.createConnection(conf)

    getHbase(conn)

    conn.close()
  }

}
