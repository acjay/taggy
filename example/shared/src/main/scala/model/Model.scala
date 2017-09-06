package model

import com.acjay.taggy.tagged

object Model {
  // Declared in a separate class to make sure importing the name also brings 
  // in the `implicit` with the `untagged` helper. Seems to work fine.
  @tagged[String] type MyString
}