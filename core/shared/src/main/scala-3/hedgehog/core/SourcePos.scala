package hedgehog.core

import scala.quoted.*

/**
 * The location in the source file at which an assertion was written.
 *
 * An instance is materialised by the compiler at the point where it is required,
 * so a `Result` constructor which takes one implicitly reports the caller's
 * location rather than a location inside Hedgehog itself.
 *
 * No absolute path is stored. An absolute path is baked into the compiled class file as a string
 * constant, so the same source compiled from two checkout directories produces two different class
 * files. That defeats a shared build cache, which would otherwise hand one machine another
 * machine's paths on a legitimate cache hit, and it defeats distributed test execution and
 * reproducible builds. It also leaks the publisher's filesystem into a published artefact.
 *
 * @param relativePath the path of the source file with the compiler's working directory stripped,
 *                     which under sbt is the build root, and the path exactly as captured when it
 *                     does not sit underneath that directory
 * @param fileName the simple name of the source file
 * @param line the 1-based line number
 */
final case class SourcePos(relativePath: String, fileName: String, line: Int)

object SourcePos {

  /**
   * A placeholder for logs which are constructed by hand rather than captured
   * at a call site, and for test expectations.
   *
   * This is deliberately not implicit.
   */
  val unknown: SourcePos =
    SourcePos("<unknown>", "<unknown>", 0)

  inline given here: SourcePos =
    ${ SourcePosMacro.hereImpl }
}

object SourcePosMacro {

  /**
   * `user.dir` is read here rather than held in a field, so that no object initialiser on
   * Scala.js or Scala Native ever touches it. This method runs on the Java Virtual Machine at
   * macro-expansion time and is unreachable at run time.
   */
  def hereImpl(using Quotes): Expr[SourcePos] = {
    import quotes.reflect.*
    val pos = Position.ofMacroExpansion
    val path = pos.sourceFile.path
    val relativePath = Expr(SourceRoot.relativize(path, sys.props.getOrElse("user.dir", "")))
    val fileName = Expr(pos.sourceFile.name)
    /* Scala 3 reports a 0-based line, Scala 2 a 1-based one. */
    val line = Expr(pos.startLine + 1)
    '{ SourcePos($relativePath, $fileName, $line) }
  }
}
