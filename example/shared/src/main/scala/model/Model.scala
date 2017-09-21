package model

import com.acjay.taggy.tagged
import com.acjay.taggy.tag.@@

object Model {
  // Declared in a separate class to make sure importing the name also brings 
  // in the `implicit` with the `untagged` helper. Seems to work fine.
  @tagged type MyString = String

  trait HasLength[T] {
    def length(t: T): Int
  }

  implicit object StringHasLength extends HasLength[String] {
    def length(t: String): Int = t.length
  }

  implicit def taggedStringTypeFormat[NewTypeTag](implicit hasLength: HasLength[String]) = new HasLength[String @@ NewTypeTag] {
    def length(t: String @@ NewTypeTag): Int = t.asInstanceOf[String].length
  }
}