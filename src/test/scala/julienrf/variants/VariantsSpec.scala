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

  implicit val fooFormat: Format[Foo] = {
    import play.api.libs.json.{Writes, Reads}

    val writes = Writes[Foo] {
      case bar: Bar => Json.toJson(bar)(Json.writes[Bar]).as[JsObject] + ("$variant" -> JsString("Bar"))
      case baz: Baz => Json.toJson(baz)(Json.writes[Baz]).as[JsObject] + ("$variant" -> JsString("Baz"))
    }

    val reads = Reads[Foo] { json =>
      (json \ "$variant").validate[String].flatMap {
        case "Bar" => Json.fromJson(json)(Json.reads[Bar])
        case "Baz" => Json.fromJson(json)(Json.reads[Baz])
      }
    }

    Format(reads, writes)
  }

  //implicit val fooFormat: Format[Foo] = Variants.format[Foo]

  "Variants" should {

    "Serialize and deserialize any variant of a sum type" in {
      Json.fromJson[Foo](Json.toJson(bar)).get must equalTo (bar)
    }

  }

}
