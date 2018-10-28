package hedgehog

import hedgehog.core._

package object runner {

  def property(name: String, result: => Property): Prop =
    Prop(name, result)

  def example(name: String, result: => Result): Prop =
    Prop(name, Property.point(result))
      // This is related to: https://github.com/hedgehogqa/scala-hedgehog/pull/35
      .config(_.copy(testLimit = SuccessCount(1)))
}
