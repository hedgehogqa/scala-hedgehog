package hedgehog.predef

import hedgehog._
import hedgehog.core.PropertyT
import hedgehog.runner._

object IdentityTest extends Properties {

  override def tests: List[Test] =
    List(
      property("tailRecM is stack safe", StackSafeTest.propTailRecMIsStackSafe[Identity](identityProperty))
    )

  private def identityProperty(id: Identity[List[Int]]): PropertyT[List[Int]] =
    Gen.constant(id.value).forAll
}
