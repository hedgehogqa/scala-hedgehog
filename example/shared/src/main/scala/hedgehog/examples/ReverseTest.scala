package hedgehog.examples

import hedgehog._
import hedgehog.runner._

object ReverseTest extends Properties {

  def tests: List[Test] =
    List(
      Test("reverse", testReverse)
    )

  def testReverse: Property =
    for {
      xs <- Gen.alpha.list(Range.linear(0, 100)).forAll
         .cover(50, "empty", _.isEmpty)
         .cover(50, "nonempty", _.nonEmpty)
    } yield xs.reverse.reverse ==== xs
}
