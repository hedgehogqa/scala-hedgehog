package hedgehog.minitest

import minitest.SimpleTestSuite
import hedgehog.runner.{SeedSource, Test}
import hedgehog.core.{PropertyConfig, Seed, Status}
import hedgehog.{Property, Result}

trait HedgehogSupport { self: SimpleTestSuite =>
  private val seedSource = SeedSource.fromEnvOrTime()
  private val seed: Seed = Seed.fromLong(seedSource.seed)

  def property(name: String, withConfig: PropertyConfig => PropertyConfig = identity)(
      prop: => Property
  ): Unit = {
    val t = hedgehog.runner.property(name, prop).config(withConfig)
    test(name)(check(t, t.withConfig(PropertyConfig.default)))
  }

  def example(name: String, withConfig: PropertyConfig => PropertyConfig = identity)(
      result: => Result
  ): Unit = {
    val t = hedgehog.runner.example(name, result).config(withConfig)
    test(name)(check(t, t.withConfig(PropertyConfig.default)))
  }

  private def check(test: Test, config: PropertyConfig): Unit = {
    val report = Property.check(test.withConfig(config), test.result, seed)
    if (report.status != Status.ok) {
      val reason = Test.renderReport(
        this.getClass.getName,
        test,
        report,
        ansiCodesSupported = true
      )
      val reasonWithSeed = s"$reason\n${seedSource.renderLog}"
      fail(reasonWithSeed)
    }
  }
}
