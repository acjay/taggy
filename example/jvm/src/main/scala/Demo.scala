import model.Model.MyString

object Demo {
  def main(args: Array[String]): Unit = {
    val regular: String = "Hello"
    val taggedString: MyString = MyString.fromString(regular)
    val modifiedTaggedString: MyString = taggedString.modify(_ + ", World!")
    val backToRegular: String = modifiedTaggedString.untagged
    println(backToRegular)
  }
}