package hedgehog

import hedgehog.core.NumericPlus
import hedgehog.predef.{DecimalPlus, IntegralPlus}

/**
 * Tests are parameterized by the size of the randomly-generated data, the
 * meaning of which depends on the particular generator used.
 */
sealed abstract case class Size private (value: Int) {

  /** Represents the size as a percentage (0 - 1) which is useful for range calculations */
  def percentage: Double =
    value.toDouble / Size.max

  def incBy(v: Size): Size =
    Size(value + v.value)

  /**
   * Scale a size using the golden ratio.
   */
  def golden: Size =
    Size((value * 0.61803398875).toInt)
}

object Size {
  def apply(value: Int): Size = {
    val remainder = value % max
    new Size(if (remainder <= 0) remainder + max else remainder) {}
  }

  def max: Int =
    100
}

/**
 * A range describes the bounds of a number to generate, which may or may not
 * be dependent on a 'Size'.
 *
 * @param origin
 *   Get the origin of a range. This might be the mid-point or the lower bound,
 *   depending on what the range represents.
 *
 *   The 'bounds' of a range are scaled around this value when using the
 *   'linear' family of combinators.
 *
 *   When using a 'Range' to generate numbers, the shrinking function will
 *   shrink towards the origin.
 *
 * @param bounds
 *   Get the extents of a range, for a given size.
 */
case class Range[A](origin: A, bounds: Size => (A, A)) {

  /** Get the lower bound of a range for the given size. */
  def lowerBound(size: Size)(implicit O: Ordering[A]): A = {
    val (x, y) = bounds(size)
    O.min(x, y)
  }

  /** Get the upper bound of a range for the given size. */
  def upperBound(size: Size)(implicit O: Ordering[A]): A = {
    val (x, y) = bounds(size)
    O.max(x, y)
  }

  def map[B](f: A => B): Range[B] =
    Range(f(origin), s => {
      val (x, y) = bounds(s)
      (f(x), f(y))
    })
}

object Range {

  /**
   * Construct a range which represents a constant single value.
   *
   * {{{
   * scala> Range.singleton(5).bounds(x)
   * (5,5)
   *
   * scala> Range.singleton(5).origin
   * 5
   * }}}
   */
  def singleton[A](x: A): Range[A] =
    Range(x, _ => (x, x))

  /**
   * Construct a range which is unaffected by the size parameter.
   *
   * A range from `0` to `10`, with the origin at `0`:
   *
   * {{{
   * scala> Range.constant(0, 10).bounds(x)
   * (0,10)
   *
   * scala> Range.constant(0, 10).origin
   * 0
   * }}}
   */
  def constant[A](x: A, y: A): Range[A] =
    constantFrom(x, x, y)

  /**
   * Construct a range which is unaffected by the size parameter with a origin
   * point which may differ from the bounds.
   *
   * A range from `-10` to `10`, with the origin at `0`:
   *
   * {{{
   * scala> Range.constantFrom(0, -10, 10).bounds(x)
   * (-10,10)
   *
   * scala> Range.constantFrom(0, -10, 10).origin
   * 0
   * }}}
   *
   * A range from `1970` to `2100`, with the origin at `2000`:
   *
   * {{{
   * scala> Range.constantFrom(2000, 1970, 2100).bounds(x)
   * (1970,2100)
   *
   * scala> Range.constantFrom(2000, 1970, 2100).origin
   * 2000
   * }}}
   */
  def constantFrom[A](z: A, x: A, y: A): Range[A] =
    Range(z, _ => (x, y))

  /**
   * Construct a range which scales the second bound relative to the size
   * parameter.
   *
   * {{{
   * scala> Range.linear(0, 10).bounds(Size(1))
   * (0,0)
   *
   * scala> Range.linear(0, 10).bounds(Size(50))
   * (0,5)
   *
   * scala> Range.linear(0, 10).bounds(Size(100))
   * (0,10)
   * }}}
   */
  def linear[A : Integral : IntegralPlus : NumericPlus](x: A, y: A): Range[A] =
    linearFrom(x, x, y)

