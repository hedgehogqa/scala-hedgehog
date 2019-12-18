package hedgehog

import hedgehog.core._
import hedgehog.runner._

import scala.util.Try

object GenTest extends Properties {

  def tests: List[Test] =
    List(
      property("long generates correctly", testLong)
    , property("withFilter filters values", testWithFilter)
    , example("frequency is random", testFrequency)
    , property("frequency handles large weights", testFrequencyLargeWeights).withTests(100000)
    , property("frequency fails if weight is non-positive", testFrequencyNonPositiveWeight)
    , example("fromSome some", testFromSomeSome)
    , example("fromSome none", testFromSomeNone)
    , example("applicative", testApplicative)
    , example("monad", testMonad)
    , example("withFilter is lazy", testWithFilterIsLazy)
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

  def testFrequencyLargeWeights: Property = {
    for {
      _ <- Gen.frequency1(
        (Int.MaxValue, Gen.constant(true)),
        (Int.MaxValue, Gen.constant(false))
      ).forAll
        .cover(CoverPercentage(45), LabelName("First generator"), Cover.Boolean2Cover)
        .cover(CoverPercentage(45), LabelName("Second generator"), b => Cover.Boolean2Cover(!b))
    } yield Result.success
  }

  def testFrequencyNonPositiveWeight: Property = {
    val genPositive = Gen.int(Range.linear(1, Int.MaxValue))
    val genNonPositive = Gen.int(Range.linear(0, Int.MinValue))
    for {
      positiveWeights <- Gen.list(genPositive, Range.linear(0, 10)).forAll
      nonPositiveWeight <- genNonPositive.forAll
      position <- Gen.int(Range.linear(0, positiveWeights.size)).forAll
    } yield {
      val weights = positiveWeights.take(position) ::: nonPositiveWeight :: positiveWeights.drop(position)
      val frequencies = weights.map(_ -> Gen.constant(true))
      val attempt = Try(Gen.frequency(
        frequencies.head,
        frequencies.tail
      ))
      val expected = new RuntimeException("Invariant: a non-positive weight was given")
      Result.diffNamed("=== Failed With ===", attempt, expected) {
        case (scala.util.Failure(ex), b) => ex.getMessage == b.getMessage
        case _ => false
      }
    }
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

  def testWithFilter: Property = {
    val genEvens = for {
      n <- Gen.int(Range.linear(0, 1))
      if n % 2 == 0
    } yield n
    for {
      n <- genEvens.forAll
    } yield n % 2 ==== 0
  }

  def testWithFilterIsLazy: Result = {
    val _ = Gen.constant("Better watch out...").withFilter { _ =>
      throw new IllegalStateException("I told you so!")
    }
    Result.success
  }
}
