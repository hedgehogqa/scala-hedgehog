package hedgehog.core

/**
 * Renders an absolute file path as a `file://` URI.
 *
 * The point is to produce something a terminal or an editor will turn into a
 * link. `file:///a/b/Spec.scala:31` is clickable in iTerm2 with semantic history
 * configured, and in IntelliJ IDEA, where it navigates to the exact line. A bare
 * path is not.
 *
 * This is hand-rolled rather than delegating to `java.nio.file.Paths.toUri` for
 * two reasons. `java.nio.file` does not exist on Scala.js or Scala Native, and
 * this has to run wherever a report is rendered. And `toUri` percent-encodes
 * non-ASCII, which turns a perfectly readable Korean or Japanese path into a
 * wall of `%XX`.
 */
private[hedgehog] object FileUri {

  /**
   * An absolute POSIX path becomes `file://` plus the path, whose own leading
   * slash supplies the third one. A Windows path becomes `file:///` plus the
   * path with its separators flipped, since it has no leading slash of its own
   * and `file://C:/...` would parse `C:` as the host.
   *
   * Anything else - a relative path, or `SourcePos.unknown` - is returned
   * unchanged rather than dressed up as a URI. That includes UNC paths such as
   * `\\server\share\Spec.scala`, which are deliberately not supported.
   */
  def fromPath(path: String): String =
    if (path.startsWith("/"))
      "file://" + encode(path)
    else if (isWindowsAbsolute(path))
      "file:///" + encode(path.replace('\\', '/'))
    else
      path

  /** A single ASCII letter, a colon, then a separator. e.g. `C:\` or `c:/`. */
  private def isWindowsAbsolute(path: String): Boolean =
    path.length >= 3 &&
      isAsciiLetter(path.charAt(0)) &&
      path.charAt(1) == ':' &&
      (path.charAt(2) == '\\' || path.charAt(2) == '/')

  private def isAsciiLetter(c: Char): Boolean =
    (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')

  /**
   * Only the characters which would actually break the link, so that non-ASCII
   * stays readable. `%` has to go first, or the escapes introduced below get
   * double-encoded.
   */
  private def encode(path: String): String =
    path
      .replace("%", "%25")
      .replace(" ", "%20")
      .replace("#", "%23")
      .replace("?", "%3F")
}
