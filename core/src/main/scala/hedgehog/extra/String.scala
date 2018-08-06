package hedgehog.extra

import hedgehog.Range
import hedgehog.core.GenT
import hedgehog.predef._

trait StringOps[M[_]] {

  /**
   * Generates a string using 'Range' to determine the length.
   */
  def string(gen: GenT[M, Char], range: Range[Int])(implicit F: Monad[M]): GenT[M, String] =
    gen.list(range).map(_.mkString)
}
