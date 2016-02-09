package julienrf.json.derived

import org.scalacheck.{Gen, Arbitrary}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.FeatureSpec
import org.scalatest.prop.Checkers
import play.api.libs.json.{JsNumber, Json, OFormat, OWrites, Reads, __}

class DerivedOFormatSuite extends FeatureSpec with Checkers {

  feature("encoding andThen decoding = identity") {

    scenario("product type") {
      case class Foo(s: String, i: Int)
      implicit val fooArbitrary: Arbitrary[Foo] =
        Arbitrary(for (s <- arbitrary[String]; i <- arbitrary[Int]) yield Foo(s, i))
      implicit val fooFormat: OFormat[Foo] = oformat[Foo]
      identityLaw[Foo]
    }

    scenario("tuple type") {
      type Foo = (String, Int)
      implicit val fooFormat: OFormat[Foo] = oformat[Foo]

      identityLaw[Foo]
    }

    scenario("sum types") {
      sealed trait Foo
      case class Bar(x: Int) extends Foo
      case class Baz(s: String) extends Foo
      case object Bah extends Foo

      implicit val fooArbitrary: Arbitrary[Foo] =
        Arbitrary(
          Gen.oneOf(
            arbitrary[Int].map(Bar),
            arbitrary[String].map(Baz),
            Gen.const(Bah)
          )
        )

      {
        implicit val fooFormat: OFormat[Foo] = oformat[Foo]
        identityLaw[Foo]
      }
      {
        implicit val fooFormat: OFormat[Foo] = flat.oformat[Foo]((__ \ "type").format[String])
        identityLaw[Foo]
      }
    }

    scenario("recursive types") {
      sealed trait Tree
      case class Leaf(s: String) extends Tree
      case class Node(lhs: Tree, rhs: Tree) extends Tree

      implicit val arbitraryTree: Arbitrary[Tree] = {
        def atDepth(depth: Int): Gen[Tree] =
          if (depth < 3) {
            Gen.oneOf(
              arbitrary[String].map(Leaf),
              for {
                lhs <- atDepth(depth + 1)
                rhs <- atDepth(depth + 1)
              } yield Node(lhs, rhs)
            )
          } else arbitrary[String].map(Leaf)
        Arbitrary(atDepth(0))
      }

      {
        implicit lazy val treeFormat: OFormat[Tree] = oformat[Tree]
        identityLaw[Tree]
      }
      {
        implicit lazy val treeFormat: OFormat[Tree] = flat.oformat[Tree]((__ \ "$type").format[String])
        identityLaw[Tree]
      }
    }

    scenario("polylmorphic types") {
      case class Quux[A](value: A)
      implicit val fooFormat: OFormat[Quux[Int]] = oformat[Quux[Int]]
      implicit val arbitraryFoo: Arbitrary[Quux[Int]] =
        Arbitrary(arbitrary[Int].map(new Quux(_)))
      identityLaw[Quux[Int]]
    }
  }

  def identityLaw[A](implicit reads: Reads[A], owrites: OWrites[A], arbA: Arbitrary[A]): Unit =
    check((a: A) => reads.reads(owrites.writes(a)).fold(_ => false, _ == a))

  feature("default codecs represent sum types using nested JSON objects") {
    sealed trait Foo
    case class Bar(x: Int) extends Foo
    case class Baz(s: String) extends Foo
    val fooFormat: OFormat[Foo] = oformat[Foo]
    assert(fooFormat.writes(Bar(42)) == Json.obj("Bar" -> Json.obj("x" -> JsNumber(42))))
  }

  feature("sum types JSON representation can be customized") {
    sealed trait Foo
    case class Bar(x: Int) extends Foo
    case class Baz(s: String) extends Foo
    val fooFlatFormat: OFormat[Foo] = flat.oformat[Foo]((__ \ "type").format[String])
    assert(fooFlatFormat.writes(Bar(42)) == Json.obj("type" -> "Bar", "x" -> JsNumber(42)))
  }

//  feature("case classes can have optional values") {
//    case class Foo(s: Option[String], i: Int)
//    implicit val fooFormat: OFormat[Foo] = oformat[Foo]
//    implicit val arbitraryFoo: Arbitrary[Foo] =
//      Arbitrary(for (s <- arbitrary[Option[String]]; i <- arbitrary[Int]) yield Foo(s, i))
//  }

}
