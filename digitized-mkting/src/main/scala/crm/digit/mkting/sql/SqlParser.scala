package crm.digit.mkting.sql

import java.util.{HashMap, HashSet, Map, Set}

//import crm.digit.mkting.sql.HbaseDataType
import org.apache.spark.sql.types.{StructType, _}

import scala.xml.XML


/**
  * Created by huoqiang on 10/7/2017.
  */
class SqlParser(sqles : Seq[String], tableConfig : String) {

  def this(sql : String){
    this(Seq.empty[String].:+(sql), "/home/hadoop/upload/conf/table_configs.xml")
  }

  def this(sqles : Seq[String]){
    this(sqles, "/home/hadoop/upload/conf/table_configs.xml")
  }

  private val sqlTextes : Seq[String] = sqles
  private val SELECT = "select "
  private val FROM = " from "
  private val DISTINCT = "distinct "
  private val SQL_KEYS = "select | from | on | where | group by | having | order by "
  private val LOGIC_OPERATOR = " and | or |,"
  private val TABLE_RELATION = " left join | right join | inner join |,"
  private val COLUMN_VALUE_RELATION = "=|>|>=|<|<=|!=|<>| is[ ]+not | is | not[ ]+in| in"
  private val UNION = " union | union all "
  private val AS = " as "
  private val DESC = " desc"
  private val ASC = " asc"
  private val BLANK = " "
  private val BLANKS_SPLIT = "[ ]+"//用于以空格分割字符
  private val NULL_STRING = ""
  private val PROJECTS_RELATION = ","
  private val COLUMN_OPERATOR = "\\+|-|\\*|/"

  //名称校验表达式: tab.column or column
  private val NAME_REG_EXP = "\\(*[ ]*([a-zA-Z_$]+[a-zA-Z0-9_$]*(\\.[a-zA-Z_$]+[a-zA-Z0-9_$]*|[a-zA-Z0-9_$]*))[ ]*\\)*"
  //表使用函数校验: count(tab.column) or sum(column)
  private val NAME_IN_F_REG_EXP = "\\(*[ ]*[a-zA-Z_$]+[a-zA-Z0-9_$]*[ ]*\\([ ]*([a-zA-Z_$]+[a-zA-Z0-9_$]*(\\.[a-zA-Z_$]+[a-zA-Z0-9_$]*|[a-zA-Z0-9_$]*))[ ]*\\)[ ]*\\)*"


  private val IS_HAVE_SUB_QUERY_REGEXP_STR = ".+\\([ ]*select .+ from .+\\s*\\).*"
  //private val SUB_QUERY_REGEXP = "\\([ ]*select .+ from .+\\s*\\)".r
  private val SUB_QUERY_REPLACE_FLAG = "-"
  private var error_message = ""

  /**
    * sql中对应的拆分后的子句
    */
  private val clausesMap = new HashMap[String, Seq[String]]

  /**
    * 表名和别名对应关系
    * key : 表的别名
    * value: 表名
    */
  private val tabAliasFromSql : Map[String, String] = new HashMap[String, String]

  /**
    * sql中列取的别名
    */
  private val tmpColAlias : Map[String, String] = new HashMap[String, String]

  /**
    * sql中子查询起的临时表名
    */
  private val tmpTabAlias : Map[String, String] = new HashMap[String, String]

  /**
    * 对应到spark的表
    * key: tableName
    * value: spark schema
    */
  private val sparkSchemas : Map[String, (StructType, Set[String])] = new HashMap[String, (StructType, Set[String])]

  /**
    * 配置文件中所有已配置的列对应转换为的StructField,按表分
    * key: tableName
    * value_key: colName
    * value_value: StructField,family:column
    * 按表进行分组
    */
  private val sqlRelatedAllTableStructFields : Map[String, Map[String, (StructField, String)]] = new HashMap[String, Map[String, (StructField, String)]]

  /**
    * 当前sql中表的所有StructField
    * 按表进行分组
    * key: tableName
    * value_key: colName
    * value_value: StructField
    */
  private val sqlRelatedStructFieldsByTable : Map[String, Map[String, StructField]] = new HashMap[String, Map[String, StructField]]

  /**
    * 列及列簇信息
    * key: tableName
    * value: Seq[family:column]
    */
  private val sqlRelatedColumnsByTable : Map[String, Set[String]] = new HashMap[String, Set[String]]

