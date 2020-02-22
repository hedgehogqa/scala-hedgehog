package hedgehog.runner

import hedgehog.Property
import hedgehog.core.Seed
import scopt.OParser

abstract class Properties {

  def tests: List[Test]

  /** Allows the implementing test to be run separately without SBT */
  def main(args: Array[String]): Unit = {
    OParser.parse(optionParser, args, Config.default) match {
      case Some(config) =>
        val tests = filteredTests(config)
        if (tests.isEmpty) {
          println("No tests to run.")
        } else {
          runTests(tests, config)
        }
      case _ =>
        System.err.println()
        sys.error("Failed to parse test arguments.")
    }
  }

  private def optionParser: OParser[_, Config] = {
    val builder = OParser.builder[Config]
    import builder._
    OParser.sequence(
      programName(this.getClass.getSimpleName.replaceAll("""\$$""", "")),
      opt[String]('n', "name")
        .action((name, config) => config.copy(testNames = name :: config.testNames))
        .text("Only runs tests with the given name. Specify multiple times to run multiple tests.")
        .unbounded(),
      opt[Long]('s', "seed")
        .action((seed, config) => config.copy(seed = seed))
        .text("Sets the seed value to use."),
      help('h', "help")
    )
  }

  private def filteredTests(config: Config): List[Test] =
    config.testNames match {
      case Nil => tests
      case names =>
        for {
          test <- tests
          name <- names
          if test.name == name
        } yield test
    }

  def runTests(tests: List[Test], config: Config): Unit = {
    val propertyConfig = config.propertyConfig
    val seed = config.seed
    println(s"Running tests (seed = $seed):")
    tests.foreach(t => {
      val report = Property.check(t.withConfig(propertyConfig), t.result, Seed.fromLong(seed))
      println(Test.renderReport(this.getClass.getName, t, report, ansiCodesSupported = true))
    })
  }
}
