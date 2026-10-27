package hedgehog

import hedgehog.core._
import hedgehog.runner._

object RenderTest extends Properties {

  override def tests: List[Test] =
    List(
      example("renderExample renders nothing when a label has no example logs", testRenderExampleNoLogs)
    , example("renderExample renders nothing when the single log equals the label name", testRenderExampleCollect)
    , property("property test renderExample renders nothing when the single log equals the label name", propertyTestRenderExampleCollect)
    , example("renderExample renders a single log as-is", testRenderExampleSingleLog)
    , property("property test renderExample renders a single log as-is", propertyTestRenderExampleSingleLog)
    , example("renderExample joins multiple logs with ', '", testRenderExampleMultipleLogs)
    , property("property test renderExample joins multiple logs with ', '", propertyTestRenderExampleMultipleLogs)
    , example("renderExample joins ForAll and Info logs in order with ', '", testRenderExampleMixedLogs)
    , property("property test renderExample joins ForAll and Info logs in order with ', '", propertyTestRenderExampleMixedLogs)
    , example("renderCoverage renders each label's example logs separated by ', '", testRenderCoverageMultipleLogs)
    , example("renderLog renders a SourceLocation as the relative path and line", testRenderLogSourceLocation)
    , example("renderFailureLog renders a SourceLocation as the relative path then a stack frame", testRenderFailureLogSourceLocation)
    , example("renderFailureLog wraps any other log in a single line", testRenderFailureLogOtherLog)
    , property("property test renderCoverage renders each label's example logs separated by ', '", propertyTestRenderCoverageMultipleLogs)
    , example("renderLog renders a ForAll as 'name: value'", testRenderLogForAll)
    , property("property test renderLog renders a ForAll as 'name: value'", propertyTestRenderLogForAll)
    , example("renderLog renders an Info as its value", testRenderLogInfo)
    , property("property test renderLog renders an Info as its value", propertyTestRenderLogInfo)
    , example("renderLog renders an Error as its stack trace", testRenderLogError)
    , example("renderReportBody renders OK with the number of passed tests", testRenderReportBodyOk)
    , property("property test renderReportBody renders OK with the number of passed tests", propertyTestRenderReportBodyOk)
    , example("renderReportBody renders OK with the coverage", testRenderReportBodyOkWithCoverage)
    , example("renderReportBody renders GaveUp with the passed and discarded tests", testRenderReportBodyGaveUp)
    , property("property test renderReportBody renders GaveUp with the passed and discarded tests", propertyTestRenderReportBodyGaveUp)
    , example("renderReportBody renders Failed with each log on its own prefixed line", testRenderReportBodyFailed)
    , example("renderReportBody renders Failed with the logs then the coverage", testRenderReportBodyFailedWithCoverage)
    , example("renderReportBody prefixes every line of an Error's stack trace", testRenderReportBodyFailedError)
    , example("renderReportBody renders an empty log as a '> ' line", testRenderReportBodyFailedEmptyLog)
    , example("renderReport renders OK with '+' and no ANSI codes", testRenderReportOkNoAnsi)
    , example("renderReport renders OK with a green '+' and ANSI codes", testRenderReportOkAnsi)
    , example("renderReport renders Failed with '-' and no ANSI codes", testRenderReportFailedNoAnsi)
    , example("renderReport renders Failed with a red '-' and ANSI codes", testRenderReportFailedAnsi)
    , example("renderReport renders GaveUp with '-' and no ANSI codes", testRenderReportGaveUpNoAnsi)
    , example("renderReport renders GaveUp with a red '-' and ANSI codes", testRenderReportGaveUpAnsi)
    , property("property test renderReport uses the class name and the test name as the qualified name", propertyTestRenderReportQualifiedName)
    , example("Test.apply keeps the name and the default config", testTestApply)
    , example("Test.apply turns an exception thrown by the property into an error", testTestApplyException)
    , example("Test.config applies to the default config", testTestConfig)
    , example("Test.config applies in the order it is called", testTestConfigOrder)
    , property("property test Test.withTests sets the test limit", propertyTestTestWithTests)
    , example("Test.withTests sets the number of tests that run", testTestWithTestsRun)
    , example("Test.noShrinking sets the shrink limit to 0", testTestNoShrinking)
    , example("Properties.main prints the seed and then the report of each test", testPropertiesMain)
    )

  def testRenderLogSourceLocation: Result =
    Test.renderLog(SourceLocation(SourcePos("b/Spec.scala", "Spec.scala", 17))) ====
      "b/Spec.scala:17"

