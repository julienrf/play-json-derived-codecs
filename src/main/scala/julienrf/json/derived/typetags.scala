package julienrf.json.derived

import play.api.libs.json.{Reads, Json, OWrites, __}

/**
  * Strategy to serialize a tagged type (used to discriminate sum types).
  *
  * Built-in instances live in the [[TypeTagOWrites$ companion object]].
  */
trait TypeTagOWrites {
  /**
    * @param typeName Type name
    * @param owrites Base serializer
    * @return A serializer that encodes an `A` value along with its type tag
    */
  def owrites[A](typeName: String, owrites: OWrites[A]): OWrites[A]
}

object TypeTagOWrites {

  /**
    * Encodes a tagged type by creating a JSON object wrapping the actual `A` JSON representation. This wrapper
    * is an object with just one field whose name is the type tag.
    *
    * For instance, consider the following type definition:
    *
    * {{{
    *   sealed trait Foo
    *   case class Bar(s: String, i: Int) extends Foo
    *   case object Baz extends Foo
    * }}}
    *
    * The JSON representation of `Bar("quux", 42)` is the following JSON object:
    *
    * {{{
    *   {
    *     "Bar": {
    *       "s": "quux",
    *       "i": 42
    *     }
    *   }
    * }}}
    */
  val nested: TypeTagOWrites =
    new TypeTagOWrites {
      def owrites[A](typeName: String, owrites: OWrites[A]): OWrites[A] =
        OWrites[A](a => Json.obj(typeName -> owrites.writes(a)))
    }

  /**
    * Encodes a tagged type by adding an extra field to the base `A` JSON representation.
    *
    * For instance, consider the following type definition:
    *
    * {{{
    *   sealed trait Foo
    *   case class Bar(s: String, i: Int) extends Foo
    *   case object Baz extends Foo
    * }}}
    *
    * And also:
    *
    * {{{
    *   implicit val fooOWrites: OWrites[Foo] = derived.flat.owrites((__ \ "type").write)
    * }}}
    *
    * The JSON representation of `Bar("quux", 42)` is then the following JSON object:
    *
    * {{{
    *   {
    *     "type": "Bar",
    *     "s": "quux",
    *     "i": 42
    *   }
    * }}}
    *
    * @param tagOwrites A way to encode the type tag as a JSON object (whose fields will be merged with the base JSON representation)
    */
  def flat(tagOwrites: OWrites[String]): TypeTagOWrites =
    new TypeTagOWrites {
      def owrites[A](typeName: String, owrites: OWrites[A]): OWrites[A] =
        OWrites[A](a => tagOwrites.writes(typeName) ++ owrites.writes(a))
    }


}

/**
  * Strategy to deserialize a tagged type (used to discriminate sum types).
  *
  * Built-in instances live in the [[TypeTagReads$ companion object]].
  */
trait TypeTagReads {
  /**
    * @param typeName Type name
    * @param reads Base deserializer
    * @return A deserializer that decodes a subtype of `A` based on the given `typeName` discriminator.
    */
  def reads[A](typeName: String, reads: Reads[A]): Reads[A]
}

object TypeTagReads {

  /**
    * Decodes a JSON value encoded with [[TypeTagOWrites.nested]].
    */
  val nested: TypeTagReads =
    new TypeTagReads {
      def reads[A](typeName: String, reads: Reads[A]): Reads[A] =
        (__ \ typeName).read(reads)
    }

  /**
    * Decodes a JSON value encoded with [[TypeTagOWrites.flat]].
    *
    * @param tagReads A way to decode the type tag value.
    */
  def flat(tagReads: Reads[String]): TypeTagReads =
    new TypeTagReads {
      def reads[A](typeName: String, reads: Reads[A]): Reads[A] =
        tagReads.filter(_ == typeName).flatMap(_ => reads)
    }

}