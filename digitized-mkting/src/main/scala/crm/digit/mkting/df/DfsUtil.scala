package crm.digit.mkting.df

import java.io._
import org.apache.hadoop.fs.{FSDataInputStream, FSDataOutputStream, FileSystem, Path}

/**
  * Created by huoqiang on 19/7/2017.
  */
object DfsUtil {

  def objectSerialize[T](fs : FileSystem, obj : T, dfsPath : Path) : Unit = {
    var file : FSDataOutputStream = null
    var oos : ObjectOutputStream = null
    try{
      file = fs.create(dfsPath, true)
      oos = new ObjectOutputStream(file)
      oos.writeObject(obj)
    }finally{
      if(null != oos){
        oos.close()
      }

      if(null != file){
        file.close()
      }
    }
  }

  def objectSerialize[T](fs : FileSystem, obj : T, path : String) : Unit = {
    objectSerialize(fs, obj, new Path(path))
  }

  def deserializeObject[T](fs : FileSystem, dfsPath : Path) : T = {
    if(!fs.exists(dfsPath)){
      throw new Exception("file not found exception at path of '" + dfsPath.toString)
    }
    var file : FSDataInputStream = null
    var ois : ObjectInputStream = null
    try{
      file = fs.open(dfsPath)
      ois = new ObjectInputStream(file)
      ois.readObject().asInstanceOf[T]
    }finally{
      if(null != ois){
        ois.close()
      }
      if(null != file){
        file.close()
      }
    }
  }

  def deserializeObject[T](fs : FileSystem, path : String) : T = {
    deserializeObject(fs, new Path(path))
  }

  /**
    * 向HDFS写
    * @param fs
    * @param address
    * @param content
    * @param overwrite
    * @param charset
    */
  def write(fs : FileSystem, address : Path, content: String, overwrite : Boolean, charset : String) : Unit = {
    var writer: OutputStreamWriter = null
    var file : FSDataOutputStream = null
    var buffer : BufferedWriter = null
    try{
      file = fs.create(address, overwrite)
      writer = new OutputStreamWriter(file, charset)
      buffer = new BufferedWriter(writer)
      buffer.write(content)
      buffer.flush()
    }finally{
      if(null != buffer){
        buffer.close()
      }
      if(null != writer){
        writer.close()
      }
      if(null != file){
        file.close()
      }
    }
  }

  /**
    * 向HDFS写
    * @param fs
    * @param address
    * @param content
    * @param overwrite
    */
  def write(fs : FileSystem, address : Path, content: String, overwrite : Boolean) : Unit = {
    write(fs, address, content, overwrite, "utf-8")
  }

  /**
    * 向HDFS写
    * @param fs
    * @param address
    * @param content
    * @param overwrite
    */
  def write(fs : FileSystem, address : String, content: String, overwrite : Boolean) : Unit = {
    write(fs, new Path(address), content, overwrite, "utf-8")
  }

  /**
    * 向HDFS写
    * @param fs
    * @param address
    * @param content
    */
  def write(fs : FileSystem, address : Path, content: String) : Unit = {
    write(fs, address, content, true, "utf-8")
  }

  /**
    * 向HDFS写
    * @param fs
    * @param address
    * @param content
    */
  def write(fs : FileSystem, address : String, content: String) : Unit = {
    write(fs, new Path(address), content, true, "utf-8")
  }



  /**
    * 向HDFS写
    * @param fs
    * @param address
    * @param content
    * @param overwrite
    * @param charset
    */
  def write(fs : FileSystem, address : Path, content: Seq[String], overwrite : Boolean, charset : String) : Unit = {
    var writer: OutputStreamWriter = null
    var file : FSDataOutputStream = null
    var buffer : BufferedWriter = null
    try{
      file = fs.create(address, overwrite)
      writer = new OutputStreamWriter(file, charset)
      buffer = new BufferedWriter(writer)
      content.foreach( c => {
        buffer.write(c)
        buffer.newLine()
      })
      buffer.flush()
    }finally{
      if(null != buffer){
        buffer.close()
      }
      if(null != writer){
        writer.close()
      }
      if(null != file){
        file.close()
      }
    }
  }

