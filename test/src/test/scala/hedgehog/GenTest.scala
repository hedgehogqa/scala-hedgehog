package hedgehog

import hedgehog.core._
import hedgehog.runner._

object GenTest extends Properties {

  def tests: List[Test] =
    List(
      property("long generates correctly", testLong)
    , example("frequency is random", testFrequency)
    , example("fromSome some", testFromSomeSome)
    , example("fromSome none", testFromSomeNone)
    )

  def testLong: Property = {
    for {
      l <- Gen.long(Range.linear(Long.MaxValue / 2, Long.MaxValue)).forAll
    } yield Result.assert(l >= Long.MaxValue / 2)
  }

  def testFrequency: Result = {
    val g = Gen.frequency1((1, Gen.constant("a")), (1, Gen.constant("b")))
    val r1 = g.run(Size(1), Seed.fromLong(3)).run.value._2
    val r2 = g.run(Size(1), Seed.fromLong(1)).run.value._2

    r1 ==== Some("a") and r2 ==== Some("b")
  }

  def testFromSomeSome: Result = {
    val r = Property.checkRandom(PropertyConfig.default, Gen.fromSome(Gen.constant(Result.success).option).forAll).value
    r ==== Report(SuccessCount(100), DiscardCount(0), OK)
  }

  def testFromSomeNone: Result = {
    val r = Property.checkRandom(PropertyConfig.default, Gen.fromSome(Gen.constant(Option.empty[Result])).forAll).value
    r ==== Report(SuccessCount(0), DiscardCount(100), GaveUp)
  }
}
