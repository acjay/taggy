package com.acjay.tagtypetemplater

import scala.meta._

/**
 * Annotation that turns an unassigned type declaration (what would normally 
 * syntactically be an abstract type member) into a tagged type of the given
 * underlying real type.
 * @tparam UnderlyingType the underlying (real) type.
 */
class tagged[UnderlyingType] extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    // Macro annotation type and value parameters come back as AST data, not 
    // values, and are accessed by destructuring `this`.
    val q"new tagged[${underlyingType: Type.Name}]()" = this
    val q"..$mods type $tname" = defn
    ShapelessTaggedImpl.expand(tname, underlyingType)
  }
}

object ShapelessTaggedImpl {
  def expand(taggedType: Type.Name, underlyingType: Type.Name) = {
    // Shapeless needs a phantom type to join with the underlying type to
    // create our tagged type. Ideally should never leak to external code.
    val tag = Type.Name(taggedType.value + "Tag")

    // The `fromX` helper will go in the companion object.
    val companionObject = Term.Name(taggedType.value)

    // We'll name the `fromX` method based on the underlying type.
    val fromMethodName = Term.Name("from" + underlyingType.value)

    // The `untagged` helper goes in an implicit class, since the tagged type
    // is only a type alias, and can't have real methods. 
    val opsClass = Type.Name(taggedType.value + "Ops")

    q"""
      sealed trait $tag
      type $taggedType = shapeless.tag.@@[$underlyingType, $tag]
      object $companionObject {
        def $fromMethodName(untagged: $underlyingType): $taggedType = {
          val tagged = shapeless.tag[$tag](untagged)
          tagged
        }
      }
      implicit class $opsClass(val tagged: $taggedType) extends AnyVal { 
        def untagged = tagged.asInstanceOf[$underlyingType]
        def modify(f: $underlyingType => $underlyingType) = $companionObject.$fromMethodName(f(untagged))
      }
    """
  }
}