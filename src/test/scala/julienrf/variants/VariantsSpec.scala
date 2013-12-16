package julienrf.variants

import org.specs2.mutable.Specification
import play.api.libs.json._
import play.api.libs.json.JsSuccess

object VariantsSpec extends Specification {

  sealed trait Foo
  case class Bar(x: Int) extends Foo
  case class Baz(s: String) extends Foo
  case object Bar extends Foo

  sealed trait Status
  object Status{
    def apply(s:String):Option[Status]=s match {
      case todo if todo == ToDo.toString => Some(ToDo)
      case done if done == Done.toString => Some(Done)
      case _ => None
    }
  }
  case object ToDo extends Status
  case object Done extends Status

  val bar = Bar(42)
  val baz = Baz("bah")

  implicit val fooFormat: Format[Foo] = Variants.format[Foo]
  implicit val statusReads:Reads[Status] = play.api.libs.json.Reads(json =>
    json.validate[String].flatMap { case s if s == ToDo.toString => JsSuccess(ToDo) }
  )
  implicit val statusFormat: Format[Status] = Variants.format[Status]

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

    "Support variants for case objects based on object's toString" in {
      Json.toJson(ToDo) must equalTo(JsString("ToDo"))
      Json.toJson(Done) must equalTo(JsString("Done"))
    }

    "Support variants for case objects" in {
      Json.toJson(ToDo).as[Status] must equalTo (ToDo)
      Json.toJson(Done).as[Status] must equalTo (Done)
    }
  }

}
