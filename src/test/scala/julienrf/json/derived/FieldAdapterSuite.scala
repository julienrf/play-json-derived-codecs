package julienrf.json.derived

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.FeatureSpec
import org.scalatest.prop.Checkers
import play.api.libs.json._

class FieldAdapterSuite extends FeatureSpec with Checkers {

  feature("use camelCase as the default casing for field names") {

    scenario("product type") {
      case class Foo(sC: String, iC: Int)
      implicit val fooArbitrary: Arbitrary[(Foo, JsValue)] =
        Arbitrary(for (s <- Gen.alphaStr; i <- arbitrary[Int]) yield (Foo(s, i), Json.obj("sC" -> s, "iC" -> i)))
      implicit val fooFormat: OFormat[Foo] = oformat
      jsonIdentityLaw[Foo]
    }


    scenario("sum types") {
      sealed trait Foo
      case class Bar(xC: Int) extends Foo
      case class Baz(sC: String) extends Foo
      case object Bah extends Foo

      implicit val fooArbitrary: Arbitrary[(Foo, JsValue)] =
        Arbitrary(
          Gen.oneOf(
            arbitrary[Int].map(i => (Bar(i), Json.obj("xC" -> i, "type" -> "Bar"))),
            Gen.alphaStr.map(s => (Baz(s), Json.obj("sC" -> s, "type" -> "Baz"))),
            Gen.const((Bah, Json.obj("type" -> "Bah"))
            )
          ))

      implicit val fooFormat: OFormat[Foo] = flat.oformat((__ \ "type").format[String])
      jsonIdentityLaw[Foo]

    }

  }

  feature("customize the casing for field names") {
    implicit val snake = Adapter.snakeCase

    scenario("product type") {
      case class Foo(sC: String, iC: Int)
      implicit val fooArbitrary: Arbitrary[(Foo, JsValue)] =
        Arbitrary(for (s <- Gen.alphaStr; i <- arbitrary[Int]) yield (Foo(s, i), Json.obj("s_c" -> s, "i_c" -> i)))
      implicit val fooFormat: OFormat[Foo] = oformat
      jsonIdentityLaw[Foo]
    }

    scenario("sum types") {
      sealed trait Foo
      case class Bar(xC: Int) extends Foo
      case class Baz(sC: String) extends Foo
      case object Bah extends Foo

      implicit val fooArbitrary: Arbitrary[(Foo, JsValue)] =
        Arbitrary(
          Gen.oneOf(
            arbitrary[Int].map(i => (Bar(i), Json.obj("x_c" -> i, "type" -> "Bar"))),
            Gen.alphaStr.map(s => (Baz(s), Json.obj("s_c" -> s, "type" -> "Baz"))),
            Gen.const((Bah, Json.obj("type" -> "Bah"))
            )
          ))

      implicit val fooFormat: OFormat[Foo] = flat.oformat((__ \ "type").format[String])
      jsonIdentityLaw[Foo]
    }
  }


  def jsonIdentityLaw[A](implicit reads: Reads[A], owrites: OWrites[A], arbA: Arbitrary[(A, JsValue)]): Unit =
    check((a: (A, JsValue)) => {
      reads.reads(a._2).fold(_ => false, r => r == a._1 && owrites.writes(r) == a._2)
    })

}
