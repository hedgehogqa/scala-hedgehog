package hedgehog

/**
 * Tests are parameterized by the size of the randomly-generated data, the
 * meaning of which depends on the particular generator used.
 */
case class Size(value: Int)

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
   * scala> Range.linear(0, 10).bounds(Size(0))
   * (0,0)
   *
   * scala> Range.linear(0, 10).bounds(Size(50))
   * (0,5)
   *
   * scala> Range.linear(0, 10).bounds(Size(99))
   * (0,10)
   * }}}
   */
  def linear[A : Integral](x: A, y: A): Range[A] =
    linearFrom(x, x, y)

  /**
   * Construct a range which scales the second bound relative to the size
   * parameter.
   *
   * {{{
   * scala> Range.linearFrom(0, -10, 10).bounds(Size(0))
   * (0,0)
   *
   * scala> Range.linearFrom(0, -10, 20).bounds(Size(50))
   * (-5,10)
   *
   * scala> Range.linearFrom(0, -10, 20).bounds(Size(20))
   * (-10,20)
   * }}}
   */
  def linearFrom[A : Integral](z: A, x: A, y: A): Range[A] =
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
  def linearFrac[A : Fractional](x: A, y: A): Range[A] =
    linearFracFrom(x, x, y)

  /**
   * Construct a range which scales the bounds relative to the size parameter.
   *
   * This works the same as [[linearFrom]], but for fractional values.
   */
  def linearFracFrom[A : Fractional : Ordering](z: A, x: A, y: A): Range[A] =
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
  def clamp[A](x: A, y: A, n: A)(implicit O: Ordering[A]) =
    if (O.gt(x, y))
      O.min(x, O.max(y, n))
    else
      O.min(y, O.max(x, n))

  /** Scale an integral linearly with the size parameter. */
  def scaleLinear[A](sz: Size, z: A, n: A)(implicit I: Integral[A]): A =
    I.plus(z, I.quot(I.times(I.minus(n, z), I.fromInt(sz.value)), I.fromInt(99)))

  /** Scale a fractional number linearly with the size parameter. */
  def scaleLinearFrac[A](sz: Size, z: A, n: A)(implicit F: Fractional[A]): A =
    F.plus(z, F.times(F.minus(n, z), F.div(F.fromInt(sz.value), F.fromInt(99))))
}
