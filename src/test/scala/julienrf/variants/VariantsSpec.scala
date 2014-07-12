package julienrf.variants

import org.specs2.mutable.Specification
import play.api.libs.json.{Json,Format}

object VariantsSpec extends Specification {

  sealed trait Foo
  case class Bar(x: Int) extends Foo
  case class Baz(s: String) extends Foo
  case object Bah extends Foo

  sealed trait Status
  case object ToDo extends Status
  case object Done extends Status

  val bar = Bar(42)
  val baz = Baz("bah")

  implicit val fooFormat: Format[Foo] = Variants.format[Foo]
  implicit val statusFormat: Format[Status] = Variants.format[Status]

  sealed trait A
  case class B(x: Int) extends A
  case class C(x: Int) extends A

  "Variants" should {

    "Generate an additional JSON field containing the variant name" in {
      (Json.toJson(bar) \ "$variant").as[String] must equalTo ("Bar")
      (Json.toJson(baz) \ "$variant").as[String] must equalTo ("Baz")
      (Json.toJson(Bah) \ "$variant").as[String] must equalTo ("Bah")
    }

    "Build the right variant from JSON data" in {
      Json.obj("$variant" -> "Bar", "x" -> 0).as[Foo] must equalTo (Bar(0))
      Json.obj("$variant" -> "Baz", "s" -> "hello").as[Foo] must equalTo (Baz("hello"))
      Json.obj("$variant" -> "Bah").as[Foo] must equalTo (Bah)
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

    "Support customization of discriminator field name" in {
      implicit val format = Variants.format[A]("type")
      (Json.toJson(B(42)) \ "type").as[String] must equalTo ("B")
      (Json.toJson(C(0)) \ "type").as[String] must equalTo ("C")
      Json.obj("type" -> "B", "x" -> 0).as[A] must equalTo (B(0))
      Json.obj("type" -> "C", "x" -> 0).as[A] must equalTo (C(0))

    }
  }

}