  //初始配置
  this.initConfig()

  //调用主入口
  this.parse()

  /**
    * 获取spark schema
    * @return
    */
  def getSparkSchemas : Map[String, (StructType, Set[String])] = {
    val iter = sqlRelatedStructFieldsByTable.keySet().iterator()
    while(iter.hasNext){
      val tabName = iter.next()
      val colsMap = sqlRelatedStructFieldsByTable.get(tabName)
      val colsIter = colsMap.values().iterator()
      var structFields : Seq[StructField] = Seq.empty[StructField]
      while(colsIter.hasNext){
        structFields = structFields.:+(colsIter.next())
      }
      val tabSchema = StructType(structFields)
      sparkSchemas.put(tabName, (tabSchema, sqlRelatedColumnsByTable.get(tabName)))
    }
    sparkSchemas
  }

  /**
    * 主入口函数
    */
  private def parse() : Unit = {
    //1、分解sql
    sqlTextes.foreach(sql => {
      sql.toLowerCase.split(UNION).foreach(sql => {
        //解析前去除无用关键字，如：distinct, as(用空格代替), desc, asc
        val tSql = sql.trim.replaceAll(DISTINCT, NULL_STRING).replaceAll(AS, BLANK)
          .replaceAll(DESC, NULL_STRING).replaceAll(ASC, NULL_STRING)
        findSubQueryAndSplit(tSql)
      })
    })

    //2、解析from子句(解析sql中有哪些表)
    clausesMap.get(FROM).foreach(f => {
      println("......................parse from clause " + f)
      parseFromClause(f.trim)
    })

    //3、解析project子句
    clausesMap.get(SELECT).foreach(f => {
      println("......................parse project clause " + f)
      parseProjectsClause(f.trim)
    })

    //4、解析其它子句
    val clauseIter = clausesMap.keySet().iterator()
    while(clauseIter.hasNext){
      val key = clauseIter.next()
      if(key != FROM && key != SELECT){
        clausesMap.get(key).foreach( c => {
          println("......................parse other clause " + c)
          parseOtherClause(c)
        })
      }
    }
  }

  /*private def findSubQueryAndSplit(querySql: String): Unit = {
    if(querySql.matches(IS_HAVE_SUB_QUERY_REGEXP_STR)){
      var handedSql = querySql
      SUB_QUERY_REGEXP.findAllIn(querySql).foreach(s=>{
        val subSql = s.substring(s.indexOf("(")+1, s.lastIndexOf(")"))
        handedSql = handedSql.replace(s, SUB_QUERY_REPLACE_FLAG)
        findSubQueryAndSplit(subSql)
      })
      //分解原sql
      subQuerySqlSplit(handedSql)
    }else{
      //解析子查询
      subQuerySqlSplit(querySql)
    }
  }*/

