package hedgehog.core

import hedgehog._
import hedgehog.runner._

object PropertyTTest extends Properties {

  override def tests: List[Test] =
    List(
      property("tailRecM is stack safe", StackSafeTest.propTailRecMIsStackSafe[PropertyT](identity))
    )
}
