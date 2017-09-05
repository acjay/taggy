package com.acjay.taggy

import scala.meta._
import scala.collection.immutable.Seq

/**
 * <p>Annotation that turns an unassigned type declaration (what would normally 
 * syntactically be an abstract type member) into a tagged type of the given
 * underlying real type.</p>
 *
 * <p>Uses this annotation as follows:</p> 
 *
 * <pre>@tagged[UnderlyingType] type NewType</pre>
 *
 * <p>To upgrade a value of UnderlyingType to NewType:</p>
 *
 * <pre>NewType.fromUnderlyingType(underlyingTypeValue)</pre>
 *
 * <p>To downgrade it back to UnderlyingType:</p>
 *
 * <pre>newTypeValue.untagged</pre>
 * 
 * @tparam UnderlyingType the underlying (real) type.
 */
class tagged[UnderlyingType] extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    // Macro annotation type and value parameters come back as AST data, not 
    // values, and are accessed by destructuring `this`.
    (this, defn) match {
      case (
        q"new tagged[${underlyingType: Type.Name}]()",
        q"..$mods type $newType"
      ) => ShapelessTaggedImpl.expand(underlyingType, newType, mods)
      case _ => abort("Correct usage: @tagged[UnderlyingType] type NewType" )
    }
  }
}

object ShapelessTaggedImpl {
  def expand(underlyingType: Type.Name, newType: Type.Name, mods: Seq[Mod]) = {
    // Shapeless needs a phantom type to join with the underlying type to
    // create our tagged type. Ideally should never leak to external code.
    val tag = Type.Name(newType.value + "Tag")

    // The `fromX` helper will go in the companion object.
    val companionObject = Term.Name(newType.value)

    // We'll name the `fromX` method based on the underlying type.
    val fromMethod = Term.Name("from" + underlyingType.value)

    // The `untagged` helper goes in an implicit class, since the tagged type
    // is only a type alias, and can't have real methods. 
    val opsClass = Type.Name(newType.value + "Ops")

    q"""
      sealed trait $tag
      ..$mods type $newType = shapeless.tag.@@[$underlyingType, $tag]
      ..$mods object $companionObject {
        def $fromMethod(untagged: $underlyingType): $newType = {
          val tagged = shapeless.tag[$tag](untagged)
          tagged
        }
      }
      ..$mods implicit class $opsClass(val tagged: $newType) extends AnyVal { 
        def untagged = tagged.asInstanceOf[$underlyingType]
        def modify(f: $underlyingType => $underlyingType) = $companionObject.$fromMethod(f(untagged))
      }
    """
  }
}