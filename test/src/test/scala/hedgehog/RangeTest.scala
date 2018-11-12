package hedgehog

import hedgehog.runner._

object RangeTest extends Properties {

  def tests: List[Test] =
    List(
      example("integral", testIntegral)
    , example("double", testFractional)
    )

  def testIntegral: Result = {
    val r1 = Range.linear(Int.MinValue, Int.MaxValue)
    val r2 = Range.linear(-1, Int.MaxValue)
    Result.all(List(
      r1.bounds(Size(1))._2 ==== -2104100140
    , r1.bounds(Size(25))._2 ==== -1062895948
    , r1.bounds(Size(75))._2 ==== 1106279454
    , r1.bounds(Size(99))._2 ==== 2147483647
    , r1.bounds(Size(100))._2 ==== 2147483647
    , r2.bounds(Size(1))._2 ==== 21691753
    , r2.bounds(Size(25))._2 ==== 542293849
    , r2.bounds(Size(75))._2 ==== 1626881550
    , r2.bounds(Size(99))._2 ==== 2147483647
    , r2.bounds(Size(100))._2 ==== 2147483647
    ))
  }

  def testFractional: Result = {
    val r1 = Range.linearFrac(Double.MinValue, Double.MaxValue)
    Result.all(List(
      r1.bounds(Size(1))._2 ==== -1.7613761018347941E308
    , r1.bounds(Size(25))._2 ==== -8.897673091742775E307
    , r1.bounds(Size(75))._2 ==== 9.26084342201799E307
    , r1.bounds(Size(99))._2 ==== 1.7976931348623157E308
    , r1.bounds(Size(100))._2 ==== 1.7976931348623157E308
    ))
  }
}
