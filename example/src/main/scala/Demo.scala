import com.acjay.tagtypetemplater._

object Demo {
  @tagged("String") type MyString

  def main(args: Array[String]) {
    val regular = "Hello, world!"
    val taggedString = MyString.fromString(regular)
    val backToRegular = taggedString.untagged

    println(taggedString)
  }
}