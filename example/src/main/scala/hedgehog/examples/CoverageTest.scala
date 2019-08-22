package hedgehog.examples

import hedgehog._
import hedgehog.runner._

object CoverageTest extends Properties {

  override def tests: List[Test] =
    List(
      property("collect", testCollect)
    , property("classify", testClassify)
    , property("cover number (expected to fail)", testCoverNumber)
    , property("cover boolean", testCoverBoolean)
    )

  def testCollect: Property =
    for {
      _ <- Gen.int(Range.linear(1, 10)).forAll.collect
    } yield Result.success

  def testClassify: Property =
    for {
      _ <- Gen.int(Range.linear(1, 100)).forAll
        .classify("small number", _ < 50)
        .classify("large number", _ >= 50)
    } yield Result.success

  def testCoverNumber: Property =
    for {
      _ <- Gen.int(Range.linear(1, 100)).forAll
        .cover(50, "small number", _ < 10)
        .cover(15, "medium number", _ >= 20)
        .cover(5, "big number", _ >= 70)
    } yield Result.success

  def testCoverBoolean: Property =
    for {
      _ <- Gen.boolean.forAll
        .cover(30, "true", x => x)
        .cover(30, "false", x => !x)
    } yield Result.success
}
