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
    , property("property test renderCoverage renders each label's example logs separated by ', '", propertyTestRenderCoverageMultipleLogs)
    )

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
}
