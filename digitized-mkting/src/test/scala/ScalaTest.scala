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



  def main(args: Array[String]): Unit = {

    var NAME_REG_EXP = "\\(*[ ]*([a-zA-Z_$]+[a-zA-Z0-9_$]*(\\.[a-zA-Z_$]+[a-zA-Z0-9_$]*|[a-zA-Z0-9_$]*))[ ]*\\)*"
    var _NAME_REG_EXP = "\\(*[ ]*[a-zA-Z_$]+[a-zA-Z0-9_$]*[ ]*\\([ ]*([a-zA-Z_$]+[a-zA-Z0-9_$]*(\\.[a-zA-Z_$]+[a-zA-Z0-9_$]*|[a-zA-Z0-9_$]*))[ ]*\\)[ ]*\\)*"

    println("(AA23".matches(_NAME_REG_EXP))
  }

}
