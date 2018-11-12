package hedgehog.predef

/** Operations that are unfortunately missing from `Integral` */
trait IntegralPlus[A] {

  def times(a: A, b: Double): A

  def toBigInt(a: A): BigInt

  def fromBigInt(a: BigInt): A
}

object IntegralPlus {

  implicit def ByteIntegralPlus: IntegralPlus[Byte] =
    new IntegralPlus[Byte] {

      override def times(a: Byte, b: Double): Byte =
        (a.toDouble * b).toByte

      override def toBigInt(a: Byte): BigInt =
        BigInt(a)

      override def fromBigInt(a: BigInt): Byte =
        a.toByte
    }

  implicit def ShortIntegralPlus: IntegralPlus[Short] =
    new IntegralPlus[Short] {

      override def times(a: Short, b: Double): Short =
        (a.toDouble * b).toShort

      override def toBigInt(a: Short): BigInt =
        BigInt(a)

      override def fromBigInt(a: BigInt): Short =
        a.toShort
    }

  implicit def IntIntegralPlus: IntegralPlus[Int] =
    new IntegralPlus[Int] {

      override def times(a: Int, b: Double): Int =
        (a.toDouble * b).toInt

      override def toBigInt(a: Int): BigInt =
        BigInt(a)

      override def fromBigInt(a: BigInt): Int =
        a.toInt
      }

  implicit def LongIntegralPlus: IntegralPlus[Long] =
    new IntegralPlus[Long] {

      override def times(a: Long, b: Double): Long =
        (a.toDouble * b).toLong

      override def toBigInt(a: Long): BigInt =
        BigInt(a)

      override def fromBigInt(a: BigInt): Long =
        a.toLong
    }


  implicit def BigIntIntegralPlus: IntegralPlus[BigInt] =
    new IntegralPlus[BigInt] {

      override def times(a: BigInt, b: Double): BigInt =
        (BigDecimal(a) * b).toBigInt

      override def toBigInt(a: BigInt): BigInt =
        a

      override def fromBigInt(a: BigInt): BigInt =
        a
    }
}
