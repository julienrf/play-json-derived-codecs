package julienrf.variants

import scala.language.experimental.macros

import play.api.libs.json.{Writes, Reads, Format, __}
import scala.reflect.macros.Context

object Variants {

  /**
   * @tparam A The base type of a case class hierarchy.
   * @return A [[play.api.libs.json.Format]] for the type hierarchy of `A`. It uses an additional field named `$variant`
   *         to discriminate between the possible subtypes of `A`.
   */
  def format[A]: Format[A] = macro Impl.format[A]

  /**
   * @param discriminator Format of the type discriminator field.
   * @tparam A Base type of case class hierarchy.
   * @return A [[play.api.libs.json.Format]] for the type hierarchy of `A`.
   */
  def format[A](discriminator: Format[String]): Format[A] = macro Impl.formatDiscriminator[A]

  /**
   * @tparam A The base type of a case class hierarchy.
   * @return A [[play.api.libs.json.Reads]] for the type hierarchy of `A`. It relies on an additional field named `$variant`
   *         to discriminate between the possible subtypes of `A`.
   */
  def reads[A]: Reads[A] = macro Impl.reads[A]

  /**
   * @param discriminator Decoder of the type discriminator field.
   * @tparam A Base type of case class hierarchy.
   * @return A [[play.api.libs.json.Reads]] for the type hierarchy of `A`.
   */
  def reads[A](discriminator: Reads[String]): Reads[A] = macro Impl.readsDiscriminator[A]

  /**
   * @tparam A The base type of a case class hierarchy.
   * @return A [[play.api.libs.json.Writes]] for the type hierarchy of `A`. It uses an additional field named `$variant`
   *         to discriminate between the possible subtypes of `A`.
   */
  def writes[A]: Writes[A] = macro Impl.writes[A]

  /**
   * @param discriminator Name of the type discriminator field.
   * @tparam A Base type of case class hierarchy.
   * @return A [[play.api.libs.json.Writes]] for the type hierarchy of `A`.
   */
  def writes[A](discriminator: Writes[String]): Writes[A] = macro Impl.writesDiscriminator[A]

  private object Impl {

    val defaultDiscriminator = (__ \ "$variant").format[String]

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
    def format[A : c.WeakTypeTag](c: Context): c.Expr[Format[A]] = {
      import c.universe._
      formatDiscriminator[A](c)(reify(defaultDiscriminator))
    }

    def formatDiscriminator[A : c.WeakTypeTag](c: Context)(discriminator: c.Expr[Format[String]]): c.Expr[Format[A]] = {
      import c.universe._
      val (baseClass, variants) = baseAndVariants[A](c)
      val writes = writesTree(c)(baseClass, variants, discriminator)
      val reads = readsTree(c)(baseClass, variants, discriminator)
      c.Expr[Format[A]](q"play.api.libs.json.Format[$baseClass]($reads, $writes)")
    }

    def reads[A : c.WeakTypeTag](c: Context): c.Expr[Reads[A]] = {
      import c.universe._
      readsDiscriminator[A](c)(reify(defaultDiscriminator))
    }

    def readsDiscriminator[A : c.WeakTypeTag](c: Context)
                                             (discriminator: c.Expr[Reads[String]]): c.Expr[Reads[A]] = {
      import c.universe._
      val (baseClass, variants) = baseAndVariants[A](c)
      c.Expr[Reads[A]](readsTree(c)(baseClass, variants, discriminator))
    }

    def writes[A : c.WeakTypeTag](c: Context): c.Expr[Writes[A]] = {
      import c.universe._
      writesDiscriminator[A](c)(reify(defaultDiscriminator))
    }

    def writesDiscriminator[A : c.WeakTypeTag](c: Context)(discriminator: c.Expr[Writes[String]]): c.Expr[Writes[A]] = {
      val (baseClass, variants) = baseAndVariants[A](c)
      c.Expr[Writes[A]](writesTree(c)(baseClass, variants, discriminator))
    }

    /*
     * Get the class hierarchy and checks that the hierarchy is closed
     */
    def baseAndVariants[A : c.WeakTypeTag](c: Context): (c.universe.ClassSymbol, Set[c.universe.ClassSymbol]) = {
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
      baseClass -> variants
    }

    def writesTree(c: Context)(baseClass: c.universe.ClassSymbol, variants: Set[c.universe.ClassSymbol], discriminator: c.Expr[Writes[String]]): c.Tree = {
      import c.universe._
      val writesCases = for (variant <- variants) yield {
        if (!variant.isModuleClass) {
          val term = newTermName(c.fresh())
          cq"""$term: $variant => play.api.libs.json.Json.toJson($term)(play.api.libs.json.Json.writes[$variant]).as[play.api.libs.json.JsObject] ++ $discriminator.writes(${variant.name.decodedName.toString})"""
        } else {
          cq"""_: $variant => $discriminator.writes(${variant.name.decodedName.toString})"""
        }
      }
      q"play.api.libs.json.Writes[$baseClass] { case ..$writesCases }"
    }

    def readsTree(c: Context)(baseClass: c.universe.ClassSymbol, variants: Set[c.universe.ClassSymbol], discriminator: c.Expr[Reads[String]]): c.Tree = {
      import c.universe._
      val readsCases = for (variant <- variants) yield {
        if (!variant.isModuleClass) {
          cq"""${variant.name.decodedName.toString} => play.api.libs.json.Json.fromJson(json)(play.api.libs.json.Json.reads[$variant])"""
        } else {
          cq"""${variant.name.decodedName.toString} => play.api.libs.json.JsSuccess(${newTermName(variant.name.decodedName.toString)})"""
        }
      }
        q"""
           play.api.libs.json.Reads[$baseClass](json =>
             $discriminator.reads(json).flatMap { case ..$readsCases }
           )
         """
    }
  }
}
