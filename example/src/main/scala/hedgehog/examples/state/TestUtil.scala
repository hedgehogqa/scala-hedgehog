package hedgehog.examples.state

import java.io.File

object TestUtil {

  def deleteRecursively(file: File): Unit = {
    if (file.isDirectory) {
      file.listFiles.foreach(deleteRecursively)
    }
    file.delete
    ()
  }
}
