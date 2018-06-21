import java.io.{FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}

import scala.io.Source
import java.util.HashMap

import org.apache.spark.SparkUserAppException
import org.apache.spark.sql.AnalysisException

/**
  * Created by huoqiang on 14/7/2017.
  */
object ScalaTest {

  private val NAME_REG_EXP = "[[a-z]|[A-Z]|_|$]+\\d*[ ]*\\(*[ ]*[[a-z]|[A-Z]|_|$]+\\d*\\.?[[[a-z]|[A-Z]|_|$]*[[a-z]|[A-Z]|_|$|\\d]*][ ]*\\)*[ ]*"


  val char = "\\(*[A-Za-z_$]+[A-Za-z0-9_$]*[ ]*\\(*[ ]*[A-Za-z_$]+[A-Za-z0-9_$]*\\.?[A-Za-z_$]+[A-Za-z0-9_$]*[ ]*\\)*[ ]*"

  var regExp_char = "[A-Za-z_$]*"
  var regExp_name = "[A-Za-z0-9_$]*"
  var lft = "\\(?"
  val exp = "\\(*[a-zA-Z_$]+[a-zA-Z0-9_$]*[ ]*\\([ ]*[a-zA-Z_$]+[a-zA-Z0-9_$]*\\.?[a-zA-Z_$]+[a-zA-Z0-9_$]*[ ]*\\)[ ]*"


  private val IS_HAVE_SUB_QUERY_REGEXP_STR = ".+\\([ ]*select .+ from .+\\).*"
  private val SUB_QUERY_REGEXP = "\\([ ]*select .+ from ([a-zA-Z0-9_$[ ]\\.]*|[\\([ ]*[^select][a-zA-Z0-9\\._$]+[ ]*\\)])[ ]*\\)".r
  private val SUB_QUERY_REPLACE_FLAG = "-"

  /**
    * 递归查找并分解子查询
    * @param querySql
    */
  private def findSubQueryAndSplit(querySql: String): Unit = {
    if(querySql.matches(IS_HAVE_SUB_QUERY_REGEXP_STR)){
      var handedSql = querySql
      SUB_QUERY_REGEXP.findAllIn(querySql).foreach(s=>{
        val subSql = s.substring(s.indexOf("(")+1, s.lastIndexOf(")"))
        handedSql = handedSql.replace(s, SUB_QUERY_REPLACE_FLAG)
        findSubQueryAndSplit(subSql)
      })
      //分解原sql
      println(handedSql)
    }else{
      //解析子查询
      println(querySql)
    }
  }

  def splitSubSql(sql : String) : Unit = {
    if(sql.matches(IS_HAVE_SUB_QUERY_REGEXP_STR)) {
      val s = sql.toLowerCase().trim
      val len = s.length
      val subSql = new StringBuffer()
      var flag = false
      var left = 0
      var right = 0
      for(i <- 0 until len) {
        val c = s.charAt(i)
        if (c == '(') {
          if(s.substring(i+1, len).trim.startsWith("select")){
            flag = true
          }
          if(flag){
            left = left + 1
            if(left > 1){
              subSql.append(c)
            }
          }
        } else if (flag) {
          if(c == ')'){
            right = right + 1
          }
          if (left == right) {
            flag = false
            val resSubSql = subSql.toString
            splitSubSql(resSubSql.trim)
            splitSubSql(sql.replace("(" + resSubSql + ")", SUB_QUERY_REPLACE_FLAG).trim)
          }else{
            subSql.append(c)
          }
        }
      }
    }else{
     println(sql)
    }
  }

  def main(args: Array[String]): Unit = {

    /*var NAME_REG_EXP = "\\(*[ ]*([a-zA-Z_$]+[a-zA-Z0-9_$]*(\\.[a-zA-Z_$]+[a-zA-Z0-9_$]*|[a-zA-Z0-9_$]*))[ ]*\\)*"
    var _NAME_REG_EXP = "\\(*[ ]*[a-zA-Z_$]+[a-zA-Z0-9_$]*[ ]*\\([ ]*([a-zA-Z_$]+[a-zA-Z0-9_$]*(\\.[a-zA-Z_$]+[a-zA-Z0-9_$]*|[a-zA-Z0-9_$]*))[ ]*\\)[ ]*\\)*"

    println("(AA23".matches(_NAME_REG_EXP))*/

    var sql = "SELECT DISTINCT rtn.user_id from (    select uup.user_id user_id from (    SELECT u.user_id user_id from user_user u,user_portrait up where u.user_id=up.user_id and up.gender IN ('F') and up.mobile_number IS NOT NULL     ) uup,user_action_collection uac where uup.user_id=uac.user_id and  uac.action IN ('LOGIN')) rtn,ord_order oo where avg(rtn.user_id)=oo.user_id and oo.create_time>'2015-07-16 10:00:39'"

    /*"\\([ ]*select .+ from .+select{0}.+\\)".r.findAllIn(sql.toLowerCase()).foreach(u=>println(u))//.findAllIn(sql.toLowerCase()).foreach(u => println(u))
    /*println(sql.lastIndexOf("\\([ ]*select"))
    findSubQueryAndSplit(sql.toLowerCase())*/*/

    splitSubSql(sql.toLowerCase())

    Source.fromFile("F:\\models", "utf-8").getLines().foreach(l => println(l))

    //序列化对象测试
    var map = new HashMap[String, String]
    map.put("1001", "a")
    map.put("1002", "b")
    map.put("1003", "c")
    map.put("1004", "select * from user_user where user_id = 1001 and user_name = 'abc'")
    val fos = new FileOutputStream("f:\\map.serialized")
    val oo = new ObjectOutputStream(fos)
    oo.writeObject(map)
    oo.close()
    fos.close()

    val fis = new FileInputStream("f:\\map.serialized")
    val oi = new ObjectInputStream(fis)
    val map2 = oi.readObject().asInstanceOf[HashMap[String, String]]
    oi.close()
    fis.close()

    val iter = map2.keySet().iterator()
    while(iter.hasNext){
      val key = iter.next()
      println(key + ": " + map2.get(key))
    }

  }


}
