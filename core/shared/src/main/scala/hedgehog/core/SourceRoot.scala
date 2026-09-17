package hedgehog.core

/**
 * Strips the compiler's working directory from a captured source path.
 *
 * An absolute path is not hermetic: it is baked into the compiled class file as a string constant,
 * so the same source compiled from two checkout directories produces two different artefacts, and
 * a build cache cannot share them. A path relative to the directory the compiler was run from is.
 *
 * The working directory is passed in rather than read here, so that this stays pure string
 * manipulation that works on every platform. Only the macro implementations, which run on the Java
 * Virtual Machine at expansion time, read `user.dir`.
 */
private[hedgehog] object SourceRoot {

  /**
   * `filePath` with `workingDirectory` and the separator which follows it removed, when
   * `filePath` sits underneath it. Anything else is returned unchanged: a path somewhere else
   * entirely, a path which is already relative - Bazel and Pants hand the compiler those - or an
   * empty working directory.
   *
   * The separator matters. Without it `/home/u/proj` would swallow the prefix of
   * `/home/u/proj2/src/Spec.scala` and leave `2/src/Spec.scala` behind.
   */
  def relativize(filePath: String, workingDirectory: String): String = {
    val root = withTrailingSeparator(workingDirectory)
    if (root.isEmpty || !filePath.startsWith(root))
      filePath
    else
      filePath.substring(root.length)
  }

  private def withTrailingSeparator(workingDirectory: String): String =
    if (workingDirectory.isEmpty)
      ""
    else if (workingDirectory.endsWith("/") || workingDirectory.endsWith("\\"))
      workingDirectory
    else
      workingDirectory + separatorOf(workingDirectory)

  /**
   * Inferred from the working directory rather than taken from `java.io.File.separator`, which
   * does not exist on Scala.js. A Windows path contains a backslash, a POSIX one does not.
   */
  private def separatorOf(path: String): String =
    if (path.contains("\\")) "\\" else "/"
}
