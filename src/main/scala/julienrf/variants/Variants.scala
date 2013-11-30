package julienrf.variants

import scala.language.experimental.macros

import play.api.libs.json.Format
import scala.reflect.macros.Context

object Variants {

  /**
   * @tparam A The base type of a case class hierarchy.
   * @return A [[play.api.libs.json.Format]] for the type hierarchy of `A`.
   */
  def format[A]: Format[A] = macro Impl.format[A]

  private object Impl {

    /**
     * Given the following definition of class hierarchy `Foo`:
     *
     * {{{
     *   sealed trait Foo
     *   case class Bar(x: Int) extends Foo
     *   case class Baz(s: String) extends Foo
     * }}}
     *
     * `Variants.format[Foo]` expands to the following:
     *
     * {{{
     *   {
     *     import play.api.libs.json.{Writes, Reads}
     *
     *     val writes = Writes[Foo] {
     *       case bar: Bar => Json.toJson(bar)(Json.writes[Bar]).as[JsObject] + ("$variant" -> JsString("Bar"))
     *       case baz: Baz => Json.toJson(baz)(Json.writes[Baz]).as[JsObject] + ("$variant" -> JsString("Baz"))
     *     }
     *
     *     val reads = Reads[Foo] { json =>
     *       (json \ "$variant").validate[String].flatMap {
     *         case "Bar" => Json.fromJson(json)(Json.reads[Bar])
     *         case "Baz" => Json.fromJson(json)(Json.reads[Baz])
     *       }
     *     }
     *
     *     Format(reads, writes)
     *   }
     *
     * }}}
     *
     */
    def format[A : c.WeakTypeTag](c: Context) = {
      import c.universe._
      val baseClass = weakTypeOf[A].typeSymbol.asClass
      baseClass.typeSignature // SI-7046
      if (!baseClass.isSealed) {
        c.abort(c.enclosingPosition, s"$baseClass is not sealed")
      }
      // Get all the possible variants of this type
      val variants = baseClass.knownDirectSubclasses
      for (variant <- variants if !variant.asClass.isCaseClass) {
        c.abort(c.enclosingPosition, s"$variant is not a case class")
      }

      val writesCases = for (variant <- variants) yield {
        val term = newTermName(c.fresh())
        cq"""$term: $variant => play.api.libs.json.Json.toJson($term)(play.api.libs.json.Json.writes[$variant]).as[JsObject] + ("$$variant" -> play.api.libs.json.JsString(${variant.name.decoded}))"""
      }
      val writes = q"play.api.libs.json.Writes[$baseClass] { case ..$writesCases }"

      val readsCases = for (variant <- variants) yield {
        cq"""${variant.name.decoded} => play.api.libs.json.Json.fromJson(json)(play.api.libs.json.Json.reads[$variant])"""
      }
      val reads =
        q"""
           play.api.libs.json.Reads(json =>
             (json \ "$$variant").validate[String].flatMap { case ..$readsCases }
           )
         """
      c.Expr(q"play.api.libs.json.Format($reads, $writes)")
    }

  }

}