  /**
    * 向HDFS写
    * @param fs
    * @param address
    * @param content
    * @param overwrite
    */
  def write(fs : FileSystem, address : Path, content: Seq[String], overwrite : Boolean) : Unit = {
    write(fs, address, content, overwrite, "utf-8")
  }

  /**
    * 向HDFS写
    * @param fs
    * @param address
    * @param content
    * @param overwrite
    */
  def write(fs : FileSystem, address : String, content: Seq[String], overwrite : Boolean) : Unit = {
    write(fs, new Path(address), content, overwrite, "utf-8")
  }

  /**
    * 向HDFS写
    * @param fs
    * @param address
    * @param content
    */
  def write(fs : FileSystem, address : Path, content: Seq[String]) : Unit = {
    write(fs, address, content, true, "utf-8")
  }

  /**
    * 向HDFS写
    * @param fs
    * @param address
    * @param content
    */
  def write(fs : FileSystem, address : String, content: Seq[String]) : Unit = {
    write(fs, new Path(address), content, true, "utf-8")
  }


  /**
    * 向HDFS写
    * @param fs
    * @param address
    * @param content
    * @param overwrite
    * @param charset
    */
  def write(fs : FileSystem, address : Path, content: List[String], overwrite : Boolean, charset : String) : Unit = {
    var writer: OutputStreamWriter = null
    var file : FSDataOutputStream = null
    var buffer : BufferedWriter = null
    try{
      file = fs.create(address, overwrite)
      writer = new OutputStreamWriter(file, charset)
      buffer = new BufferedWriter(writer)
      content.foreach( c => {
        buffer.write(c)
        buffer.newLine()
      })
      buffer.flush()
    }finally{
      if(null != buffer){
        buffer.close()
      }
      if(null != writer){
        writer.close()
      }
      if(null != file){
        file.close()
      }
    }
  }

  /**
    * 向HDFS写
    * @param fs
    * @param address
    * @param content
    * @param overwrite
    */
  def write(fs : FileSystem, address : Path, content: List[String], overwrite : Boolean) : Unit = {
    write(fs, address, content, overwrite, "utf-8")
  }

  /**
    * 向HDFS写
    * @param fs
    * @param address
    * @param content
    * @param overwrite
    */
  def write(fs : FileSystem, address : String, content: List[String], overwrite : Boolean) : Unit = {
    write(fs, new Path(address), content, overwrite, "utf-8")
  }

  /**
    * 向HDFS写
    * @param fs
    * @param address
    * @param content
    */
  def write(fs : FileSystem, address : Path, content: List[String]) : Unit = {
    write(fs, address, content, true, "utf-8")
  }

  /**
    * 向HDFS写
    * @param fs
    * @param address
    * @param content
    */
  def write(fs : FileSystem, address : String, content: List[String]) : Unit = {
    write(fs, new Path(address), content, true, "utf-8")
  }


  /**
    * 向HDFS写
    * @param fs
    * @param address
    * @param content
    * @param overwrite
    * @param charset
    */
  def write(fs : FileSystem, address : Path, content: java.util.List[String], overwrite : Boolean, charset : String) : Unit = {
    var writer: OutputStreamWriter = null
    var file : FSDataOutputStream = null
    var buffer : BufferedWriter = null
    try{
      file = fs.create(address, overwrite)
      writer = new OutputStreamWriter(file, charset)
      buffer = new BufferedWriter(writer)
      val len = content.size()
      for(i <- 0 until len){
        buffer.write(content.get(i))
        buffer.newLine()
      }
      buffer.flush()
    }finally{
      if(null != buffer){
        buffer.close()
      }
      if(null != writer){
        writer.close()
      }
      if(null != file){
        file.close()
      }
    }
  }

