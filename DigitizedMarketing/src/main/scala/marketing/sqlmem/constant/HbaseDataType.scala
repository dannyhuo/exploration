package marketing.sqlmem.constant

/**
  * Created by huoqiang on 7/7/2017.
  */
object HbaseDataType extends Enumeration{

  var BOOL = Value(0, "bool")
  var SHORT = Value(1, "short")
  var INT = Value(2, "int")
  var LONG = Value(3, "long")
  var FLOAT = Value(4, "float")
  var DOUBLE = Value(5, "double")
  var DECIMAL = Value(6, "decimal")
  var STRING = Value(7, "string")
  var DATE = Value(8, "date")

}
