package hedgehog

import hedgehog.core._
import hedgehog.runner._

object ShrinkTest extends Properties {

  def tests: List[Test] =
    List(
      example("test that shrinking only 'runs' the test once per shrink", testLazy)
    )

  // https://github.com/hedgehogqa/scala-hedgehog/issues/66
  def testLazy: Result = {
    // What's that, mutable state?!?
    // We really want to observe how many times our test is _actually_ run, not just what hedgehog thinks it ran.
    // In previous incarnations we were accidentally running the test for _each_ shrink,
    // but only taking the first failed result. For any non-trivial test (ie IO test) this would basically make
    // shrinking useless.
    var failed = 0

    val r = Property.check(PropertyConfig.default, for {
      // NOTE: We're also generating lists-of-lists here at the same time
      // If implemented too strictly the shrinking _will_ run out of memory
      // https://github.com/hedgehogqa/scala-hedgehog/issues/62
      x <- Gen.string(Gen.alpha, Range.linear(0, 100)).list(Range.linear(0, 100)).log("x")
    } yield {
      val b = x.length < 5
      if (!b) {
        failed = failed + 1
      }
      Result.assert(b)
    }, Seed.fromTime())

    r.status match {
      case Failed(s, _) =>
        // This count also includes the first failure case
        ShrinkCount(failed - 1) ==== s
      case _ =>
        Result.failure.log("Test failed incorrectly")
    }
  }
}
