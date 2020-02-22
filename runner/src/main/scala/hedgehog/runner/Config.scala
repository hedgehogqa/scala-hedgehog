package hedgehog.runner

import hedgehog.core.PropertyConfig

final case class Config(testNames: List[String], seed: Long, propertyConfig: PropertyConfig)

object Config {

  def default: Config = Config(Nil, System.nanoTime(), PropertyConfig.default)
}
