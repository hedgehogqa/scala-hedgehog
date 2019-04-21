package hedgehog

import hedgehog.core._
import hedgehog.Gen._
import hedgehog.runner._

object PropertyTest extends Properties {

  def tests: List[Test] =
    List(
      example("example1", example1)
    , example("total", total)
    , example("fail", fail)
    )

  def example1: Result = {
    val seed = Seed.fromLong(5489)
    val r = Property.check(PropertyConfig.default, for {
      x <- Gen.char('a', 'z').log("x")
      y <- int(Range.linear(0, 50)).log("y")
      _ <- if (y % 2 == 0) Property.discard else Property.point(())
    } yield Result.assert(y < 87 && x <= 'r'), seed)
    r ==== Report(SuccessCount(2), DiscardCount(4), Failed(ShrinkCount(2), List(
        ForAll("x", "s")
      , ForAll("y", "1"))
      ))
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
      p <- long(Range.constant(5, 10)).map(USD)
    } yield Item(n, p)

  def expensive: Gen[Item] =
    for {
      n <- element("oculus", List("vive"))
      p <- long(Range.linear(1000, 2000)).map(USD)
    } yield Item(n, p)

  def order(gen: Gen[Item]): Gen[Order] =
    gen.list(Range.linear(0, 50)).map(Order)

  def total: Result = {
    val seed = Seed.fromLong(5489)
    val r = Property.check(PropertyConfig.default, for {
      x <- order(cheap).log("cheap")
      y <- order(expensive).log("expensive")
    } yield Result.assert(merge(x, y).total.value == x.total.value + y.total.value)
      , seed)
    r ==== Report(SuccessCount(1), DiscardCount(0), Failed(ShrinkCount(4), List(
        ForAll("cheap", "Order(List())")
      , ForAll("expensive", "Order(List(Item(oculus,USD(1000))))"
      ))))
  }

  def fail: Result =
    Property.checkRandom(PropertyConfig.default, Property.point(Result.failure)).status ==== Failed(ShrinkCount(0), Nil)
}
