package hedgehog

import hedgehog.core._
import hedgehog.runner._

/**
 * WARNING: the expectations in this file assert on literal line numbers.
 * Inserting or removing lines above an assertion will break the test which
 * follows it. Each expected line number is written immediately next to the
 * assertion it describes, so keep the two together when editing.
 */
object SourcePosTest extends Properties {

  /**
   * Where this file sits relative to the build root.
   *
   * `relativePath` is the captured path with the compiler's working directory stripped, and under
   * sbt that working directory is the build root. Compiling from anywhere else makes the strip
   * fail and leaves an absolute path behind, which is exactly what this pins down.
   */
  val thisFileRelativePath: String =
    "test/shared/src/test/scala/hedgehog/SourcePosTest.scala"

  def tests: List[Test] =
    List(
      example("==== records the location of the assertion", testEqualsEquals)
    , example("Result.assert records the location of the assertion", testAssert)
    , example("Result.diff records the location of the assertion", testDiff)
    , example("Result.diffNamed records the location of the assertion", testDiffNamed)
    , example("Result.failure records the location of the assertion", testFailure)
    , example("matchPattern records the location of a non-match", testMatchPattern)
    , example("Result.error records no location", testErrorHasNoLocation)
    , example("Result.any of Nil records no location", testAnyHasNoLocation)
    , example("the location is the first log entry", testLocationComesFirst)
    , example("SourceLocation equality ignores the position", testEqualityIgnoresPosition)
    , example(".log does not move the recorded line", testChainedLogDoesNotMoveLine)
    , example(".log does not move the recorded line for assert", testChainedLogDoesNotMoveLineForAssert)
    , example("Result.all records one location per failing element", testAllRecordsEachElement)
    )

  def locations(r: Result): List[SourcePos] =
    r.logs.collect { case SourceLocation(pos) => pos }

  /**
   * Every location must name this file - by simple name, by absolute path, and by path relative
   * to the build root.
   */
  def wellFormed(pos: SourcePos): Result =
    Result.all(List(
      pos.fileName ==== "SourcePosTest.scala"
    , Result.assert(pos.filePath.startsWith("/")).log(s"not absolute: ${pos.filePath}")
    , Result.assert(pos.filePath.endsWith("/" + pos.fileName)).log(s"path/name mismatch: ${pos.filePath}")
      // No path in this repository contains a character which gets encoded, so the
      // two representations differ only by the scheme.
    , pos.fileUri ==== "file://" + pos.filePath
    , pos.relativePath ==== thisFileRelativePath
    , Result.assert(pos.filePath.endsWith("/" + pos.relativePath))
        .log(s"relative path is not a suffix of the absolute one: ${pos.filePath}")
    ))

  def onlyLocationAt(r: Result, expectedLine: Int): Result =
    locations(r) match {
      case pos :: Nil =>
        wellFormed(pos).and(pos.line ==== expectedLine)
      case other =>
        Result.failure.log(s"expected exactly one location, got: ${other.toString}")
    }

  def testEqualsEquals: Result = {
    val r = 1 ==== 2 // line 70
    onlyLocationAt(r, 70)
  }

  def testAssert: Result = {
    val r = Result.assert(false) // line 75
    onlyLocationAt(r, 75)
  }

  def testDiff: Result = {
    val r = Result.diff(1, 2)(_ == _) // line 80
    onlyLocationAt(r, 80)
  }

  def testDiffNamed: Result = {
    val r = Result.diffNamed("=== Nope ===", 1, 2)(_ == _) // line 85
    onlyLocationAt(r, 85)
  }

  def testFailure: Result = {
    val r = Result.failure // line 90
    onlyLocationAt(r, 90)
  }

  def testMatchPattern: Result = {
    val r = "abc".matchPattern { case "xyz" => } // line 95
    onlyLocationAt(r, 95)
  }

  def testErrorHasNoLocation: Result = {
    val e = new RuntimeException("boom")
    val r = Result.error(e)
    Result.all(List(
      locations(r) ==== Nil
    , r.logs ==== List(Error(e))
    ))
  }

  def testAnyHasNoLocation: Result =
    locations(Result.any(Nil)) ==== Nil

  def testLocationComesFirst: Result =
    (1 ==== 2).logs match {
      case SourceLocation(_) :: Info("=== Not Equal ===") :: _ =>
        Result.success
      case other =>
        Result.failure.log(s"location was not the first log entry: ${other.toString}")
    }

  def testEqualityIgnoresPosition: Result =
    SourceLocation(SourcePos("/a/A.scala", "a/A.scala", "A.scala", 1)) ====
      SourceLocation(SourcePos("/b/B.scala", "b/B.scala", "B.scala", 2))

  def testChainedLogDoesNotMoveLine: Result = {
    val r = (1 ==== 2) // line 124
      .log("a should be equal to b")
    onlyLocationAt(r, 124)
  }

  def testChainedLogDoesNotMoveLineForAssert: Result = {
    val r = Result.assert(1 == 2) // line 130
      .log("a should be equal to b")
    onlyLocationAt(r, 130)
  }

  def testAllRecordsEachElement: Result = {
    val r = Result.all(List(
      1 ==== 1 // passes, contributes nothing
    , 2 ==== 3 // line 138
    , 4 ==== 5 // line 139
    ))
    locations(r).map(_.line) ==== List(138, 139)
  }
}
