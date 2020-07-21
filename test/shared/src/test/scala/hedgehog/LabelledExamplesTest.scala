package hedgehog

import hedgehog.core._
import hedgehog.runner._

object LabelledExamplesTest extends Properties {

  def tests: List[Test] =
    List(
      property("testLabelledExamples", testLabelledExamples)
    , property("testProperty", prop).withExamples
    )

  def prop: Property =
    for {
      _ <- Gen.int(Range.linear(0, 10)).list(Range.linear(0, 10)).forAll
        .classify("empty", _.isEmpty)
        .classify("nonempty", _.nonEmpty)
    } yield Result.success

  def testLabelledExamples: Property = {
    for {
      examples <- Gen.generate { (size, seed) =>
        val config = PropertyConfig.default.copy(withExamples = WithExamples.WithExamples)
        val labelledExamples = Property.report(config, Some(size), seed, prop)
        Seed(seed.seed.next) -> labelledExamples.examples
      }.forAll
    } yield
      examples ==== Examples(Map(
        LabelName("empty") -> List(Info("List()"))
      , LabelName("nonempty") -> List(Info("List(0)"))
      ))
  }
}
