package hedgehog.extra

import hedgehog.Range
import hedgehog.core.GenT
import hedgehog.predef._

trait ByteOps[M[_]] {

  private val genT = new hedgehog.GenTOps[M] with CharacterOps[M] {}

  /**
   * Generates a random 'Array[Byte]', using 'Range' to determine the length.
   *
   * _Shrinks down to the ascii characters._
   */
  def bytes(range: Range[Int])(implicit F: Monad[M]): GenT[M, Array[Byte]] =
    genT.choice1(
      genT.ascii.map(_.toByte)
    , genT.byte(Range.constant(java.lang.Byte.MIN_VALUE, java.lang.Byte.MAX_VALUE))
    ).list(range).map(_.toArray)
}
