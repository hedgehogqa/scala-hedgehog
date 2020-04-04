package hedgehog.core

import hedgehog._
import hedgehog.runner._

object PropertyTTest extends Properties {

  private val ToProperty = ??? //λ[PF1[PropertyT, PropertyT]](identity)

  override def tests: List[Test] =
    List(
      property("tailRecM is stack safe", StackSafeTest.propTailRecMIsStackSafe[PropertyT](ToProperty))
    )
}
