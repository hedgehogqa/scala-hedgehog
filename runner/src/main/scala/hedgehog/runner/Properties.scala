package hedgehog.runner

import hedgehog._

abstract class Properties {

  def tests: List[Prop]
}

case class Prop(name: String, result: Property[Unit])
