package julienrf.json.derived

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary
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
      check { (foo: Foo) =>
        fooFormat.reads(fooFormat.writes(foo)).fold(_ => false, _ == foo)
      }
    }
  }

  feature("sum types") {
    sealed trait Foo
    case class Bar(x: Int) extends Foo
    case class Baz(s: String) extends Foo
    case object Bah extends Foo

    implicit val fooReads: Reads[Foo] = reads[Foo]
    implicit val fooWrites: OWrites[Foo] = owrites[Foo]
  }

  feature("recursive types") {
    sealed trait Tree
    case class Leaf(s: String) extends Tree
    case class Node(lhs: Tree, rhs: Tree) extends Tree

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

    implicit val arbitraryTree: Arbitrary[Tree] = Arbitrary(atDepth(0))

    implicit lazy val treeReads: Reads[Tree] = reads[Tree]
    implicit lazy val treeWrites: OWrites[Tree] = owrites[Tree]
  }
}
