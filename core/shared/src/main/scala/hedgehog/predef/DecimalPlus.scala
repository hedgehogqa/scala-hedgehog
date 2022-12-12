package hedgehog.predef

/** Operations that are unfortunately missing from `Fractional` */
trait DecimalPlus[A] {

  def toBigDecimal(a: A): BigDecimal

  def fromBigDecimal(a: BigDecimal): A
}

object DecimalPlus {

  implicit val FloatDecimalPlus: DecimalPlus[Float] =
    new DecimalPlus[Float] {

      override def toBigDecimal(a: Float): BigDecimal =
        BigDecimal(a.toDouble)

      override def fromBigDecimal(a: BigDecimal): Float =
        a.toFloat
    }

  implicit val DoubleDecimalPlus: DecimalPlus[Double] =
    new DecimalPlus[Double] {

      override def toBigDecimal(a: Double): BigDecimal =
        BigDecimal(a)

      override def fromBigDecimal(a: BigDecimal): Double =
        a.toDouble
    }
}
