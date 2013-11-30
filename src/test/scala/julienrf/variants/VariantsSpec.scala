package julienrf.variants

import org.specs2.mutable.Specification
import play.api.libs.json._
import play.api.libs.json.JsSuccess

object VariantsSpec extends Specification {

  sealed trait Foo
  case class Bar(x: Int) extends Foo
  case class Baz(s: String) extends Foo

  val bar = Bar(42)
  val baz = Baz("bah")

  implicit val fooFormat: Format[Foo] = Variants.format[Foo]

  sealed trait A
  case class B(x: Int) extends A
  case class C(x: Int) extends A

  "Variants" should {

    "Generate an additional JSON field containing the variant name" in {
      (Json.toJson(bar) \ "$variant").as[String] must equalTo ("Bar")
      (Json.toJson(baz) \ "$variant").as[String] must equalTo ("Baz")
    }

    "Build the right variant from JSON data" in {
      Json.obj("$variant" -> "Bar", "x" -> 0).as[Foo] must equalTo (Bar(0))
      Json.obj("$variant" -> "Baz", "s" -> "hello").as[Foo] must equalTo (Baz("hello"))
    }

    "Serialize and deserialize any variant of a sum type" in {
      Json.toJson(bar).as[Foo] must equalTo (bar)
      Json.toJson(baz).as[Foo] must equalTo (baz)
    }

    "Support variants with the same types" in {
      implicit val format = Variants.format[A]
      Json.toJson(B(42)).as[A] must equalTo (B(42))
      Json.toJson(C(0)).as[A] must equalTo (C(0))
    }

  }

}
