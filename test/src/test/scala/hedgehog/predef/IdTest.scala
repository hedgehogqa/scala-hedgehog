package hedgehog.predef

import hedgehog.StackSafeTest.PF1
import hedgehog._
import hedgehog.core.PropertyT
import hedgehog.runner._

object IdTest extends Properties {

  private val ToProperty = λ[PF1[Id, PropertyT]](identityProperty)

  override def tests: List[Test] =
    List(
      property("tailRecM is stack safe", StackSafeTest.propTailRecMIsStackSafe(ToProperty))
    )

  private def identityProperty[A](id: Id[A]): PropertyT[A] =
    Gen.constant(id).forAll
}