  /**
    * 向HDFS写
    * @param fs
    * @param address
    * @param content
    * @param overwrite
    */
  def write(fs : FileSystem, address : Path, content: java.util.List[String], overwrite : Boolean) : Unit = {
    write(fs, address, content, overwrite, "utf-8")
  }

  /**
    * 向HDFS写
    * @param fs
    * @param address
    * @param content
    * @param overwrite
    */
  def write(fs : FileSystem, address : String, content: java.util.List[String], overwrite : Boolean) : Unit = {
    write(fs, new Path(address), content, overwrite, "utf-8")
  }

  /**
    * 向HDFS写
    * @param fs
    * @param address
    * @param content
    */
  def write(fs : FileSystem, address : Path, content: java.util.List[String]) : Unit = {
    write(fs, address, content, true, "utf-8")
  }

  /**
    * 向HDFS写
    * @param fs
    * @param address
    * @param content
    */
  def write(fs : FileSystem, address : String, content: java.util.List[String]) : Unit = {
    write(fs, new Path(address), content, true, "utf-8")
  }


  def readTxt(fs : FileSystem, address : Path, charset: String) : Seq[String] = {
    var reader: InputStreamReader = null
    var buffer : BufferedReader = null
    var file : FSDataInputStream = null
    try{
      file = fs.open(address)
      reader = new InputStreamReader(file, charset)
      buffer = new BufferedReader(reader)
      var line : String = null
      var txt = Seq.empty[String]
      while(null != (line = buffer.readLine())){
        txt = txt.:+(line)
      }
      return txt
    }finally{
      if(null != buffer){
        buffer.close()
      }
      if(null != reader){
        reader.close()
      }
      if(null != file){
        file.close()
      }
    }
  }

  def readTxt(fs : FileSystem, address : String, charset: String) : Seq[String] = {
    readTxt(fs, new Path(address), charset)
  }

  def readTxt(fs : FileSystem, address : String) : Seq[String] = {
    readTxt(fs, new Path(address), "utf-8")
  }

  def readTxt(fs : FileSystem, address : Path) : Seq[String] = {
    readTxt(fs, address, "utf-8")
  }



  /**
    * 读取文本文件的第一行
    * @param fs
    * @param address
    * @param charset
    * @return
    */
  def readLine(fs : FileSystem, address : Path, charset: String) : String = {
    var reader: InputStreamReader = null
    var buffer : BufferedReader = null
    var file : FSDataInputStream = null
    try{
      file = fs.open(address)
      reader = new InputStreamReader(file, charset)
      buffer = new BufferedReader(reader)
      buffer.readLine()
    }finally{
      if(null != buffer){
        buffer.close()
      }
      if(null != reader){
        reader.close()
      }
      if(null != file){
        file.close()
      }
    }
  }

  def readLine(fs : FileSystem, address : Path) : String = {
    readLine(fs, address, "utf-8")
  }

  def readLine(fs : FileSystem, address : String, charset: String) : String = {
    readLine(fs, new Path(address), charset)
  }

  def readLine(fs : FileSystem, address : String) : String = {
    readLine(fs, new Path(address), "utf-8")
  }

  /**
    * 创建个一个空文件
    * @param fs
    * @param address
    * @return
    */
  def touchEmpty(fs : FileSystem, address : Path) : Boolean = {
    var file : FSDataOutputStream = null
    try{
      file = fs.create(address, true)
      return true
    }finally{
      file.close()
    }
    return false
  }


  /**
    * 删除文件
    * @param fs
    * @param address
    */
  def remove(fs : FileSystem, address : Path) : Boolean = {
    if(fs.exists(address)){
      return fs.delete(address, true)
    }
    return false
  }

  /**
    * 删除文件
    * @param fs
    * @param address
    */
  def remove(fs : FileSystem, address : String) : Boolean = {
    remove(fs, new Path(address))
  }

}
