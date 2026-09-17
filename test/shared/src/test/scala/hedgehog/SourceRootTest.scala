package hedgehog

import hedgehog.core._
import hedgehog.runner._

object SourceRootTest extends Properties {

  def tests: List[Test] =
    List(
      example("a path under the working directory loses that prefix", testUnderRoot)
    , example("a trailing separator on the working directory is not doubled", testTrailingSeparator)
    , example("a path outside the working directory is returned unchanged", testOutsideRoot)
    , example("a sibling directory sharing a prefix is not stripped", testSiblingPrefix)
    , example("an empty working directory leaves the path alone", testEmptyRoot)
    , example("a path which is already relative is returned unchanged", testAlreadyRelative)
    , example("a Windows path loses its Windows working directory", testWindows)
    , example("a Windows working directory with a trailing separator works", testWindowsTrailingSeparator)
    , example("SourcePos.unknown is returned unchanged", testUnknown)
    )

  def testUnderRoot: Result =
    SourceRoot.relativize("/home/u/proj/src/test/scala/Spec.scala", "/home/u/proj") ====
      "src/test/scala/Spec.scala"

  def testTrailingSeparator: Result =
    SourceRoot.relativize("/home/u/proj/src/test/scala/Spec.scala", "/home/u/proj/") ====
      "src/test/scala/Spec.scala"

  def testOutsideRoot: Result =
    SourceRoot.relativize("/elsewhere/Spec.scala", "/home/u/proj") ====
      "/elsewhere/Spec.scala"

  /** `/home/u/proj2` must not be read as `/home/u/proj` followed by `2`. */
  def testSiblingPrefix: Result =
    SourceRoot.relativize("/home/u/proj2/src/Spec.scala", "/home/u/proj") ====
      "/home/u/proj2/src/Spec.scala"

  def testEmptyRoot: Result =
    SourceRoot.relativize("/home/u/proj/Spec.scala", "") ====
      "/home/u/proj/Spec.scala"

  /** Bazel and Pants hand the compiler relative paths already. */
  def testAlreadyRelative: Result =
    SourceRoot.relativize("src/test/scala/Spec.scala", "/home/u/proj") ====
      "src/test/scala/Spec.scala"

  def testWindows: Result =
    SourceRoot.relativize("C:\\Users\\kevin\\proj\\src\\Spec.scala", "C:\\Users\\kevin\\proj") ====
      "src\\Spec.scala"

  def testWindowsTrailingSeparator: Result =
    SourceRoot.relativize("C:\\Users\\kevin\\proj\\src\\Spec.scala", "C:\\Users\\kevin\\proj\\") ====
      "src\\Spec.scala"

  def testUnknown: Result =
    SourceRoot.relativize(SourcePos.unknown.relativePath, "/home/u/proj") ==== "<unknown>"
}
