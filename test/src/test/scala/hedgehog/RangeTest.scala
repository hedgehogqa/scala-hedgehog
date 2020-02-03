package hedgehog

import hedgehog.runner._

object RangeTest extends Properties {

  def tests: List[Test] =
    List(
      example("integral", testIntegral)
    , example("double", testFractional)
    , example("long", testLong)
    , property("double should not generate values outside of the range", testDoubleWithinRange).withTests(1000)
    )

  def testIntegral: Result = {
    val r1 = Range.linear(Int.MinValue, Int.MaxValue)
    val r2 = Range.linear(-1, Int.MaxValue)
    Result.all(List(
      r1.bounds(Size(1))._2 ==== -2104533976
    , r1.bounds(Size(25))._2 ==== -1073741825
    , r1.bounds(Size(75))._2 ==== 1073741823
    , r1.bounds(Size(99))._2 ==== 2104533974
    , r1.bounds(Size(100))._2 ==== 2147483647
    , r2.bounds(Size(1))._2 ==== 21474835
    , r2.bounds(Size(25))._2 ==== 536870911
    , r2.bounds(Size(75))._2 ==== 1610612735
    , r2.bounds(Size(99))._2 ==== 2126008810
    , r2.bounds(Size(100))._2 ==== 2147483647
    ))
  }

  def testLong: Result = {
    val r1 = Range.linear(Long.MinValue, Long.MaxValue)
    val r2 = Range.linear(1, Long.MaxValue)
    Result.all(List(
      r1.bounds(Size(1))._2 ==== -9038904596117680292L
      , r1.bounds(Size(25))._2 ==== -4611686018427387905L
      , r1.bounds(Size(75))._2 ==== 4611686018427387903L
      , r1.bounds(Size(99))._2 ==== 9038904596117680290L
      , r1.bounds(Size(100))._2 ==== Long.MaxValue
      , r2.bounds(Size(1))._2 ==== 92233720368547759L
      , r2.bounds(Size(25))._2 ==== 2305843009213693952L
      , r2.bounds(Size(75))._2 ==== 6917529027641081855L
      , r2.bounds(Size(99))._2 ==== 9131138316486228048L
      , r2.bounds(Size(100))._2 ==== Long.MaxValue
    ))
  }

  def testFractional: Result = {
    val r1 = Range.linearFrac(Double.MinValue, Double.MaxValue)
    Result.all(List(
      r1.bounds(Size(1))._2 ==== -1.7617392721650694E308
    , r1.bounds(Size(25))._2 ==== -8.988465674311579E307
    , r1.bounds(Size(75))._2 ==== 8.988465674311579E307
    , r1.bounds(Size(99))._2 ==== 1.7617392721650694E308
    , r1.bounds(Size(100))._2 ==== 1.7976931348623157E308
    ))
  }

  def testDoubleWithinRange: Property =
    for {
      d <- Gen.double(Range.linearFracFrom(0, Double.MinValue, Double.MaxValue)).forAll
    } yield
      Result.all(List(
        Result.assert(!d.isInfinite).log("infinite")
      , Result.assert(d >= Double.MinValue).log("min_value")
      , Result.assert(d <= Double.MaxValue).log("max_value")
      ))
}
