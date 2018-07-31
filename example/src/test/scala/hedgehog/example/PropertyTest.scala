package hedgehog

import hedgehog.Gen._
import hedgehog.Property._
import hedgehog.runner._

class PropertyTest extends Properties {

  def tests: List[Prop] =
    List(
      Prop("example1", example1)
    , Prop("total", total)
    )

  def example1: Property[Unit] =
    for {
      x <- Gen.char('a', 'z').log("x")
      y <- integral(Range.linear(0, 50)).log("y")
      _ <- if (y % 2 == 0) discard else success
      _ <- assert(y < 87 && x <= 'r')
    } yield ()

  def total: Property[Unit] =
    for {
      x <- order(cheap).log("cheap")
      y <- order(expensive).log("expensive")
      _ <- assert(merge(x, y).total.value == x.total.value + y.total.value)
    } yield ()

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
      p <- integral[Long](Range.constant(5, 10)).map(USD)
    } yield Item(n, p)

  def expensive: Gen[Item] =
    for {
      n <- element("oculus", List("vive"))
      p <- integral[Long](Range.linear(1000, 2000)).map(USD)
    } yield Item(n, p)

  def order(gen: Gen[Item]): Gen[Order] =
    gen.list(Range.linear(0, 50)).map(Order)

}
