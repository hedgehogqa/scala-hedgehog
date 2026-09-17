package hedgehog.core

import scala.reflect.macros.blackbox

/**
 * The location in the source file at which an assertion was written.
 *
 * An instance is materialised by the compiler at the point where it is required,
 * so a `Result` constructor which takes one implicitly reports the caller's
 * location rather than a location inside Hedgehog itself.
 *
 * @param filePath the path of the source file exactly as the compiler was given it, which under
 *                 sbt is absolute
 * @param relativePath `filePath` with the compiler's working directory stripped, or `filePath`
 *                     unchanged when it does not sit underneath that directory
 * @param fileName the simple name of the source file
 * @param line the 1-based line number
 */
final case class SourcePos(filePath: String, relativePath: String, fileName: String, line: Int) {

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
    SourcePos("<unknown>", "<unknown>", "<unknown>", 0)

  implicit def here: SourcePos =
    macro SourcePosMacro.hereImpl
}

object SourcePosMacro {

  /**
   * `path` rather than `canonicalPath`: sbt derives its base directory from `user.dir` and
   * resolves source paths against it, so the two share a prefix by construction. Canonicalising
   * one side and not the other can only break that.
   *
   * `user.dir` is read here rather than held in a field, so that no object initialiser on
   * Scala.js or Scala Native ever touches it. This method runs on the Java Virtual Machine at
   * macro-expansion time and is unreachable at run time.
   */
  def hereImpl(c: blackbox.Context): c.Expr[SourcePos] = {
    import c.universe._
    val pos = c.enclosingPosition
    val filePath = pos.source.file.path
    val relativePath = SourceRoot.relativize(filePath, sys.props.getOrElse("user.dir", ""))
    val fileName = pos.source.file.name
    val line = pos.line
    c.Expr[SourcePos](
      q"_root_.hedgehog.core.SourcePos($filePath, $relativePath, $fileName, $line)")
  }
}
