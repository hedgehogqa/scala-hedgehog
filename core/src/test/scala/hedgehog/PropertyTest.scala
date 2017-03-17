package hedgehog

import hedgehog.Gen._
import hedgehog.Property._
import org.scalacheck._
import org.scalacheck.Prop._
import scalaz._
import scalaz.effect._

object PropertyTest extends Properties("Property") {

  property("example1") = {
    val seed = Seed.fromLong(5489)
    val r = (for {
      x <- Gen.char[IO]('a', 'z').log("x")
      y <- integral[IO, Int](Range.linear(0, 50)).log("y")
      _ <- if (y % 2 == 0) Property.discard[IO] else success[IO]
      _ <- assert[IO](y < 87 && x <= 'r')
    } yield ()).check(seed).unsafePerformIO
    r ?= Report(SuccessCount(0), DiscardCount(2), Failed(ShrinkCount(1), List(
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
        List(Item("processing", (USD(1))))
      else
        Nil
    Order(xs.items ++ ys.items ++ extra)
  }

  def cheap[M[_]: Monad]: Gen[M, Item] =
    for {
      n <- element[M, String]("sandwich", List("noodles"))
      p <- integral[M, Long](Range.constant(5, 10)).map(USD)
    } yield Item(n, p)

  def expensive[M[_]: Monad]: Gen[M, Item] =
    for {
      n <- element[M, String]("oculus", List("vive"))
      p <- integral[M, Long](Range.linear(1000, 2000)).map(USD)
    } yield Item(n, p)

  def order[M[_]: Monad](gen: Gen[M, Item]): Gen[M, Order] =
    gen.list(Range.linear(0, 50)).map(Order)

  property("total") = {
    val seed = Seed.fromLong(5489)
    val r = (for {
      x <- order(cheap[IO]).log("cheap")
      y <- order(expensive[IO]).log("expensive")
      _ <- assert[IO](merge(x, y).total.value == x.total.value + y.total.value)
    } yield ()).check(seed).unsafePerformIO
    r ?= Report(SuccessCount(3), DiscardCount(0), Failed(ShrinkCount(5), List(
        ForAll("cheap", "Order(List())")
      , ForAll("expensive", "Order(List(Item(oculus,USD(1000))))"
      ))))
  }
}