  def testRenderFailureLogSourceLocation: Result =
    Test.renderFailureLog("pkg.Spec$.someTest", SourceLocation(SourcePos("b/Spec.scala", "Spec.scala", 17))) ====
      List("b/Spec.scala:17", "at pkg.Spec$.someTest(Spec.scala:17)")

  def testRenderFailureLogOtherLog: Result =
    Test.renderFailureLog("pkg.Spec$.someTest", Info("hello")) ==== List("hello")

  def testRenderExampleNoLogs: Result =
    Result.all(List(
      Test.renderExample(Examples.empty, LabelName("missing")) ==== Nil
    , Test.renderExample(Examples(Map(LabelName("empty-label") -> Nil)), LabelName("empty-label")) ==== Nil
    ))

  def testRenderExampleCollect: Result =
    Test.renderExample(Examples(Map(LabelName("42") -> List(Info("42")))), LabelName("42")) ==== Nil

  def propertyTestRenderExampleCollect: Property = for {
    label <- Gen.string(Gen.alphaNum, Range.linear(2, 10)).log("label")
  } yield {
    val expected = List.empty[String]
    val actual = Test.renderExample(Examples(Map(LabelName(label) -> List(Info(label)))), LabelName(label))
    actual ==== expected
  }

  def testRenderExampleSingleLog: Result =
    Test.renderExample(Examples(Map(LabelName("nonempty") -> List(Info("List(0)")))), LabelName("nonempty")) ==== List("List(0)")

  def propertyTestRenderExampleSingleLog: Property = for {
    label <- Gen.string(Gen.alphaNum, Range.linear(2, 5)).log("label")
    value <- Gen.string(Gen.alphaNum, Range.linear(6, 10)).log("value")
  } yield {
    val expected = List(s"List($value)")
    val actual = Test.renderExample(Examples(Map(LabelName(label) -> List(Info(s"List($value)")))), LabelName(label))

    actual ==== expected
  }

  def testRenderExampleMultipleLogs: Result = {
    val examples = Examples(Map(
      LabelName("small number") -> List(ForAll(Name("n"), "23"), ForAll(Name("m"), "87"))
    ))
    Test.renderExample(examples, LabelName("small number")) ==== List("n: 23, m: 87")
  }

  def propertyTestRenderExampleMultipleLogs: Property = for {
    label <- Gen.string(Gen.alphaNum, Range.linear(2, 5)).log("label")
    n <- Gen.int(Range.linear(0, 100)).map(_.toString).log("n")
    m <- Gen.int(Range.linear(0, 100)).map(_.toString).log("m")
  } yield {
    val examples = Examples(Map(
      LabelName(label) -> List(ForAll(Name("n"), n), ForAll(Name("m"), m))
    ))

    val expected = List(s"n: $n, m: $m")
    val actual = Test.renderExample(examples, LabelName(label))

    actual ==== expected
  }

  def testRenderExampleMixedLogs: Result = {
    val examples = Examples(Map(
      LabelName("mixed") -> List(ForAll(Name("n"), "23"), Info("extra info"), ForAll(Name("m"), "87"))
    ))
    Test.renderExample(examples, LabelName("mixed")) ==== List("n: 23, extra info, m: 87")
  }

  def propertyTestRenderExampleMixedLogs: Property = for {
    label <- Gen.string(Gen.alphaNum, Range.linear(2, 5)).log("label")
    n <- Gen.int(Range.linear(0, 100)).map(_.toString).log("n")
    extraInfo <- Gen.string(Gen.frequency1(9 -> Gen.alphaNum, 1 -> Gen.constant(' ')), Range.linear(5, 10)).log("extraInfo")
    m <- Gen.int(Range.linear(0, 100)).map(_.toString).log("m")
  } yield {
    val examples = Examples(Map(
      LabelName(label) -> List(ForAll(Name("n"), n), Info(extraInfo), ForAll(Name("m"), m))
    ))
    val expected = List(s"n: $n, $extraInfo, m: $m")
    val actual = Test.renderExample(examples, LabelName(label))

    actual ==== expected
  }

