package hedgehog.predef

import hedgehog._
import hedgehog.core.PropertyT
import hedgehog.runner._

object IdTest extends Properties {

  override def tests: List[Test] =
    List(
      property("tailRecM is stack safe", StackSafeTest.propTailRecMIsStackSafe[Id](identityProperty))
    )

  private def identityProperty(id: Id[List[Int]]): PropertyT[List[Int]] =
    Gen.constant(id).forAll
}
