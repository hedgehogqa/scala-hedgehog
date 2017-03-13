package hedgehog

import hedgehog.GenTree._
import hedgehog.Property._
import hedgehog.Property._
import org.scalacheck._
import org.scalacheck.Prop._
import scalaz._
import scalaz.effect._

object PropertyTest extends Properties("Property") {

  property("example1") = {
    val r = Property.check(5489)(for {
      x <- Property.forAll(GenTree.char[IO]('a', 'z'))
      y <- Property.forAll(integral[IO](0, 50))
      _ <- if (y % 2 == 0) discard[IO] else success[IO]
      _ <- assert[IO](y < 87 && x <= 'r')
    } yield ()).unsafePerformIO
    r ?= Report(4, 2, Failed(Shrinks(7), List("s", "3")))
  }

  case class Name(value: String)
  case class USD(value: Long)
  case class Item(name: Name, price: USD)
  case class Order(items: List[Item]) {

    def total: USD =
      USD(items.map(_.price.value).sum)
  }

  def merge(xs: Order, ys: Order): Order = {
    val extra =
      if (xs.items.exists(_.price.value > 50) || ys.items.exists(_.price.value > 50) )
        List(Item(Name("processing"), (USD(1))))
      else
        Nil
    Order(xs.items ++ ys.items ++ extra)
  }

  // TODO Use applicative here, but we need to fix the GenTree Applicative type inference
  def cheap[M[_]: Monad]: GenTree.T[M, Item] =
    for {
      n <- element[M, String]("sandwich", List("noodles")).map(Name)
      p <- integral[M](5, 10).map(USD)
    } yield Item(n, p)

  def expensive[M[_]: Monad]: GenTree.T[M, Item] =
    for {
      n <- element[M, String]("oculus", List("vive")).map(Name)
      p <- integral[M](1000, 2000).map(USD)
    } yield Item(n, p)

  def order[M[_]: Monad](gen: GenTree.T[M, Item]): GenTree.T[M, Order] =
    list(0, 50, gen).map(Order)

  property("total") = {
    val r = Property.check(5489)(for {
      x <- Property.forAll(order(cheap[IO]))
      y <- Property.forAll(order(expensive[IO]))
      _ <- assert[IO](merge(x, y).total.value == x.total.value + y.total.value)
    } yield ()).unsafePerformIO
    r ?= Report(0, 0, Failed(Shrinks(50), List("Order(List())", "Order(List(Item(Name(oculus),USD(1000))))")))
  }
}
