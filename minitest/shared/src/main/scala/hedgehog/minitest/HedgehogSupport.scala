package hedgehog.minitest

import minitest.SimpleTestSuite
import hedgehog.runner.{SeedSource, Test}
import hedgehog.core.{PropertyConfig, Seed, Status}
import hedgehog.{Property, Result}

trait HedgehogSupport { self: SimpleTestSuite =>
  private val seedSource = SeedSource.fromEnvOrTime()
  private val seed: Seed = Seed.fromLong(seedSource.seed)

  def property(name: String, config: PropertyConfig = PropertyConfig.default)(
      prop: => Property
  ): Unit = {
    val t = hedgehog.runner.property(name, prop)
    test(name)(check(t, config))
  }

  def example(name: String, config: PropertyConfig = PropertyConfig.default)(
      result: => Result
  ): Unit = {
    val t = hedgehog.runner.example(name, result)
    test(name)(check(t, config))
  }

  private def check(test: Test, config: PropertyConfig): Unit = {
    val report = Property.check(config, test.result, seed)
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
