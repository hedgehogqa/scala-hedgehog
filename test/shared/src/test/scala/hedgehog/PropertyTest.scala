package hedgehog

import hedgehog.core._
import hedgehog.Gen._
import hedgehog.runner._

object PropertyTest extends Properties {

  def tests: List[Test] =
    List(
      example("example1", example1)
    , example("applicative", testApplicative)
    , property("applicative shrink", testApplicativeShrinking)
    , example("monad shrinking", testMonadShrinking)
    , example("total", total)
    , example("fail", fail)
    , property("matchPatternExample", matchPatternExample)
    , property("matchMoreThanOnePatternExample", matchMoreThanOnePatternExample)
    , property("matchShouldFailureForNonMatchingCaseExample", matchShouldFailureForNonMatchingCaseExample)
    )

  def example1: Result = {
    val seed = Seed.fromLong(5489)
    val r = Property.check(PropertyConfig.default, for {
      x <- Gen.char('a', 'z').log("x")
      y <- int(Range.linear(0, 50)).log("y")
      _ <- if (y % 2 == 0) Property.discard else Property.point(())
    } yield Result.assert(y < 87 && x <= 'r'), seed)
    r ==== Report(SuccessCount(2), DiscardCount(4), Coverage.empty, Examples.empty, Failed(ShrinkCount(2), List(
        ForAll("x", "s")
      , ForAll("y", "1"))
      ))
  }

  def testApplicative: Result = {
    val seed = Seed.fromLong(5489)
    val r = Property.check(PropertyConfig.default, forTupled(
      Gen.char('a', 'z').log("x")
    , Gen.int(Range.linear(0, 50)).log("y")
    )
    .flatMap { case (x, y) =>
      (if (y % 2 == 0) Property.discard else Property.point(())).map(_ =>
      Result.assert(y < 87 && x <= 'r')
    )}, seed)
    r ==== Report(SuccessCount(2), DiscardCount(4), Coverage.empty, Examples.empty, Failed(ShrinkCount(2), List(
        ForAll("x", "s")
      , ForAll("y", "1"))
      ))
  }

  def testApplicativeShrinking: Property = {
    for {
      l <- Gen.long(Range.linearFrom(0L, Long.MinValue, Long.MaxValue)).forAll
    } yield {
      val seed = Seed.fromLong(l)
      val r = Property.report(PropertyConfig.default, Some(Size(100)), seed, forTupled(
        Gen.int(Range.linear(0, 10)).log("x")
      , Gen.int(Range.linear(0, 10)).log("y")
      ).map { case (x, y) => Result.assert(x < y) })
      statusLog(r.status) ==== List(ForAll("x", "0"), ForAll("y", "0"))
    }
  }

  def testMonadShrinking: Result = {
    // This is one example where using a monad we don't find the optimal shrink (like we do with applicative)
    val seed = Seed.fromLong(17418018500145L)
    val r = Property.report(PropertyConfig.default, Some(Size(100)), seed, for {
      x <- Gen.int(Range.linear(0, 10)).log("x")
      y <- Gen.int(Range.linear(0, 10)).log("y")
    } yield Result.assert(x < y))
    statusLog(r.status) ==== List(ForAll("x", "7"), ForAll("y", "0"))
  }

  case class USD(value: Long)
  case class Item(name: String, price: USD)
  case class Order(items: List[Item]) {

    def total: USD =
      USD(items.map(_.price.value).sum)
  }

  def merge(xs: Order, ys: Order): Order = {
    val extra =
      if (xs.items.exists(_.price.value > 50) || ys.items.exists(_.price.value > 50) )
        List(Item("processing", USD(1)))
      else
        Nil
    Order(xs.items ++ ys.items ++ extra)
  }

  def cheap: Gen[Item] =
    for {
      n <- element("sandwich", List("noodles"))
      p <- long(Range.constant(5, 10)).map(USD.apply)
    } yield Item(n, p)

  def expensive: Gen[Item] =
    for {
      n <- element("oculus", List("vive"))
      p <- long(Range.linear(1000, 2000)).map(USD.apply)
    } yield Item(n, p)

  def order(gen: Gen[Item]): Gen[Order] =
    gen.list(Range.linear(0, 50)).map(Order.apply)

  def total: Result = {
    val seed = Seed.fromLong(5489)
    val r = Property.check(PropertyConfig.default, for {
      x <- order(cheap).log("cheap")
      y <- order(expensive).log("expensive")
    } yield Result.assert(merge(x, y).total.value == x.total.value + y.total.value)
      , seed)
    r ==== Report(SuccessCount(1), DiscardCount(0), Coverage.empty, Examples.empty, Failed(ShrinkCount(4), List(
        ForAll("cheap", "Order(List())")
      , ForAll("expensive", "Order(List(Item(oculus,USD(1000))))"
      ))))
  }

  def fail: Result =
    Property.checkRandom(PropertyConfig.default, Property.point(Result.failure)).status ==== Failed(ShrinkCount(0), Nil)

  def statusLog(s: Status): List[Log]=
    s match {
      case f@Failed(_, _) =>
        f.log
      case _ =>
        Nil
    }

  def matchPatternExample: Property =
    for {
      x <- Gen.element1("abc").log("x")
      y <- Gen.integral[Long](Range.constant(5L, 10L), identity).map(USD.apply).lift
    } yield {
      Item(x, y) matchPattern { case Item("abc", _) => }
    }

  def matchMoreThanOnePatternExample: Property =
    for {
      x <- Gen.element1("abc", "bbb", "ccc").log("x")
      y <- Gen.integral[Long](Range.constant(5L, 10L), identity).map(USD.apply).log("y")
    } yield {
      Item(x, y) matchPattern {
        case Item("abc", USD(_)) =>
        case Item("bbb", USD(_)) =>
        case Item("ccc", USD(_)) =>
      }
    }

  def matchShouldFailureForNonMatchingCaseExample: Property =
    for {
      x <- Gen.element1("abc", "bbb", "ccc").log("x")
      y <- Gen.integral[Long](Range.constant(5L, 10L), identity).map(USD.apply).log("y")
    } yield {
      val result = Item(x, y) matchPattern {
        case Item("abc", USD(_)) =>
        case Item("ccc", USD(_)) =>
      }
      if (x == "bbb") {
        result ==== Result.failure
      } else {
        result ==== Result.success
      }
    }

}
