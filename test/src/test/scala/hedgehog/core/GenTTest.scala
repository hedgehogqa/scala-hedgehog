package hedgehog.core

import hedgehog._
import hedgehog.runner._

object GenTTest extends Properties {

  override def tests: List[Test] =
    List(
      property("tailRecM is stack safe", StackSafeTest.propTailRecMIsStackSafe[GenT](_.forAll))
    )
}
