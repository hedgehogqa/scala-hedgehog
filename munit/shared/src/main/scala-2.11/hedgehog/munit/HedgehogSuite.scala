package hedgehog
package munit

import hedgehog.core.PropertyConfig
import hedgehog.core.Seed
import hedgehog.core.Status
import hedgehog.{runner => hr}
import _root_.munit.FunSuite
import _root_.munit.Location

abstract class HedgehogSuite extends FunSuite with HedgehogAssertions {

  private val seedSource = hr.SeedSource.fromEnvOrTime()

  private val seed: Seed = Seed.fromLong(seedSource.seed)

  /** Runs a hedgehog property-based test.
    *
    * @see
    *   hedgehog.runner.property
    * @param name
    * @param withConfig
    *   A function with which to change the test PropertyConfig
    * @param prop
    *   The property under test
    * @param loc
    *   The location in the test suite source file
    */
  def property(
      name: String,
      withConfig: PropertyConfig => PropertyConfig = identity
  )(
      prop: => Property
  )(implicit loc: Location): Unit = {
    val t = hedgehog.runner.property(name, prop).config(withConfig)
    test(name)(check(t, t.withConfig(PropertyConfig.default)))
  }

  private def check(test: hr.Test, config: PropertyConfig)(implicit
      loc: Location
  ): Any = {
    val report = Property.check(test.withConfig(config), test.result, seed)
    if (report.status != Status.ok) {
      val reason = hr.Test.renderReport(
        this.getClass.getName,
        test,
        report,
        ansiCodesSupported = true
      )
      withMunitAssertions(assertions =>
        assertions.fail(s"$reason\n${seedSource.renderLog}")
      )
    }
  }
}
