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

  def format[A](discriminator: String): Format[A] = macro Impl.formatDiscriminator[A]

  private object Impl {

    def format[A : c.WeakTypeTag](c: Context): c.Expr[Format[A]] = {
      import c.universe._
      formatDiscriminator[A](c)(reify("$variant"))
    }

    /**
     * Given the following definition of class hierarchy `Foo`:
     *
     * {{{
     *   sealed trait Foo
     *   case class Bar(x: Int) extends Foo
     *   case class Baz(s: String) extends Foo
     *   case object Bah extends Foo
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
     *       case _: Bah => JsObject(Seq("$variant" -> JsString("Bah")))
     *     }
     *
     *     val reads = Reads[Foo] { json =>
     *       (json \ "$variant").validate[String].flatMap {
     *         case "Bar" => Json.fromJson(json)(Json.reads[Bar])
     *         case "Baz" => Json.fromJson(json)(Json.reads[Baz])
     *         case "Bah" => JsSuccess(Bah)
     *       }
     *     }
     *
     *     Format(reads, writes)
     *   }
     *
     * }}}
     *
     */
    def formatDiscriminator[A : c.WeakTypeTag](c: Context)(discriminator: c.Expr[String]): c.Expr[Format[A]] = {
      import c.universe._
      val baseClass = weakTypeOf[A].typeSymbol.asClass
      baseClass.typeSignature // SI-7046
      if (!baseClass.isSealed) {
        c.abort(c.enclosingPosition, s"$baseClass is not sealed")
      }
      // Get all the possible variants of this type
      val variants = baseClass.knownDirectSubclasses.map(_.asClass)
      for (variant <- variants if !(variant.isCaseClass || variant.isModuleClass)) {
        c.abort(c.enclosingPosition, s"$variant is not a case class nor a case object")
      }

      val writesCases = for (variant <- variants) yield {
        if (!variant.isModuleClass) {
          val term = newTermName(c.fresh())
          cq"""$term: $variant => play.api.libs.json.Json.toJson($term)(play.api.libs.json.Json.writes[$variant]).as[play.api.libs.json.JsObject] + ($discriminator -> play.api.libs.json.JsString(${variant.name.decodedName.toString}))"""
        } else {
          cq"""_: $variant => play.api.libs.json.JsObject(Seq($discriminator -> play.api.libs.json.JsString(${variant.name.decodedName.toString})))"""
        }
      }
      val writes = q"play.api.libs.json.Writes[$baseClass] { case ..$writesCases }"

      val readsCases = for (variant <- variants) yield {
        if (!variant.isModuleClass) {
          cq"""${variant.name.decodedName.toString} => play.api.libs.json.Json.fromJson(json)(play.api.libs.json.Json.reads[$variant])"""
        } else {
          cq"""${variant.name.decodedName.toString} => play.api.libs.json.JsSuccess(${newTermName(variant.name.decodedName.toString)})"""
        }
      }
      val reads =
        q"""
           play.api.libs.json.Reads(json =>
             (json \ $discriminator).validate[String].flatMap { case ..$readsCases }
           )
         """
      c.Expr[Format[A]](q"play.api.libs.json.Format[$baseClass]($reads, $writes)")
    }
  }
}
