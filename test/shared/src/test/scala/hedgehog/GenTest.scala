package hedgehog

import hedgehog.core._
import hedgehog.runner._

import scala.annotation.tailrec
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
    , example("list is stack safe", testListStackSafe)
    , example("string is stack safe", testStringStackSafe)
    , example("bytes is stack safe", testBytesStackSafe)
    , property("list matches replicateM", testListMatchesReplicateM).withTests(200)
    , example("list shrinking of a large list", testListShrinkLarge)
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
    r ==== Report(SuccessCount(100), DiscardCount(0), Coverage.empty, Examples.empty, OK)
  }

  def testFromSomeNone: Result = {
    val r = Property.checkRandom(PropertyConfig.default, Gen.fromSome(Gen.constant(Option.empty[Result])).forAll)
    r ==== Report(SuccessCount(0), DiscardCount(100), Coverage.empty, Examples.empty, GaveUp)
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

  def testListStackSafe: Result = {
    val n = 100000
    val r = Gen.list(Gen.int(Range.linear(0, 100)), Range.singleton(n)).run(Size(100), Seed.fromLong(0))
    r.value._2.map(_.length) ==== Some(n)
  }

  def testStringStackSafe: Result = {
    val n = 100000
    val r = Gen.string(Gen.alpha, Range.singleton(n)).run(Size(100), Seed.fromLong(0))
    r.value._2.map(_.length) ==== Some(n)
  }

  def testBytesStackSafe: Result = {
    val n = 100000
    val r = Gen.bytes(Range.singleton(n)).run(Size(100), Seed.fromLong(0))
    r.value._2.map(_.length) ==== Some(n)
  }

  def testListMatchesReplicateM: Property =
    for {
      n <- Gen.int(Range.linear(0, 6)).log("n")
      seed <- Gen.long(Range.linearFrom(0, Long.MinValue, Long.MaxValue)).log("seed")
      size <- Gen.int(Range.linear(1, 100)).log("size")
    } yield {
      val g = Gen.int(Range.linear(0, 5)).ensure(_ != 3)
      def flatten(t: Tree[(Seed, Option[List[Int]])]) =
        TTree.fromTree(4, 20, t)
      val expected = predef.replicateM[GenT, Int](n, g).run(Size(size), Seed.fromLong(seed))
      val actual = GenT.replicate(n, g).run(Size(size), Seed.fromLong(seed))
      flatten(actual) ==== flatten(expected)
    }

  def testListShrinkLarge: Result = {
    val t = Gen.list(Gen.constant(0), Range.constant(0, 100000)).run(Size(100), Seed.fromLong(0))
    // Greedy shrink, as the property runner does: take the first child that still fails.
    def fails(x: (Seed, Option[List[Int]])): Boolean =
      x._2.exists(_.length >= 1000)
    @tailrec
    def shrink(t: Tree[(Seed, Option[List[Int]])]): Tree[(Seed, Option[List[Int]])] =
      predef.findMap(t.children.value)(c => if (fails(c.value)) Some(c) else None) match {
        case None => t
        case Some(c) => shrink(c)
      }
    val start = t.value._2.map(_.length).getOrElse(0)
    Result.assert(start > 10000).log("start: " + start) and
      shrink(t).value._2.map(_.length) ==== Some(1000)
  }
}