  /**
   * Construct a range which scales the second bound relative to the size
   * parameter.
   *
   * {{{
   * scala> Range.linearFrom(0, -10, 10).bounds(Size(1))
   * (0,0)
   *
   * scala> Range.linearFrom(0, -10, 20).bounds(Size(50))
   * (-5,10)
   *
   * scala> Range.linearFrom(0, -10, 20).bounds(Size(100))
   * (-10,20)
   * }}}
   */
  def linearFrom[A](z: A, x: A, y: A)(implicit I: Integral[A], J: IntegralPlus[A], R: NumericPlus[A]): Range[A] =
    // Check for overflow and if we do then start using BigInt
    if (I.lt(I.minus(y, x), I.zero) && I.gt(y, I.zero)) {
      linearFrom_(J.toBigInt(z), J.toBigInt(x), J.toBigInt(y))
        .map(J.fromBigInt)
    } else {
      linearFrom_(z, x, y)
    }

  def linearFrom_[A : Integral : NumericPlus](z: A, x: A, y: A): Range[A] =
    Range(z, sz => (
        clamp(x, y, scaleLinear(sz, z, x))
      , clamp(x, y, scaleLinear(sz, z, y))
      )
    )

  /**
   * Construct a range which scales the second bound relative to the size
   * parameter.
   *
   * This works the same as 'linear', but for fractional values.
   */
  def linearFrac[A : Fractional : DecimalPlus : NumericPlus](x: A, y: A): Range[A] =
    linearFracFrom(x, x, y)

  /**
   * Construct a range which scales the bounds relative to the size parameter.
   *
   * This works the same as [[linearFrom]], but for fractional values.
   */
  def linearFracFrom[A](z: A, x: A, y: A)(implicit I: Fractional[A], J: DecimalPlus[A], R: NumericPlus[A]): Range[A] =
    // Check for gross imprecision and lift to `BigDecimal` to ensure we don't produce a bad range
    if (I.toDouble(I.minus(y, x)).isInfinity) {
      linearFracFrom_(J.toBigDecimal(z), J.toBigDecimal(x), J.toBigDecimal(y))
        .map(J.fromBigDecimal)
    } else {
      linearFracFrom_(z, x, y)
    }

  def linearFracFrom_[A : Fractional : NumericPlus](z: A, x: A, y: A): Range[A] =
    Range(z, sz => (
        clamp(x, y, scaleLinearFrac(sz, z, x))
      , clamp(x, y, scaleLinearFrac(sz, z, y))
      )
    )

  /**
   * Truncate a value so it stays within some range.
   *
   * {{{
   * scala> clamp(5, 10, 15)
   * 10
   *
   * scala> clamp(5, 10, 0)
   * 5
   * }}}
   */
  def clamp[A](x: A, y: A, n: A)(implicit O: Ordering[A]): A =
    if (O.gt(x, y))
      O.min(x, O.max(y, n))
    else
      O.min(y, O.max(x, n))

  /** Scale an integral linearly with the size parameter. */
  def scaleLinear[A](sz: Size, z: A, n: A)(implicit I: Integral[A], J: NumericPlus[A]): A =
    I.plus(z, J.timesDouble(I.minus(n, z), sz.percentage))

  /** Scale a fractional number linearly with the size parameter. */
  def scaleLinearFrac[A](sz: Size, z: A, n: A)(implicit F: Fractional[A], J: NumericPlus[A]): A =
    F.plus(z, J.timesDouble(F.minus(n, z), sz.percentage))

  /** Check that list contains at least a certain number of elements. */
  def atLeast[A](n: Int, l: List[A]): Boolean =
    if (n == 0)
      true
    else
      l.drop(n - 1).nonEmpty
}
