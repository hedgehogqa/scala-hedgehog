package hedgehog

import scalaz._, Scalaz._

/**
 * Generator for random values of `A`.
 */
case class Gen[M[_], A](run: (Size, Seed) => Tree[M, (Seed, Option[A])]) {

  def map[B](f: A => B)(implicit F: Functor[M]): Gen[M, B] =
    Gen((size, seed) => run(size, seed).map(_.map(_.map(f))))

  def flatMap[B](f: A => Gen[M, B])(implicit F: Monad[M]): Gen[M, B] =
    Gen((size, seed) => run(size, seed).flatMap(x =>
      x._2.cata(a => f(a).run(size, x._1), Tree.TreeApplicative(F).point(x.as(none)))
    ))

  def mapTree[N[_], B](f: Tree[M, (Seed, Option[A])] => Tree[N, (Seed, Option[B])]): Gen[N, B] =
    Gen((size, seed) => f(run(size, seed)))

  /**********************************************************************/
  // Shrinking

  /**
   * Apply a shrinking function to a generator.
   */
  def shrink(f: A => List[A])(implicit F: Monad[M]): Gen[M, A] =
    mapTree(_.expand(x =>
      x._2.cata(a => f(a).map(y => (x._1, Some(y))), Nil)
    ))

  /**
   * Throw away a generator's shrink tree.
   */
  def prune(implicit F: Monad[M]): Gen[M, A] =
    mapTree(_.prune)

  /**********************************************************************/
  // Combinators - Property

  def log(name: Name)(implicit F: Monad[M]): Property[M, A] =
    for {
      x <- Property.fromGen(this)
      // TODO Add better render, although I don't really like Show
      _ <- Property.writeLog[M](ForAll(name, x.toString))
    } yield x

  /**********************************************************************/
  // Combinators - Size

  /**
   * Override the size parameter. Returns a generator which uses the given size
   * instead of the runtime-size parameter.
   */
  def resize(size: Size): Gen[M, A] =
    if (size.value < 0)
      sys.error("Hedgehog.Random.resize: negative size")
    else
      Gen((_, seed) => run(size, seed))

  /**
   * Adjust the size parameter by transforming it with the given function.
   */
  def scale(f: Size => Size): Gen[M, A] =
    Gen.sized(n => resize(f(n)))

  /**
   * Make a generator smaller by scaling its size parameter.
   */
  def small: Gen[M, A] =
    scale(_.golden)

  /**********************************************************************/
  // Combinators

  def list(range: Range[Int])(implicit F: Monad[M]): Gen[M, List[A]] =
    // TODO filterM, needs a MonadPlus for Gen
    Gen.integral_[M, Int](range).flatMap(k => this.replicateM(k))
      .shrink(Shrink.list)
}

abstract class GenImplicits1 {

  implicit def GenFunctor[M[_]](implicit F: Functor[M]): Functor[Gen[M, ?]] =
    new Functor[Gen[M, ?]] {
      override def map[A, B](fa: Gen[M, A])(f: A => B): Gen[M, B] =
        fa.map(f)
    }
}

abstract class GenImplicits2 extends GenImplicits1 {

  implicit def GenApplicative[M[_]](implicit F: Monad[M]): Applicative[Gen[M, ?]] =
    new Applicative[Gen[M, ?]] {
      def point[A](a: => A): Gen[M, A] =
        Gen((_, s) => Tree.TreeApplicative(F).point((s, Some(a))))
      def ap[A, B](fa: => Gen[M, A])(f: => Gen[M, A => B]): Gen[M, B] =
        for {
          ab <- f
          a <- fa
        } yield ab(a)
    }
}

object Gen extends GenImplicits2 {

  /**********************************************************************/
  // Combinators - Size

  /**
   * Construct a generator that depends on the size parameter.
   */
  def sized[M[_], A](f: Size => Gen[M, A]): Gen[M, A] =
    Gen((size, seed) => f(size).run(size, seed))

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
  def integral[M[_] : Monad, A : Integral](range: Range[A]): Gen[M, A] =
    integral_[M, A](range).shrink(Shrink.towards(range.origin, _))

  /**
   * Generates a random integral number in the `[inclusive,inclusive]` range.
   *
   * ''This generator does not shrink.''
   */
  def integral_[M[_], A](range: Range[A])(implicit F: Monad[M], I: Integral[A]): Gen[M, A] =
    Gen((size, seed) => {
      val (x, y) = range.bounds(size)
      val (s2, a) = seed.chooseLong(I.toLong(x), I.toLong(y))
      // TODO Integral doesn't support Long, can we only use int seeds? :(
      I.fromInt(a.toInt).point[Gen[M, ?]].run(size, s2)
    })

  def char[M[_] : Monad](lo: Char, hi: Char): Gen[M, Char] =
    integral[M, Long](Range.constant(lo.toLong, hi.toLong)).map(_.toChar)

  /**********************************************************************/
  // Combinators - Fractional

  def double[M[_] : Monad](range: Range[Double]): Gen[M, Double] =
    double_[M](range).shrink(Shrink.towardsFloat(range.origin, _))

  def double_[M[_]](range: Range[Double])(implicit F: Monad[M]): Gen[M, Double] =
    Gen((size, seed) => {
      val (x, y) = range.bounds(size)
      val (s2, a) = seed.chooseDouble(x, y)
      a.point[Gen[M, ?]].run(size, s2)
    })

  /**********************************************************************/
  // Combinators - Choice

  /**
   * Randomly selects one of the elements in the list.
   *
   * This generator shrinks towards the first element in the list.
   */
  def element[M[_] : Monad, A](x: A, xs: List[A]): Gen[M, A] =
    integral[M, Int](Range.constant(0, xs.length)).map(i => (x :: xs)(i))

  /**
   * Randomly selects one of the generators in the list.
   *
   * This generator shrinks towards the first generator in the list.
   */
  def choice[M[_] : Monad, A](x: Gen[M, A], xs: List[Gen[M, A]]): Gen[M, A] =
    integral[M, Int](Range.constant(0, xs.length)).flatMap(i => (x :: xs)(i))

  /**********************************************************************/
  // Combinators - Conditional

  /**
   * Discards the whole generator.
   */
  def discard[M[_], A](implicit F: Applicative[M]): Gen[M, A] =
    Gen((_, seed) => Tree.TreeApplicative(F).point((seed, None)))
}
