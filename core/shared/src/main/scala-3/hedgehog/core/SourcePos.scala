package hedgehog.core

import scala.quoted.*

/**
 * The location in the source file at which an assertion was written.
 *
 * An instance is materialised by the compiler at the point where it is required,
 * so a `Result` constructor which takes one implicitly reports the caller's
 * location rather than a location inside Hedgehog itself.
 *
 * @param filePath the absolute path of the source file
 * @param fileName the simple name of the source file
 * @param line the 1-based line number
 */
final case class SourcePos(filePath: String, fileName: String, line: Int) {

  /**
   * `filePath` rendered as a `file://` URI, which terminals and editors turn
   * into a link. Returns `filePath` unchanged when it is not an absolute path.
   */
  def fileUri: String =
    FileUri.fromPath(filePath)
}

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

  def hereImpl(using Quotes): Expr[SourcePos] = {
    import quotes.reflect.*
    val pos = Position.ofMacroExpansion
    val filePath = Expr(pos.sourceFile.path)
    val fileName = Expr(pos.sourceFile.name)
    /* Scala 3 reports a 0-based line, Scala 2 a 1-based one. */
    val line = Expr(pos.startLine + 1)
    '{ SourcePos($filePath, $fileName, $line) }
  }
}