  /**
    * 递归查找并分解子查询
    * @param querySql
    */
  def findSubQueryAndSplit(querySql : String) : Unit = {
    if(querySql.matches(IS_HAVE_SUB_QUERY_REGEXP_STR)) {
      val s = querySql.toLowerCase().trim
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
            findSubQueryAndSplit(resSubSql.trim)
            findSubQueryAndSplit(querySql.replace("(" + resSubSql + ")", SUB_QUERY_REPLACE_FLAG).trim)
          }else{
            subSql.append(c)
          }
        }
      }
    }else{
      subQuerySqlSplit(querySql)
    }
  }

  /**
    * 分解sql子句
    * @param sql
    */
  private def subQuerySqlSplit(sql : String) : Unit = {
    val clauses = sql.trim.split(SQL_KEYS)
    if(clauses.size < 3){
      error_message = "sql syntax error : " + sql
      throw new RuntimeException(error_message)
      //sql_text_error = true
      //return
    }

    //2、拆分sql子句
    val indexMap = new HashMap[String, Int]
    SQL_KEYS.split("\\|").foreach(key => {
      val index = sql.indexOf(key)
      if(index != -1){
        indexMap.put(key, index)
      }
    })

    //3、对子句子进行划分，分类
    var i: Int = 0
    while(indexMap.size() > 0) {
      //找出某类子句
      var minIndex = sql.length
      var minKey = ""
      val iter = indexMap.keySet().iterator()
      while (iter.hasNext) {
        val key = iter.next()
        val value = indexMap.get(key)
        if (value < minIndex) {
          minIndex = value
          minKey = key
        }
      }

      //子句归类
      i = i + 1
      var tmpSeq = clausesMap.get(minKey)
      if(null == tmpSeq){
        tmpSeq = Seq.empty[String]
      }
      tmpSeq = tmpSeq.:+(clauses(i).trim)
      clausesMap.put(minKey, tmpSeq)

      //已归类的子句进行移除
      indexMap.remove(minKey)
    }
  }


  /**
    * 解析from子句中的表
    * @param fromClause
    */
  private def parseFromClause(fromClause : String) : Unit = {
    fromClause.split(TABLE_RELATION).foreach(t => {
      val kv = t.trim.split(BLANKS_SPLIT)

      if(kv(0).trim != SUB_QUERY_REPLACE_FLAG && !sqlRelatedAllTableStructFields.containsKey(kv(0).trim)){
        //TODO
        error_message = "The table '" + kv(0).trim + "' not found in the config file of " + tableConfig
        throw new RuntimeException(error_message)
        //sql_text_error = true
        //return
      }

      if(kv.length == 2){
        if(kv(0).trim == SUB_QUERY_REPLACE_FLAG){
          //子查询中的临时表
          tmpTabAlias.put(kv(1).trim, SUB_QUERY_REPLACE_FLAG)
        }else{
          tabAliasFromSql.put(kv(1).trim, kv(0).trim)
          println("========================table name of '" + kv(0) + "' will put to the map, the key is '" + kv(1) + "'")
        }
      }else if(kv.length == 1){
        if(kv(0).trim != SUB_QUERY_REPLACE_FLAG){
          tabAliasFromSql.put(kv(0).trim, kv(0).trim)
          println("======================table name of '" + kv(0) + "' will put to the map, the key is '" + kv(0) + "'")
        }
      }else{
        //TODO
        error_message = "Syntax found error near '" + t + "'."
        throw new RuntimeException(error_message)
        //sql_text_error = true
        //return
      }
    })
  }

  /**
    *
    * @param clause
    */
  private def parseProjectsClause(clause : String): Unit ={
    val clauseTrimed = clause.trim
    //*表示查询全部
    if(clauseTrimed == "*"){
      val alerIter = tabAliasFromSql.keySet().iterator()
      while(alerIter.hasNext){
        val tabName = tabAliasFromSql.get(alerIter.next())
        val tabColMapFrmConf = sqlRelatedAllTableStructFields.get(tabName)

        var srsfbtMap = sqlRelatedStructFieldsByTable.get(tabName)
        if(null == srsfbtMap){
          srsfbtMap = new HashMap[String, StructField]
          sqlRelatedStructFieldsByTable.put(tabName, srsfbtMap)
        }

        var srcbtSet = sqlRelatedColumnsByTable.get(tabName)
        if(null == srcbtSet){
          srcbtSet = new HashSet[String]
          sqlRelatedColumnsByTable.put(tabName, srcbtSet)
        }

        val sratsIter = tabColMapFrmConf.keySet().iterator()
        while(sratsIter.hasNext){
          val colName = sratsIter.next()
          val tmpTuple = tabColMapFrmConf.get(colName)
          srsfbtMap.put(colName, tmpTuple._1)
          srcbtSet.add(tmpTuple._2)
        }
      }
      return
    }

    clauseTrimed.split(PROJECTS_RELATION).foreach(c => {
      c.split(COLUMN_OPERATOR).foreach(u => {
        val trimedu = u.trim
        val colPair = trimedu.split(BLANKS_SPLIT)
        val column = extractColumn(colPair(0))
        if(null == column){
          error_message = "Syntax error near sql of '" + c + "'."
          throw new RuntimeException(error_message)
        }
        if(colPair.length == 2){//对列取别名了
          tmpColAlias.put(colPair(1).trim, column)
          parseColumn(column)
        }else if(colPair.length == 1){//无别名
          parseColumn(column)
        }else{//错误
          //TODO
          error_message = "Syntax error near sql of '" + c + "'."
          throw new RuntimeException(error_message)
          //sql_text_error = true
          //return
        }
      })
    })
  }

  /**
    * 解析project子句中的列
    * @param clause
    */
  private def parseOtherClause(clause : String) : Unit = {
    if(null == clause){
      return
    }
    val c = clause.trim

    c.split(LOGIC_OPERATOR).foreach(t => {
      val v = t.trim
      v.split(COLUMN_VALUE_RELATION).foreach(x => {
        x.split(COLUMN_OPERATOR).foreach(u => {
          val exactColumn = extractColumn(u)
          if(null != exactColumn){
            parseColumn(exactColumn)
          }
        })
      })
    })
  }

  /**
    * 抽取出列
    * 如 sum(column)中取出column
    * @param roughColumn
    * @return
    */
  private def extractColumn(roughColumn : String): String ={
    val l = roughColumn.trim
    if(l == "null"){
      return null
    }else if(l.matches(NAME_IN_F_REG_EXP)){//列中使用了函数
      val rBracketIdx = l.indexOf(")")
      var flag = false
      var buffer : StringBuffer = null
      for(i <- 0 until rBracketIdx){
        if(l.charAt(i) == '('){
          buffer = new StringBuffer()
          flag = true
        }else if(l.charAt(i) == ')'){
          flag = false
        }else if(flag){
          buffer.append(l.charAt(i))
        }
      }
      return buffer.toString.trim
    }else if(l.matches(NAME_REG_EXP)){
      if(l.indexOf("(") != -1){//只存在左括号,条件中and or用了括号
        return l.replaceAll("\\(", NULL_STRING).trim
      }else if(l.indexOf(")") != -1){//只存在右括号,条件中and or用了括号
        return l.replaceAll("\\)", NULL_STRING).trim
      }else{//无括号，直接解析
        return l
      }
    }else{
      println("......................................without parse '" + roughColumn + "'.")
      return null
    }

    /*if(l.matches(NAME_REG_EXP) && l != "null"){
      val lBracketIdx = l.indexOf("(")
      val rBracketIdx = l.indexOf(")")
      //有左右括号，说明使用了函数
      if(lBracketIdx != -1 && rBracketIdx != -1){
        var flag = false
        var buffer : StringBuffer = null
        for(i <- 0 until rBracketIdx){
          if(l.charAt(i) == '('){
            buffer = new StringBuffer()
            flag = true
          }else if(l.charAt(i) == ')'){
            flag = false
          }else if(flag){
            buffer.append(l.charAt(i))
          }
        }
        return buffer.toString.trim
      }else{
        if(lBracketIdx != -1){//只存在左括号,条件中and or用了括号
          return l.replaceAll("\\(", NULL_STRING).trim
        }else if(rBracketIdx != -1){//只存在右括号,条件中and or用了括号
          return l.replaceAll("\\)", NULL_STRING).trim
        }else{//无括号，直接解析
          return l
        }
      }
    }else{
      println("......................................without parse '" + roughColumn + "'.")
      return null
    }*/
  }


  /**
    * 解析列
    * @param exactCol
    */
  private def parseColumn(exactCol : String) : Unit = {
    val arr = exactCol.split("\\.")
    var colStructFieldTuple : (StructField, String) = null
    var colName : String = null
    var colTabName : String = null
    if(arr.length == 2){//列指定了表名或表的别名
      if(null != tmpTabAlias.get(arr(0))){
        //临时表中的列，不需重复解析列
        return
      }

      colTabName = tabAliasFromSql.get(arr(0))
      if(colTabName == null){
        //TODO
        error_message = "Alias of '" + arr(0) + "' is undefined in sql near '" + exactCol + "'."
        throw new RuntimeException(error_message)
        //sql_text_error = true
        //return
      }

      val tabColsMap = sqlRelatedAllTableStructFields.get(colTabName)
      if(null == tabColsMap){
        //TODO
        error_message = "The table name of '" + colTabName + "' is not exist on config xml of " + tableConfig
        throw new RuntimeException(error_message)
        //sql_text_error = true
        //return
      }

      colStructFieldTuple = tabColsMap.get(arr(1))
      if(null == colStructFieldTuple){
        //TODO
        error_message = "The column of '" + arr(1) + "' is not available, you should config it in config file of " + tableConfig + " !"
        throw new RuntimeException(error_message)
        //sql_text_error = true
        //return
      }
      colName = arr(1)
    }else if(arr.length == 1){//不带别名
      colName = arr(0)
      val aliasTableIterator = tabAliasFromSql.values().iterator()
      while(aliasTableIterator.hasNext){
        val curTabName = aliasTableIterator.next()
        val curTableColsMap = sqlRelatedAllTableStructFields.get(curTabName)
        if(null == curTableColsMap){
          //TODO
          error_message = "The table name of '" + curTabName + "' is not exist on config xml of " + tableConfig
          throw new RuntimeException(error_message)
          //sql_text_error = true
          //return
        }
        val tmpColsStructField = curTableColsMap.get(arr(0))
        if(null != colStructFieldTuple && null != tmpColsStructField){
          //TODO
          error_message = "The column of '" + colName + "' is ambiguous, you can with tablename.column to point it at your sql."
          throw new RuntimeException(error_message)
          //sql_text_error = true
          //return
        }
        if(null != tmpColsStructField){
          colStructFieldTuple = tmpColsStructField
          colTabName = curTabName
        }
      }
    }else{//语法错
      //TODO
      error_message = "Found syntax near the sql of " + exactCol
      throw new RuntimeException(error_message)
      //sql_text_error = true
      //return
    }
    if(null == colName || null == colTabName || null == colStructFieldTuple){
      if(null != tmpColAlias.get(arr(0))){
        //查询列中取的别名，不需重复解析
        println(".......................not parse duplicate the column of " + arr(0))
        return
      }
      //TODO
      error_message = "Found unknown error for parse clause near sql of '" + exactCol + "'."
      throw new RuntimeException(error_message)
      //sql_text_error = true
      //return
    }
    var sqlColsMap : Map[String, StructField] = sqlRelatedStructFieldsByTable.get(colTabName)
    if(null == sqlColsMap){
      sqlColsMap = new HashMap[String, StructField]
      sqlRelatedStructFieldsByTable.put(colTabName, sqlColsMap)
    }
    sqlColsMap.put(colName, colStructFieldTuple._1)

    var sqlColsForHbaseSet : Set[String] = sqlRelatedColumnsByTable.get(colTabName)
    if(null == sqlColsForHbaseSet){
      sqlColsForHbaseSet = new HashSet[String]
      sqlRelatedColumnsByTable.put(colTabName, sqlColsForHbaseSet)
    }
    sqlColsForHbaseSet.add(colStructFieldTuple._2 + ":" + colName)
  }

  /**
    * 构造StructField
    * @param colName
    * @param dataType
    * @return
    */
  private def toStructField(colName:String, dataType:String) : StructField = {
    var structField : StructField = null
    if(dataType == HbaseDataType.BOOL){
      structField = StructField(colName, BooleanType, nullable = true)
    } else if(dataType == HbaseDataType.DATE){
      structField = StructField(colName, DateType, nullable = true)
    } else if(dataType == HbaseDataType.DECIMAL){
      structField = StructField(colName, new DecimalType, nullable = true)
    } else if(dataType == HbaseDataType.DOUBLE){
      structField = StructField(colName, DoubleType, nullable = true)
    } else if(dataType == HbaseDataType.FLOAT){
      structField = StructField(colName, FloatType, nullable = true)
    } else if(dataType == HbaseDataType.INT){
      structField = StructField(colName, IntegerType, nullable = true)
    } else if(dataType == HbaseDataType.LONG){
      structField = StructField(colName, LongType, nullable = true)
    } else if(dataType == HbaseDataType.SHORT){
      structField = StructField(colName, ShortType, nullable = true)
    } else if(dataType == HbaseDataType.STRING){
      structField = StructField(colName, StringType, nullable = true)
    }else{
      structField = StructField(colName, StringType, nullable = true)
    }
    structField
  }

  /**
    * 初始化表配置
    */
  private def initConfig() : Unit={
    val xmlFile = XML.loadFile(tableConfig)
    val tableNodes = xmlFile \ "table"
    tableNodes.foreach(tableNode => {
      val tabName = (tableNode \ "tableName").text
      val cols = tableNode \ "col"

      val tabStructMap = new HashMap[String, (StructField, String)]
      cols.foreach( colNode => {
        val name = (colNode \ "name").text.toLowerCase()
        val family = (colNode \ "family").text.toLowerCase()
        val dataType = (colNode \ "dataType").text.toLowerCase()
        tabStructMap.put(name, (toStructField(name, dataType), family))
      })
      sqlRelatedAllTableStructFields.put(tabName, tabStructMap)
    })
  }

}
