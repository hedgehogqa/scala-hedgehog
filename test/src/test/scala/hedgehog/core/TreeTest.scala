package hedgehog.core

import hedgehog._
import hedgehog.runner._

object TreeTest extends Properties {

  override def tests: List[Test] =
    List(
      property("tailRecM is stack safe", StackSafeTest.propTailRecMIsStackSafe[Tree](treeProperty))
    )

  private def treeProperty(tree: Tree[List[Int]]): PropertyT[List[Int]] =
    GenT { (_, seed) => tree.map[(Seed, Option[List[Int]])](x => seed -> Some(x)) }.forAll
}
