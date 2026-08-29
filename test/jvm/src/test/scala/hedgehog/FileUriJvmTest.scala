package hedgehog

import hedgehog.core._
import hedgehog.runner._

/**
 * `FileUri` is hand-rolled because `java.nio.file` is absent on Scala.js and
 * Scala Native. This guards against it drifting from the JDK's own notion of a
 * file URI for the common case: an absolute path of plain ASCII segments, where
 * nothing needs encoding and the two must agree exactly.
 *
 * Paths with a space or non-ASCII are deliberately excluded, since that is where
 * we differ from `toUri` on purpose - see FileUriTest.
 */
object FileUriJvmTest extends Properties {

  def tests: List[Test] =
    List(
      property("matches java.nio.file.Paths.toUri for plain ASCII paths", testMatchesJdk)
    )

  def testMatchesJdk: Property =
    for {
      segments <- Gen.string(Gen.alphaNum, Range.linear(1, 8))
        .list(Range.linear(1, 5))
        .log("segments")
    } yield {
      val path = segments.mkString("/", "/", "")
      val expected = java.nio.file.Paths.get(path).toUri.toString
      FileUri.fromPath(path) ==== expected
    }
}
