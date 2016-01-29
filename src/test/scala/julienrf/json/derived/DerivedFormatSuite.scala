package julienrf.json.derived

import org.scalacheck.{Gen, Arbitrary}
import org.scalatest.FunSuite
import org.scalatest.prop.Checkers
import play.api.libs.json.Reads

class DerivedFormatSuite extends FunSuite with Checkers {

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
