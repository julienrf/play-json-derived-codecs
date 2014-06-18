package julienrf.variants

import org.specs2.mutable.Specification
import play.api.libs.json._
import play.api.libs.json.JsSuccess

object VariantsTSpec extends Specification {

  sealed trait Foo
  case class Bar(x: Int) extends Foo
  case class Baz(s: String) extends Foo
  case object Bah extends Foo

  sealed trait Status
  case object ToDo extends Status
  case object Done extends Status

  val bar = Bar(42)
  val baz = Baz("bah")

  implicit val fooFormat: Format[Foo] = Variants.formatT[Foo]("type")
  implicit val statusFormat: Format[Status] = Variants.formatT[Status]("type")

  sealed trait A
  case class B(x: Int) extends A
  case class C(x: Int) extends A

  "Variants" should {

    "Generate an additional JSON field containing the variant name" in {
      (Json.toJson(bar) \ "type").as[String] must equalTo ("Bar")
      (Json.toJson(baz) \ "type").as[String] must equalTo ("Baz")
      (Json.toJson(Bah) \ "type").as[String] must equalTo ("Bah")
    }

    "Build the right variant from JSON data" in {
      Json.obj("type" -> "Bar", "x" -> 0).as[Foo] must equalTo (Bar(0))
      Json.obj("type" -> "Baz", "s" -> "hello").as[Foo] must equalTo (Baz("hello"))
      Json.obj("type" -> "Bah").as[Foo] must equalTo (Bah)
    }

    "Serialize and deserialize any variant of a sum type" in {
      Json.toJson(bar).as[Foo] must equalTo (bar)
      Json.toJson(baz).as[Foo] must equalTo (baz)
      Json.toJson(Bah).as[Foo] must equalTo (Bah)
    }

    "Support variants with the same types" in {
      implicit val format = Variants.format[A]
      Json.toJson(B(42)).as[A] must equalTo (B(42))
      Json.toJson(C(0)).as[A] must equalTo (C(0))
    }

    "Support case object style enumerations" in {
      Json.toJson(ToDo).as[Status] must equalTo (ToDo)
      Json.toJson(Done).as[Status] must equalTo (Done)
    }
  }

}
