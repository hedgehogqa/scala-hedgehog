package hedgehog.runner

import hedgehog._
import hedgehog.core._

abstract class Properties {

  def tests: List[Prop]

  /** Allows the implementing test to be run separately without SBT */
  def main(args: Array[String]): Unit = {
    val config = PropertyConfig.default
    val seed = Seed.fromTime()
    tests.foreach(t => {
      val report = Property.check(t.withConfig(config), t.result, seed).value
      println(Prop.renderReport(this.getClass.getName, t, report, ansiCodesSupported = true))
    })
  }
}

class Prop(
    val name: String
  , val withConfig: PropertyConfig => PropertyConfig
  , val result: Property
  ) {

  def config(f: PropertyConfig => PropertyConfig): Prop =
    new Prop(name, c => f(withConfig(c)), result)
}

object Prop {

  /** Wrap the actual constructor so we can catch any exceptions thrown */
  def apply(name: String, result: => Property): Prop =
    try {
      new Prop(name, identity, result)
    } catch {
      case e: Exception =>
        new Prop(name, identity, Property.error(e))
    }

  def renderReport(className: String, t: Prop, report: Report, ansiCodesSupported: Boolean): String = {
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

    report.status match {
      case Failed(shrinks, log) =>
        render(false, s"Falsified after ${report.tests.value} passed tests", log.map(renderLog))
      case GaveUp =>
        render(false, s"Gave up after only ${report.tests.value} passed test. " +
          s"${report.discards.value} were discarded", Nil)
      case OK =>
        render(true, s"OK, passed ${report.tests.value} tests", Nil)
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
}