  def testRenderCoverageMultipleLogs: Result = {
    val coverage = Coverage(Map(
      LabelName("small number") -> Label(LabelName("small number"), CoverPercentage(0), CoverCount(45))
    , LabelName("large number") -> Label(LabelName("large number"), CoverPercentage(0), CoverCount(55))
    ))
    val examples = Examples(Map(
      LabelName("small number") -> List(ForAll(Name("n"), "23"), ForAll(Name("m"), "87"))
    , LabelName("large number") -> List(ForAll(Name("n"), "61"), ForAll(Name("m"), "4"))
    ))
    Test.renderCoverage(coverage, SuccessCount(100), examples) ====
      List(
        "55% large number n: 61, m: 4",
        "45% small number n: 23, m: 87"
      )
  }

  def propertyTestRenderCoverageMultipleLogs: Property = for {
    label1 <- Gen.string(Gen.alphaNum, Range.linear(2, 5)).log("label1")
    label2 <- Gen.string(Gen.alphaNum, Range.linear(6, 10)).log("label2")
    label1Percent <- Gen.int(Range.linear(1, 49)).log("label1Percent")
    label2Percent <- Gen.constant(100 - label1Percent).log("label2Percent")
    n1 <- Gen.int(Range.linear(0, 100)).map(_.toString).log("n1")
    m1 <- Gen.int(Range.linear(0, 100)).map(_.toString).log("m1")
    n2 <- Gen.int(Range.linear(0, 100)).map(_.toString).log("n2")
    m2 <- Gen.int(Range.linear(0, 100)).map(_.toString).log("m2")
  } yield {
    val coverage = Coverage(Map(
      LabelName(label1) -> Label(LabelName(label1), CoverPercentage(0), CoverCount(label1Percent))
    , LabelName(label2) -> Label(LabelName(label2), CoverPercentage(0), CoverCount(label2Percent))
    ))
    val examples = Examples(Map(
      LabelName(label1) -> List(ForAll(Name("n"), n1), ForAll(Name("m"), m1))
    , LabelName(label2) -> List(ForAll(Name("n"), n2), ForAll(Name("m"), m2))
    ))
    val expected = List(
      s"$label2Percent% $label2 n: $n2, m: $m2",
      s"$label1Percent% $label1 n: $n1, m: $m1"
    )
    val actual = Test.renderCoverage(coverage, SuccessCount(100), examples)

    actual ==== expected
  }

  def testRenderLogForAll: Result =
    Test.renderLog(ForAll(Name("n"), "23")) ==== "n: 23"

  def propertyTestRenderLogForAll: Property = for {
    name <- Gen.string(Gen.alphaNum, Range.linear(1, 10)).log("name")
    value <- Gen.string(Gen.alphaNum, Range.linear(0, 10)).log("value")
  } yield {
    Test.renderLog(ForAll(Name(name), value)) ==== s"$name: $value"
  }

  def testRenderLogInfo: Result =
    Test.renderLog(Info("hello")) ==== "hello"

  def propertyTestRenderLogInfo: Property = for {
    value <- Gen.string(Gen.unicode, Range.linear(0, 20)).log("value")
  } yield {
    Test.renderLog(Info(value)) ==== value
  }

  def testRenderLogError: Result = {
    val e = new RuntimeException("boom")
    val lines = Test.renderLog(Error(e)).split("\n", -1).toList
    Result.all(List(
      lines.headOption.map(_.trim) ==== Some(e.toString)
    , Result.assert(lines.length > 1).log(Info(s"Expected the stack frames after the first line, got $lines"))
    ))
  }

  def okReport(tests: Int, coverage: Coverage[CoverCount]): Report =
    Report(SuccessCount(tests), DiscardCount(0), coverage, Examples.empty, OK)

  def failedReport(tests: Int, logs: List[Log], coverage: Coverage[CoverCount]): Report =
    Report(SuccessCount(tests), DiscardCount(0), coverage, Examples.empty, Failed(ShrinkCount(0), logs))

  def gaveUpReport(tests: Int, discards: Int): Report =
    Report(SuccessCount(tests), DiscardCount(discards), Coverage.empty, Examples.empty, GaveUp)

  val largeNumberCoverage: Coverage[CoverCount] =
    Coverage(Map(
      LabelName("large number") -> Label(LabelName("large number"), CoverPercentage(0), CoverCount(55))
    ))

  def testRenderReportBodyOk: Result =
    Test.renderReportBody(okReport(100, Coverage.empty), "pkg.Spec.someTest") ====
      "OK, passed 100 tests"

  def propertyTestRenderReportBodyOk: Property = for {
    tests <- Gen.int(Range.linear(0, 1000)).log("tests")
  } yield {
    Test.renderReportBody(okReport(tests, Coverage.empty), "pkg.Spec.someTest") ====
      s"OK, passed $tests tests"
  }

