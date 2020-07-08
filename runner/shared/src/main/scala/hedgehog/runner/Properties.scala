package hedgehog.runner

import hedgehog._
import hedgehog.core._
import org.portablescala.reflect.annotation.EnableReflectiveInstantiation

@EnableReflectiveInstantiation
abstract class Properties {

  def tests: List[Test]

  /** Allows the implementing test to be run separately without SBT */
  def main(args: Array[String]): Unit = {
    val config = PropertyConfig.default
    val seedSource = SeedSource.fromEnvOrTime()
    val seed = Seed.fromLong(seedSource.seed)
    println(seedSource.renderLog)
    tests.foreach(t => {
      val report = Property.check(t.withConfig(config), t.result, seed)
      println(Test.renderReport(this.getClass.getName, t, report, ansiCodesSupported = true))
    })
  }
}

class Test(
    val name: String
  , val withConfig: PropertyConfig => PropertyConfig
  , val result: Property
  ) {

  def config(f: PropertyConfig => PropertyConfig): Test =
    new Test(name, c => f(withConfig(c)), result)

  def withTests(count: SuccessCount): Test =
    config(_.copy(testLimit = count))

  def noShrinking: Test =
    config(_.copy(shrinkLimit = ShrinkLimit(0)))
}

object Test {

  /** Wrap the actual constructor so we can catch any exceptions thrown */
  def apply(name: String, result: => Property): Test =
    try {
      new Test(name, identity, result)
    } catch {
      case e: Exception =>
        new Test(name, identity, Property.error(e))
    }

  def renderReport(className: String, t: Test, report: Report, ansiCodesSupported: Boolean): String = {
    def render(ok: Boolean, msg: String, extraS: List[String]): String = {
      val name = className + "." + t.name
      val sym = if (ok) "+" else "-"
      val colour = if (ok) Console.GREEN else Console.RED
      val extra = if (extraS.isEmpty) "" else "\n" + extraS.map(s => "> " + s).mkString("\n")
      if(ansiCodesSupported) {
        s"$colour$sym${Console.RESET} $name: $msg$extra"
      } else {
        s"$sym $name: $msg$extra"
      }
    }

    val coverage = renderCoverage(report.coverage, report.tests)
    report.status match {
      case Failed(shrinks, log) =>
        render(false, s"Falsified after ${report.tests.value} passed tests", log.map(renderLog) ++ coverage)
      case GaveUp =>
        render(false, s"Gave up after only ${report.tests.value} passed test. " +
          s"${report.discards.value} were discarded", coverage)
      case OK =>
        render(true, s"OK, passed ${report.tests.value} tests", coverage)
    }
  }

  def renderLog(log: Log): String =
    log match {
      case ForAll(name, value) =>
        s"${name.value}: $value"
      case Info(value) =>
        value
      case Error(e) =>
        val sw = new java.io.StringWriter()
        e.printStackTrace(new java.io.PrintWriter(sw))
        sw.toString
    }

  def renderCoverage(coverage: Coverage[CoverCount], tests: SuccessCount): List[String] =
    coverage.labels.values.toList
      .sortBy(_.annotation.percentage(tests).toDouble.toInt * -1)
      .map(l => {
        List(
          List(l.annotation.percentage(tests).toDouble.toInt.toString + "%")
        , List(l.name.render)
        , if (l.minimum.toDouble > 0) List(
            l.minimum.toDouble.toInt.toString + "%"
          , if (Label.covered(l, tests)) "✓" else "✗"
          ) else Nil
        ).flatten.mkString(" ")
      })
}
