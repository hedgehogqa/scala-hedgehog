package hedgehog.core

trait NumericPlus[A] {

  def timesDouble(a: A, b: Double): A
}

object NumericPlus {

  implicit val ByteRatio: NumericPlus[Byte] =
    new NumericPlus[Byte] {

      override def timesDouble(a: Byte, b: Double): Byte =
        (a.toDouble * b).toByte
    }

  implicit val ShortRatio: NumericPlus[Short] =
    new NumericPlus[Short] {

      override def timesDouble(a: Short, b: Double): Short =
        (a.toDouble * b).toShort
    }

  implicit val IntRatio: NumericPlus[Int] =
    new NumericPlus[Int] {

      override def timesDouble(a: Int, b: Double): Int =
        (a.toDouble * b).toInt
      }

  implicit val LongRatio: NumericPlus[Long] =
    new NumericPlus[Long] {

      override def timesDouble(a: Long, b: Double): Long =
        (BigDecimal(a) * b).toLong
    }

  implicit val BigIntRatio: NumericPlus[BigInt] =
    new NumericPlus[BigInt] {

      override def timesDouble(a: BigInt, b: Double): BigInt =
        (BigDecimal(a) * b).toBigInt
    }

  implicit val FloatRatio: NumericPlus[Float] =
    new NumericPlus[Float] {

      override def timesDouble(a: Float, b: Double): Float =
        (a.toDouble * b).toFloat
    }

  implicit val DoubleRatio: NumericPlus[Double] =
    new NumericPlus[Double] {

      override def timesDouble(a: Double, b: Double): Double =
        a * b
    }

  implicit val BigDecimalRatio: NumericPlus[BigDecimal] =
    new NumericPlus[BigDecimal] {

      override def timesDouble(a: BigDecimal, b: Double): BigDecimal =
        a * b
    }
}
