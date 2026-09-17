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
    val qualifiedName = className + "." + t.name

    def render(ok: Boolean, msg: String, extraS: List[String]): String = {
      val sym = if (ok) "+" else "-"
      val colour = if (ok) Console.GREEN else Console.RED
      /* Split on newlines so that a log entry which renders as more than one line - an `Error`'s
       * stack trace - has every line prefixed rather than only its first. `-1` keeps trailing
       * empty segments, so an entry which renders as "" still produces a "> " line, exactly as it
       * did before.
       */
      val extra =
        if (extraS.isEmpty) ""
        else "\n" + extraS.flatMap(_.split("\n", -1).toList).map(s => "> " + s).mkString("\n")
      if(ansiCodesSupported) {
        s"$colour$sym${Console.RESET} $qualifiedName: $msg$extra"
      } else {
        s"$sym $qualifiedName: $msg$extra"
      }
    }

    val coverage = renderCoverage(report.coverage, report.tests, report.examples)
    report.status match {
      case Failed(shrinks, log) =>
        render(
          false
        , s"Falsified after ${report.tests.value} passed tests"
        , log.flatMap(l => renderFailureLog(qualifiedName, l)) ++ coverage
        )
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
      /* A type pattern rather than `SourceLocation(pos)`: the exhaustivity checker
       * cannot see through a hand-written `unapply`.
       *
       * The location on its own, without the navigation frame which a failure report also carries.
       * See `renderFailureLog` for that form.
       */
      case l: SourceLocation =>
        l.pos.relativePath + ":" + l.pos.line.toString
      case Error(e) =>
        val sw = new java.io.StringWriter()
        e.printStackTrace(new java.io.PrintWriter(sw))
        sw.toString
    }

  /**
   * The lines a single log entry contributes to a failure report.
   *
   * A `SourceLocation` becomes two: the location as `renderLog` renders it, then a line shaped like
   * a stack frame. That second line is not a real frame - it is synthesised from the same captured
   * location - but IntelliJ IDEA's `ExceptionFilter` recognises the shape and resolves the file
   * through the project index rather than the filesystem, which is what makes the failure navigable
   * there. The first line is what iTerm2 linkifies, and the build-root-relative path is not
   * linkified by the sbt shell at all, so both are needed to cover both tools.
   *
   * `qualifiedTestName` is the suite's class name joined to the test's name. It is the suite's, so
   * an assertion written in a shared helper file pairs this suite's class name with that helper's
   * file name. IntelliJ resolves the file by name, so the link still lands.
   *
   * Any other log entry becomes the one line `renderLog` gives it.
   */
  def renderFailureLog(qualifiedTestName: String, log: Log): List[String] =
    log match {
      /* A type pattern, and a default case, for the same reason as in `renderLog`. */
      case l: SourceLocation =>
        List(
          renderLog(l)
        , "at " + qualifiedTestName + "(" + l.pos.fileName + ":" + l.pos.line.toString + ")"
        )
      case _ =>
        List(renderLog(log))
    }

  def renderCoverage(coverage: Coverage[CoverCount], tests: SuccessCount, examples: Examples): List[String] =
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
        , renderExample(examples, l.name)
        ).flatten.mkString(" ")
      })

  def renderExample(examples: Examples, name: LabelName): List[String] =
    examples.examples.getOrElse(name, Nil).map(renderLog) match {
      case Nil =>
        Nil
      case x :: Nil =>
        if (x == name.render) // i.e. `.collect`
          Nil
        else
          List(x)
      case xs =>
        List(xs.mkString(", "))
    }
}
