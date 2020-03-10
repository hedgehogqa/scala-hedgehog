package hedgehog.extra

import hedgehog.Range
import hedgehog.core.GenT

trait ByteOps {

  private val genT = new hedgehog.GenTOps with CharacterOps {}

  /**
   * Generates a random 'Array[Byte]', using 'Range' to determine the length.
   *
   * _Shrinks down to the ascii characters._
   */
  def bytes(range: Range[Int]): GenT[Array[Byte]] =
    genT.choice1(
      genT.ascii.map(_.toByte)
    , genT.byte(Range.constant(java.lang.Byte.MIN_VALUE, java.lang.Byte.MAX_VALUE))
    ).list(range).map(_.toArray)
}