  def testRenderReportBodyOkWithCoverage: Result =
    Test.renderReportBody(okReport(100, largeNumberCoverage), "pkg.Spec.someTest") ====
      "OK, passed 100 tests\n> 55% large number"

  def testRenderReportBodyGaveUp: Result =
    Test.renderReportBody(gaveUpReport(3, 100), "pkg.Spec.someTest") ====
      "Gave up after only 3 passed test. 100 were discarded"

  def propertyTestRenderReportBodyGaveUp: Property = for {
    tests <- Gen.int(Range.linear(0, 1000)).log("tests")
    discards <- Gen.int(Range.linear(0, 1000)).log("discards")
  } yield {
    Test.renderReportBody(gaveUpReport(tests, discards), "pkg.Spec.someTest") ====
      s"Gave up after only $tests passed test. $discards were discarded"
  }

  def testRenderReportBodyFailed: Result = {
    val logs = List(
      ForAll(Name("n"), "23")
    , Info("hello")
    , SourceLocation(SourcePos("b/Spec.scala", "Spec.scala", 17))
    )
    Test.renderReportBody(failedReport(5, logs, Coverage.empty), "pkg.Spec$.someTest") ====
      List(
        "Falsified after 5 passed tests"
      , "> n: 23"
      , "> hello"
      , "> b/Spec.scala:17"
      , "> at pkg.Spec$.someTest(Spec.scala:17)"
      ).mkString("\n")
  }

  def testRenderReportBodyFailedWithCoverage: Result =
    Test.renderReportBody(failedReport(100, List(Info("hello")), largeNumberCoverage), "pkg.Spec.someTest") ====
      "Falsified after 100 passed tests\n> hello\n> 55% large number"

  def testRenderReportBodyFailedError: Result = {
    val e = new RuntimeException("boom")
    val expected =
      "Falsified after 0 passed tests" :: Test.renderLog(Error(e)).split("\n", -1).toList.map("> " + _)
    Test.renderReportBody(failedReport(0, List(Error(e)), Coverage.empty), "pkg.Spec.someTest")
      .split("\n", -1).toList ==== expected
  }

  def testRenderReportBodyFailedEmptyLog: Result =
    Test.renderReportBody(failedReport(0, List(Info("")), Coverage.empty), "pkg.Spec.someTest") ====
      "Falsified after 0 passed tests\n> "

  val someTest: Test =
    example("someTest", Result.success)

  def testRenderReportOkNoAnsi: Result =
    Test.renderReport("pkg.Spec", someTest, okReport(100, Coverage.empty), ansiCodesSupported = false) ====
      "+ pkg.Spec.someTest: OK, passed 100 tests"

  def testRenderReportOkAnsi: Result =
    Test.renderReport("pkg.Spec", someTest, okReport(100, Coverage.empty), ansiCodesSupported = true) ====
      s"${Console.GREEN}+${Console.RESET} pkg.Spec.someTest: OK, passed 100 tests"

  def testRenderReportFailedNoAnsi: Result =
    Test.renderReport(
      "pkg.Spec"
    , someTest
    , failedReport(5, List(SourceLocation(SourcePos("b/Spec.scala", "Spec.scala", 17))), Coverage.empty)
    , ansiCodesSupported = false
    ) ====
      "- pkg.Spec.someTest: Falsified after 5 passed tests\n> b/Spec.scala:17\n> at pkg.Spec.someTest(Spec.scala:17)"

  def testRenderReportFailedAnsi: Result =
    Test.renderReport("pkg.Spec", someTest, failedReport(5, List(Info("hello")), Coverage.empty), ansiCodesSupported = true) ====
      s"${Console.RED}-${Console.RESET} pkg.Spec.someTest: Falsified after 5 passed tests\n> hello"

  def testRenderReportGaveUpNoAnsi: Result =
    Test.renderReport("pkg.Spec", someTest, gaveUpReport(3, 100), ansiCodesSupported = false) ====
      "- pkg.Spec.someTest: Gave up after only 3 passed test. 100 were discarded"

  def testRenderReportGaveUpAnsi: Result =
    Test.renderReport("pkg.Spec", someTest, gaveUpReport(3, 100), ansiCodesSupported = true) ====
      s"${Console.RED}-${Console.RESET} pkg.Spec.someTest: Gave up after only 3 passed test. 100 were discarded"

