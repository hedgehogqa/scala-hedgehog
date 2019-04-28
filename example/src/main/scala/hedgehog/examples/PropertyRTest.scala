package hedgehog.examples

import hedgehog._
import hedgehog.Gen._
import hedgehog.runner._
import hedgehog.examples.PropertyTest._

object PropertyRTest extends Properties {

  def tests: List[Test] =
    List(
      property("example1", example1.property)
    , example("example1 example", example1.test(('x', 1)))
    , property("total", total.property)
    , example("total example", total.test((Order(Nil), Order(Nil))))
    )

  def example1: PropertyR[(Char, Int)] =
    PropertyR(forTupled(
      Gen.char('a', 'z').log("x")
    , int(Range.linear(0, 50)).lift
    )) {
      case (x, y) => Result.assert(y < 87 && x <= 'r')
    }

  def total: PropertyR[(Order, Order)] =
    PropertyR(forTupled(
      order(cheap).log("cheap")
    , order(expensive).log("expensive")
    )) {
      case (x, y) => merge(x, y).total.value ==== x.total.value + y.total.value
    }
}
