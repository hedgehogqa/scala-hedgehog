package hedgehog.core

import hedgehog.StackSafeTest._
import hedgehog.runner._

object TreeTest extends Properties {

  private val ToProperty = λ[PF1[Tree, PropertyT]](treeProperty)

  override def tests: List[Test] =
    List(
      property("tailRecM is stack safe", propTailRecMIsStackSafe(ToProperty))
    )

  private def treeProperty[A](tree: Tree[A]): PropertyT[A] =
    GenT { (_, seed) => tree.map[(Seed, Option[A])](x => seed -> Some(x)) }.forAll
}
