package julienrf.variants

import scala.language.experimental.macros

import play.api.libs.json.Format
import scala.reflect.macros.Context

object Variants {

  def format[A]: Format[A] = macro Impl.format[A]

  private object Impl {

    def format[A](c: Context)(implicit tag: c.WeakTypeTag[A]) = {
      import c.universe._
      val baseClass = tag.tpe.typeSymbol.asClass
      if (!baseClass.isSealed) {
        c.abort(c.enclosingPosition, s"$baseClass is not sealed")
      }
      // Get all the possible variants of this type
      val variants = baseClass.knownDirectSubclasses
      for (variant <- variants if !variant.asClass.isCaseClass) {
        c.abort(c.enclosingPosition, s"$variant is not a case class")
      }

      val writes =
        q"""
           play.api.libs.json.Writes[$baseClass] {
             ..${
               for (variant <- variants) yield {
                 val term = newTermName(c.fresh())
                 cq"""$term: $variant => play.api.libs.json.Json.toJson($term)(play.api.libs.json.Json.writes[$variant]).as[JsObject] + ("$$variant" -> play.api.libs.json.JsString(${variant.name.decoded}))"""
               }
             }
           }

        """

      val reads =
        q"""
           play.api.libs.json.Reads(json =>
             (json \ "$$variant").validate[String].flatMap {
               ..${
                 for (variant <- variants) yield {
                   cq"""${variant.name.decoded} => play.api.libs.json.Json.fromJson(json)(play.api.libs.json.reads[$variant])"""
                 }
               }
             }
           )
         """
      c.Expr(q"play.api.libs.json.Format($reads, $writes)")
    }

  }

}
