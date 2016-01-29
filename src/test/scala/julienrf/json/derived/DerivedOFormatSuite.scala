package julienrf.json.derived

import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.FeatureSpec
import org.scalatest.prop.Checkers
import play.api.libs.json.Reads

class DerivedOFormatSuite extends FeatureSpec with Checkers {

  feature("product types") {
    case class Foo(s: String, i: Int)

    implicit val fooReads: Reads[Foo] = reads[Foo]
  }

  feature("sum types") {
    sealed trait Foo
    case class Bar(x: Int) extends Foo
    case class Baz(s: String) extends Foo
    case object Bah extends Foo

    implicit val fooReads: Reads[Foo] = reads[Foo]
  }

  feature("recursive types") {
    sealed trait Tree
    case class Leaf(s: String) extends Tree
    case class Node(lhs: Tree, rhs: Tree) extends Tree

    def atDepth(depth: Int): Gen[Tree] =
      if (depth < 3) {
        Gen.oneOf(
          Arbitrary.arbitrary[String].map(Leaf),
          for {
            lhs <- atDepth(depth + 1)
            rhs <- atDepth(depth + 1)
          } yield Node(lhs, rhs)
        )
      } else Arbitrary.arbitrary[String].map(Leaf)

    implicit val arbitraryTree: Arbitrary[Tree] = Arbitrary(atDepth(0))

    implicit val treeReads: Reads[Tree] = reads[Tree]
  }
}
