package julienrf.json.derived

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.FeatureSpec
import org.scalatest.prop.Checkers
import play.api.libs.json.{OFormat, OWrites, Reads}

class DerivedOFormatSuite extends FeatureSpec with Checkers {

  feature("product types") {
    case class Foo(s: String, i: Int)

    implicit val fooFormat: OFormat[Foo] = oformat[Foo]
    implicit val fooArbitrary: Arbitrary[Foo] =
      Arbitrary(for (s <- arbitrary[String]; i <- arbitrary[Int]) yield Foo(s, i))

    scenario("identity") {
      identityLaw[Foo]
    }
  }

  feature("tuple types") {
    type Foo = (String, Int)

    implicit val fooFormat: OFormat[Foo] = oformat[Foo]

    scenario("identity") {
      identityLaw[Foo]
    }
  }

  feature("sum types") {
    sealed trait Foo
    case class Bar(x: Int) extends Foo
    case class Baz(s: String) extends Foo
    case object Bah extends Foo

    implicit val fooFormat: OFormat[Foo] = oformat[Foo]
    implicit val fooArbitrary: Arbitrary[Foo] =
      Arbitrary(
        Gen.oneOf(
          arbitrary[Int].map(Bar),
          arbitrary[String].map(Baz),
          Gen.const(Bah)
        )
      )

    scenario("identity") {
      identityLaw[Foo]
    }
  }

  feature("recursive types") {
    sealed trait Tree
    case class Leaf(s: String) extends Tree
    case class Node(lhs: Tree, rhs: Tree) extends Tree

    implicit lazy val treeFormat: OFormat[Tree] = oformat[Tree]
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

    scenario("identity") {
      identityLaw[Tree]
    }
  }

  def identityLaw[A](implicit reads: Reads[A], owrites: OWrites[A], arbA: Arbitrary[A]): Unit =
    check((a: A) => reads.reads(owrites.writes(a)).fold(_ => false, _ == a))

}
