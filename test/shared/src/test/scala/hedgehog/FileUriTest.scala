package hedgehog

import hedgehog.core._
import hedgehog.runner._

object FileUriTest extends Properties {

  def tests: List[Test] =
    List(
      example("a plain POSIX path becomes file:// plus the path", testPosix)
    , example("a space is encoded", testSpace)
    , example("non-ASCII is left alone", testNonAscii)
    , example("a percent sign is encoded", testPercent)
    , example("a literal %20 is not confused with an encoded space", testLiteralPercent20)
    , example("a hash is encoded", testHash)
    , example("a question mark is encoded", testQuestionMark)
    , example("a Windows path becomes file:/// with flipped separators", testWindows)
    , example("a lower case Windows drive letter works", testWindowsLowerCase)
    , example("a Windows path already using forward slashes works", testWindowsForwardSlashes)
    , example("SourcePos.unknown is returned unchanged", testUnknown)
    , example("a relative path is returned unchanged", testRelative)
    , example("a UNC path is returned unchanged", testUnc)
    , example("SourcePos.fileUri delegates to FileUri", testSourcePosDelegates)
    )

  def testPosix: Result =
    FileUri.fromPath("/a/b/Spec.scala") ==== "file:///a/b/Spec.scala"

  def testSpace: Result =
    FileUri.fromPath("/a/my project/Spec.scala") ==== "file:///a/my%20project/Spec.scala"

  /**
   * The whole reason this is not `java.nio.file.Paths.toUri`, which would turn
   * this into `%ED%95%9C%EA%B8%80%EA%B2%BD%EB%A1%9C`.
   */
  def testNonAscii: Result =
    Result.all(List(
      FileUri.fromPath("/a/한글경로/Spec.scala") ==== "file:///a/한글경로/Spec.scala"
    , FileUri.fromPath("/a/日本語/Spec.scala") ==== "file:///a/日本語/Spec.scala"
    , FileUri.fromPath("/a/café/Spec.scala") ==== "file:///a/café/Spec.scala"
    ))

  def testPercent: Result =
    FileUri.fromPath("/a/100%/Spec.scala") ==== "file:///a/100%25/Spec.scala"

  def testLiteralPercent20: Result =
    FileUri.fromPath("/a/b%20c/Spec.scala") ==== "file:///a/b%2520c/Spec.scala"

  def testHash: Result =
    FileUri.fromPath("/a/c#1/Spec.scala") ==== "file:///a/c%231/Spec.scala"

  def testQuestionMark: Result =
    FileUri.fromPath("/a/what?/Spec.scala") ==== "file:///a/what%3F/Spec.scala"

  def testWindows: Result =
    FileUri.fromPath("C:\\Users\\kevin\\Spec.scala") ==== "file:///C:/Users/kevin/Spec.scala"

  def testWindowsLowerCase: Result =
    FileUri.fromPath("c:\\Users\\kevin\\Spec.scala") ==== "file:///c:/Users/kevin/Spec.scala"

  def testWindowsForwardSlashes: Result =
    FileUri.fromPath("C:/Users/kevin/Spec.scala") ==== "file:///C:/Users/kevin/Spec.scala"

  def testUnknown: Result =
    Result.all(List(
      FileUri.fromPath(SourcePos.unknown.filePath) ==== "<unknown>"
    , SourcePos.unknown.fileUri ==== "<unknown>"
    ))

  def testRelative: Result =
    FileUri.fromPath("src/test/scala/Spec.scala") ==== "src/test/scala/Spec.scala"

  /** A documented limitation rather than a desired behaviour. */
  def testUnc: Result =
    FileUri.fromPath("\\\\server\\share\\Spec.scala") ==== "\\\\server\\share\\Spec.scala"

  def testSourcePosDelegates: Result =
    SourcePos("/a/b/Spec.scala", "b/Spec.scala", "Spec.scala", 17).fileUri ==== "file:///a/b/Spec.scala"
}
