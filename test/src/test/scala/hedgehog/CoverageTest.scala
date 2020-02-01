package hedgehog

import hedgehog.core._
import hedgehog.runner._

object CoverageTest extends Properties {

  override def tests: List[Test] =
    List(
      example("test cover passes", testCoverPass)
    , example("test cover fails", testCoverFail)
    , example("test cover fractional percentages", testCoverFractionalPercentages)
    )

  def testCoverPass: Result = {
    val g =
      for {
        _ <- Gen.boolean.forAll
          .cover(30, "true", x => x)
          .cover(30, "false", x => !x)
      } yield Result.success

    val r = Property.checkRandom(PropertyConfig.default, g)
    Result.all(List(
      r.coverage.labels.keys.toList.sortBy(_.render) ==== List(LabelName("false"), LabelName("true"))
    , r.status ==== Status.ok
    ))
  }

  def testCoverFail: Result = {
    val g =
      for {
        _ <- Gen.boolean.forAll
          .cover(70, "true", x => x)
          .cover(30, "false", x => !x)
      } yield Result.success

    val r = Property.checkRandom(PropertyConfig.default, g)
    Result.all(List(
      r.coverage.labels.keys.toList.sortBy(_.render) ==== List(LabelName("false"), LabelName("true"))
    , r.status match {
        case Failed(_, _) =>
          Result.success
        case s =>
          Result.failure.log(s.toString)
      }
    ))
  }

  // We generate a random number between 1 and 10,000.
  // The expected probability of getting 80 or lower is 80 / 10,000 or 1 / 125.
  // The expected probability of getting 80 or higher is 9,920 / 10,000 or 124 / 125.
  // If we run the test 10,000 times then we would expect:
  // <= 80, 80 times, and > 80, 9920 times.
  // Calculating the binomial proportion confidence intervals for 99.9999999999999% confidence:
  // None: 0.0063 to 0.0099
  // Some: 0.9901 to 0.9937
  // See https://statpages.info/confint.html#Binomial
  def testCoverFractionalPercentages: Result = {
    val g =
      for {
        _ <- Gen.int(Range.constant(1, 10000)).forAll
          .cover(0.0063, "<= 80", _ <= 80)
          .cover(0.9901, "> 80", _ > 80)
      } yield Result.success

    val r = Property.checkRandom(PropertyConfig.default.copy(testLimit = 10000), g)
    Result.all(List(
      r.coverage.labels.keys.toList.sortBy(_.render) ==== List(LabelName("<= 80"), LabelName("> 80"))
      , r.status ==== Status.ok
    ))
  }
}
