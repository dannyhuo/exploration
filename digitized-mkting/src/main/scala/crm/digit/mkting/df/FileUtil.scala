package crm.digit.mkting.df

import java.io.{FileInputStream, ObjectInputStream, ObjectOutputStream}
import java.util.HashMap

import crm.digit.mkting.MemQuery.fs
import org.apache.hadoop.fs.{FSDataInputStream, FSDataOutputStream, FileSystem, Path}

/**
  * Created by huoqiang on 19/7/2017.
  */
object FileUtil {

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

}
