package hedgehog

import hedgehog.core._
import hedgehog.runner._

object GenTest extends Properties {

  def tests: List[Prop] =
    List(
      Prop("long generates correctly", testLong)
    , Prop("frequency is random", testFrequency.property)
    , Prop("fromSome some", testFromSomeSome.property)
    , Prop("fromSome none", testFromSomeNone.property)
    )

  def testLong: Property = {
    for {
      l <- Gen.long(Range.linear(Long.MaxValue / 2, Long.MaxValue)).forAll
    } yield Result.assert(l >= Long.MaxValue / 2)
  }

  def testFrequency: Result = {
    val g = Gen.frequency1((1, Gen.constant("a")), (1, Gen.constant("b")))
    val r1 = g.run(Size(1), Seed.fromLong(3)).run.value.value._2
    val r2 = g.run(Size(1), Seed.fromLong(1)).run.value.value._2

    r1 ==== Some("a") and r2 ==== Some("b")
  }

  def testFromSomeSome: Result = {
    val r = Property.checkRandom(Gen.fromSome(Gen.constant(Result.success).option).forAll).value
    r ==== Report(SuccessCount(100), DiscardCount(0), OK)
  }

  def testFromSomeNone: Result = {
    val r = Property.checkRandom(Gen.fromSome(Gen.constant(Option.empty[Result])).forAll).value
    r ==== Report(SuccessCount(0), DiscardCount(100), GaveUp)
  }
}