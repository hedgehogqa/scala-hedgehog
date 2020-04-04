package hedgehog.predef

import hedgehog.StackSafeTest.PF1
import hedgehog._
import hedgehog.core.PropertyT
import hedgehog.runner._

object IdentityTest extends Properties {

  private val ToProperty = λ[PF1[Identity, PropertyT]](identityProperty)

  override def tests: List[Test] =
    List(
      property("tailRecM is stack safe", StackSafeTest.propTailRecMIsStackSafe(ToProperty))
    )

  private def identityProperty[A](id: Identity[A]): PropertyT[A] =
    Gen.constant(id.value).forAll
}
