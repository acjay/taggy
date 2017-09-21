import scala.scalajs.js
import model.Model.MyString

object Demo extends js.JSApp {
  def main(): Unit = {
    val regular: String = "Hello"
    val taggedString: MyString = MyString.fromString(regular)
    val modifiedTaggedString: MyString = taggedString.modify(_ + ", World!")
    val backToRegular: String = modifiedTaggedString.untagged
    println(backToRegular)
    
    val length = implicitly[HasLength[MyString]].length(modifiedTaggedString)
    println(s"length: $length")
  }
}