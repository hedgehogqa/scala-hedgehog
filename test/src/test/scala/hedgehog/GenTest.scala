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
    , example("applicative", testApplicative)
    , example("monad", testMonad)
    )

  def testLong: Property = {
    for {
      l <- Gen.long(Range.linear(Long.MaxValue / 2, Long.MaxValue)).forAll
    } yield Result.assert(l >= Long.MaxValue / 2)
  }

  def testFrequency: Result = {
    val g = Gen.frequency1((1, Gen.constant("a")), (1, Gen.constant("b")))
    val r1 = g.run(Size(1), Seed.fromLong(3)).value._2
    val r2 = g.run(Size(1), Seed.fromLong(1)).value._2

    r1 ==== Some("a") and r2 ==== Some("b")
  }

  def testFromSomeSome: Result = {
    val r = Property.checkRandom(PropertyConfig.default, Gen.fromSome(Gen.constant(Result.success).option).forAll)
    r ==== Report(SuccessCount(100), DiscardCount(0), Coverage.empty, OK)
  }

  def testFromSomeNone: Result = {
    val r = Property.checkRandom(PropertyConfig.default, Gen.fromSome(Gen.constant(Option.empty[Result])).forAll)
    r ==== Report(SuccessCount(0), DiscardCount(100), Coverage.empty, GaveUp)
  }

  def testApplicative: Result = {
    val r = TTree.fromTree(100, 100, forTupled(
      Gen.int(Range.linear(0, 1))
    , Gen.int(Range.linear(0, 1))
    ).run(Size(100), Seed.fromLong(0)).map(_._2.orNull))
    r ==== TTree((1, 1), List(
        TTree((0, 1), List(TTree((0, 0), List())))
      , TTree((1, 0), List(TTree((0, 0), List())))
      ))
  }

  def testMonad: Result = {
    val r = TTree.fromTree(100, 100, (for {
      x <- Gen.int(Range.linear(0, 1))
      y <- Gen.int(Range.linear(0, 1))
    } yield (x, y)).run(Size(100), Seed.fromLong(0)).map(_._2.orNull))
    r ==== TTree((1, 1), List(
        TTree((0, 1), List(TTree((0, 0), List())))
      , TTree((1, 0), List())
      ))
  }
}
