package hedgehog

import hedgehog.core._
import hedgehog.predef._

trait GenTOps[M[_]] {

  /**********************************************************************/
  // Combinators - Size

  /**
   * Construct a generator that depends on the size parameter.
   */
  def sized[A](f: Size => GenT[M, A]): GenT[M, A] =
    GenT((size, seed) => f(size).run(size, seed))

  /**********************************************************************/
  // Combinators - Integral

  /**
   * Generates a random integral number in the given `[inclusive,inclusive]` range.
   *
   * When the generator tries to shrink, it will shrink towards the
   * [[Range.origin]] of the specified [[Range]].
   *
   * For example, the following generator will produce a number between `1970`
   * and `2100`, but will shrink towards `2000`:
   *
   * {{{
   * Gen.integral(Range.constantFrom(2000, 1970, 2100))
   * }}}
   *
   * Some sample outputs from this generator might look like:
   *
   * {{{
   * === Outcome ===
   * 1973
   * === Shrinks ===
   * 2000
   * 1987
   * 1980
   * 1976
   * 1974
   *
   * === Outcome ===
   * 2061
   * === Shrinks ===
   * 2000
   * 2031
   * 2046
   * 2054
   * 2058
   * 2060
   * }}}
   */
  def integral[A : Integral](range: Range[A])(implicit F: Monad[M]): GenT[M, A] =
    integral_[A](range).shrink(Shrink.towards(range.origin, _))

  /**
   * Generates a random integral number in the `[inclusive,inclusive]` range.
   *
   * ''This generator does not shrink.''
   */
  def integral_[A](range: Range[A])(implicit F: Monad[M], I: Integral[A]): GenT[M, A] =
    GenT((size, seed) => {
      val (x, y) = range.bounds(size)
      val (s2, a) = seed.chooseLong(I.toLong(x), I.toLong(y))
      // TODO Integral doesn't support Long, can we only use int seeds? :(
      GenT.GenApplicative.point(I.fromInt(a.toInt)).run(size, s2)
    })

  def char(lo: Char, hi: Char)(implicit F: Monad[M]): GenT[M, Char] =
    integral[Long](Range.constant(lo.toLong, hi.toLong)).map(_.toChar)

  /**********************************************************************/
  // Combinators - Fractional

  def double(range: Range[Double])(implicit F: Monad[M]): GenT[M, Double] =
    double_(range).shrink(Shrink.towardsFloat(range.origin, _))

  def double_(range: Range[Double])(implicit F: Monad[M]): GenT[M, Double] =
    GenT((size, seed) => {
      val (x, y) = range.bounds(size)
      val (s2, a) = seed.chooseDouble(x, y)
      GenT.GenApplicative[M].point(a).run(size, s2)
    })

  /**********************************************************************/
  // Combinators - Choice

  /**
   * Randomly selects one of the elements in the list.
   *
   * This generator shrinks towards the first element in the list.
   */
  def element[A](x: A, xs: List[A])(implicit F: Monad[M]): GenT[M, A] =
    integral[Int](Range.constant(0, xs.length)).map(i => (x :: xs)(i))

  /**
   * Randomly selects one of the generators in the list.
   *
   * This generator shrinks towards the first generator in the list.
   */
  def choice[A](x: GenT[M, A], xs: List[GenT[M, A]])(implicit F: Monad[M]): GenT[M, A] =
    integral[Int](Range.constant(0, xs.length)).flatMap(i => (x :: xs)(i))

  /**********************************************************************/
  // Combinators - Conditional

  /**
   * Discards the whole generator.
   */
  def discard[A](implicit F: Monad[M]): GenT[M, A] =
    GenT((_, seed) => Tree.TreeApplicative(F).point((seed, None)))
}
