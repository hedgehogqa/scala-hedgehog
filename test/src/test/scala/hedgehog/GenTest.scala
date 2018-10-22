package hedgehog

import hedgehog.core._
import hedgehog.runner._

object GenTest extends Properties {

  def tests: List[Prop] =
    List(
      Prop("long generates correctly", testLong)
    , Prop("frequency is random", testFrequency)
    , Prop("fromSome some", testFromSomeSome)
    , Prop("fromSome none", testFromSomeNone)
    )

  def testLong: Property = {
    for {
      l <- Gen.long(Range.linear(Long.MaxValue / 2, Long.MaxValue)).forAll
      _ <- Property.assert(l >= Long.MaxValue / 2)
    } yield ()
  }

  def testFrequency: Property = {
    val g = Gen.frequency1((1, Gen.constant("a")), (1, Gen.constant("b")))
    val r1 = g.run(Size(1), Seed.fromLong(3)).run.value.value._2
    val r2 = g.run(Size(1), Seed.fromLong(1)).run.value.value._2
    for {
      _ <- r1 ==== Some("a")
      _ <- r2 ==== Some("b")
    } yield ()
  }

  def testFromSomeSome: Property = {
    val r = Gen.fromSome(Gen.constant(()).option).forAll.checkRandom.value
    r ==== Report(SuccessCount(100), DiscardCount(0), OK)
  }

  def testFromSomeNone: Property = {
    val r = Gen.fromSome(Gen.constant(Option.empty[Unit])).forAll.checkRandom.value
    r ==== Report(SuccessCount(0), DiscardCount(100), GaveUp)
  }
}
