package hedgehog.core

import hedgehog.StackSafeTest.PF1
import hedgehog._
import hedgehog.runner._

object GenTTest extends Properties {

  private val ToProperty = λ[PF1[GenT, PropertyT]](_.forAll)

  override def tests: List[Test] =
    List(
      property("tailRecM is stack safe", StackSafeTest.propTailRecMIsStackSafe(ToProperty))
    )
}
