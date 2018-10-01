package hedgehog.example

import hedgehog._
import hedgehog.Gen._
import hedgehog.Property._
import hedgehog.runner._

object PropertyTest extends Properties {

  def tests: List[Prop] =
    List(
      Prop("example1", example1)
    , Prop("total", total)
    , Prop("matchPatternExample", matchPatternExample)
    , Prop("matchMoreThanOnePatternExample", matchMoreThanOnePatternExample)
    )

  def example1: Property[Unit] =
    for {
      x <- Gen.char('a', 'z').log("x")
      y <- integral(Range.linear(0, 50)).lift
      _ <- if (y % 2 == 0) Property.discard else success
      _ <- assert(y < 87 && x <= 'r')
    } yield ()

  def total: Property[Unit] =
    for {
      x <- order(cheap).log("cheap")
      y <- order(expensive).log("expensive")
      _ <- merge(x, y).total.value === x.total.value + y.total.value
    } yield ()

  def matchPatternExample: Property[Unit] =
    for {
      x <- Gen.element1("abc").log("x")
      y <- integral[Long](Range.constant(5, 10)).map(USD).lift
      _ <- matchPattern(Item(x, y)) { case Item("abc", _) => }
    } yield ()

  def matchMoreThanOnePatternExample: Property[Unit] =
    for {
      x <- Gen.element1("abc", "bbb", "ccc").log("x")
      y <- integral[Long](Range.constant(5, 10)).map(USD).lift
      _ <- matchPattern(Item(x, y)) {
            case Item("abc", _) =>
            case Item("bbb", _) =>
          }
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
