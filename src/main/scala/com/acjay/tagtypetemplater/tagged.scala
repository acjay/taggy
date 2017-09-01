package com.acjay.tagtypetemplater

import scala.meta._

class tagged(_underlyingTypeName: String) extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    // println(this) => new tagged("String")
    // Can't figure out how to do this extraction as a quasiquote, so I 
    // figured out exactly the AST `this` produces to extract the string 
    // parameter.
    //val Lit.String(underlyingTypeName) = this
    //val q"new $_(Lit.String($underlyingTypeName))" = this
    val Term.New(
      Template(
        List(),
        List(Term.Apply(Ctor.Ref.Name("tagged"), List(Lit.String(underlyingTypeName)))),
        Term.Param(List(), Name.Anonymous(), None, None),
        None
      )
    ) = this
    
    val q"..$mods type $tname[..$tparams]" = defn
    val underlyingType = Type.Name(underlyingTypeName)
    TaggedImpl.expand(tname, underlyingType)
  }
}

object TaggedImpl {
  def expand(taggedType: Type.Name, underlyingType: Type.Name) = {
    val tag = Type.Name(taggedType.value + "Tag")
    val companionObject = Term.Name(taggedType.value)
    val opsClass = Type.Name(taggedType.value + "Ops")

    q"""
      sealed trait $tag
      type $taggedType = shapeless.tag.@@[$underlyingType, $tag]
      object $companionObject {
        def fromString(untagged: $underlyingType): $taggedType = {
          val tagged = shapeless.tag[$tag](untagged)
          tagged
        }
      }
      implicit class $opsClass(val tagged: $taggedType) extends AnyVal { 
        def untagged = tagged.asInstanceOf[$underlyingType] 
      }
    """
  }
}