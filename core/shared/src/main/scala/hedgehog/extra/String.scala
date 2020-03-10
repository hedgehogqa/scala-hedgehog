package hedgehog.extra

import hedgehog.Range
import hedgehog.core.GenT

trait StringOps {

  /**
   * Generates a string using 'Range' to determine the length.
   */
  def string(gen: GenT[Char], range: Range[Int]): GenT[String] =
    gen.list(range).map(_.mkString)
}
