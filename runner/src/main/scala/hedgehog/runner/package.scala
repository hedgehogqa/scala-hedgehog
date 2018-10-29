package hedgehog

import hedgehog.core._

package object runner {

  type Prop = Test
  val Prop = Test

  def property(name: String, result: => Property): Test =
    Test(name, result)

  def example(name: String, result: => Result): Test =
    Test(name, Property.point(result))
      // This is related to: https://github.com/hedgehogqa/scala-hedgehog/pull/35
      .config(_.copy(testLimit = SuccessCount(1)))
}
