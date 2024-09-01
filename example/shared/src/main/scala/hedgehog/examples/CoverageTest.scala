package hedgehog.examples

import hedgehog._
import hedgehog.runner._

object CoverageTest extends Properties {

  override def tests: List[Test] =
    List(
      property("collect", testCollect)
      , property("collect 2", testCollect2)
      , property("classify", testClassify)
      , property("classify 2", testClassify2)
      , property("cover number (expected to fail)", testCoverNumber)
      , property("cover number 2 (expected to fail)", testCoverNumber2)
      , property("cover boolean", testCoverBoolean)
      , property("cover boolean 2", testCoverBoolean2)
    )

  def testCollect: Property =
    for {
      _ <- Gen.int(Range.linear(1, 10)).forAll.collect
    } yield Result.success

  def testCollect2: Property =
    for {
      _ <- Gen.int(Range.linear(1, 10)).log("n").collect
      _ <- Gen.string(Gen.char('a', 'c'), Range.singleton(1)).log("s").collect
    } yield Result.success

  def testClassify: Property =
    for {
      _ <- Gen.int(Range.linear(1, 100)).forAll
        .classify("small number", _ < 50)
        .classify("large number", _ >= 50)
    } yield Result.success

  def testClassify2: Property =
    for {
      _ <- Gen.int(Range.linear(1, 100)).log("n")
        .classify("small number", _ < 50)
        .classify("large number", _ >= 50)
      _ <- Gen.int(Range.linear(1, 100)).log("m")
    } yield Result.success

  def testCoverNumber: Property =
    for {
      _ <- Gen.int(Range.linear(1, 100)).forAll
        .cover(50, "small number", _ < 10)
        .cover(15, "medium number", _ >= 20)
        .cover(5, "big number", _ >= 70)
    } yield Result.success

  def testCoverNumber2: Property =
    for {
      _ <- Gen.int(Range.linear(1, 100)).log("n")
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

  def testCoverBoolean2: Property =
    for {
      _ <- Gen.boolean.log("b")
        .cover(30, "true", x => x)
        .cover(30, "false", x => !x)
    } yield Result.success
}