  def propertyTestRenderReportQualifiedName: Property = for {
    className <- Gen.string(Gen.alphaNum, Range.linear(1, 10)).log("className")
    testName <- Gen.string(Gen.alphaNum, Range.linear(1, 10)).log("testName")
    line <- Gen.int(Range.linear(1, 1000)).log("line")
  } yield {
    val t = example(testName, Result.success)
    val report = failedReport(0, List(SourceLocation(SourcePos("b/Spec.scala", "Spec.scala", line))), Coverage.empty)
    Test.renderReport(className, t, report, ansiCodesSupported = false) ====
      s"- $className.$testName: Falsified after 0 passed tests\n> b/Spec.scala:$line\n> at $className.$testName(Spec.scala:$line)"
  }

  def testTestApply: Result = {
    val t = Test("someTest", Property.point(Result.success))
    Result.all(List(
      t.name ==== "someTest"
    , t.withConfig(PropertyConfig.default) ==== PropertyConfig.default
    ))
  }

  def testTestApplyException: Result = {
    val e = new RuntimeException("boom")
    val t = Test("someTest", throw e)
    val report = Property.check(t.withConfig(PropertyConfig.default), t.result, Seed.fromLong(0))
    Result.all(List(
      t.name ==== "someTest"
    , report.status match {
        case Failed(_, List(Error(actual))) =>
          Result.assert(actual eq e).log(Info(s"Expected the thrown exception, got $actual"))
        case other =>
          Result.failure.log(Info(s"Expected Failed with only the Error log, got $other"))
      }
    ))
  }

  def testTestConfig: Result =
    Test("someTest", Property.point(Result.success))
      .config(_.copy(testLimit = SuccessCount(5)))
      .config(_.copy(discardLimit = DiscardCount(7)))
      .withConfig(PropertyConfig.default) ====
      PropertyConfig(SuccessCount(5), DiscardCount(7), ShrinkLimit(1000))

  def testTestConfigOrder: Result =
    Test("someTest", Property.point(Result.success))
      .withTests(SuccessCount(5))
      .withTests(SuccessCount(9))
      .withConfig(PropertyConfig.default) ====
      PropertyConfig.default.copy(testLimit = SuccessCount(9))

  def propertyTestTestWithTests: Property = for {
    count <- Gen.int(Range.linear(0, 1000)).log("count")
  } yield {
    Test("someTest", Property.point(Result.success))
      .withTests(SuccessCount(count))
      .withConfig(PropertyConfig.default) ====
      PropertyConfig.default.copy(testLimit = SuccessCount(count))
  }

  def testTestWithTestsRun: Result = {
    val t = Test("someTest", Gen.int(Range.linear(0, 100)).forAll.map(_ => Result.success)).withTests(SuccessCount(7))
    val report = Property.check(t.withConfig(PropertyConfig.default), t.result, Seed.fromLong(0))
    Result.all(List(
      report.status ==== OK
    , report.tests ==== SuccessCount(7)
    ))
  }

  def testTestNoShrinking: Result =
    Test("someTest", Property.point(Result.success))
      .noShrinking
      .withConfig(PropertyConfig.default) ====
      PropertyConfig.default.copy(shrinkLimit = ShrinkLimit(0))

  def testPropertiesMain: Result = {
    /* Anonymous, so the test framework does not find it and run it as a suite. */
    val props = new Properties {
      override def tests: List[Test] =
        List(
          example("pass", Result.success)
        , example("fail", Result.Failure(List(Info("nope"))))
        )
    }
    val out = new java.io.ByteArrayOutputStream()
    Console.withOut(new java.io.PrintStream(out, true, "UTF-8")) {
      props.main(Array.empty[String])
    }
    val lines = out.toString("UTF-8").split("\r?\n").toList
    val className = props.getClass.getName
    lines match {
      case seedLine :: rest =>
        Result.all(List(
          Result.assert(
            seedLine.startsWith("Using random seed: ") ||
              seedLine.startsWith("Using seed from environment variable HEDGEHOG_SEED: ")
          ).log(Info(s"Unexpected seed line: $seedLine"))
        , rest ==== List(
            s"${Console.GREEN}+${Console.RESET} $className.pass: OK, passed 1 tests"
          , s"${Console.RED}-${Console.RESET} $className.fail: Falsified after 0 passed tests"
          , "> nope"
          )
        ))
      case Nil =>
        Result.failure.log(Info("Properties.main printed nothing"))
    }
  }
}
