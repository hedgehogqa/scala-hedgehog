package hedgehog

import hedgehog.core._
import hedgehog.predef._

trait GenTOps extends MonadGenOps[Gen] {

  /**********************************************************************/
  // Combinators

  /**********************************************************************/
  // Combinators - Integral

  def int(range: Range[Int]): GenT[Int] =
    integral(range, _.toInt)

  def short(range: Range[Short]): GenT[Short] =
    integral(range, _.toShort)

  def long(range: Range[Long]): GenT[Long] =
    integral(range, identity)

  def byte(range: Range[Byte]): GenT[Byte] =
    integral(range, _.toByte)

  def char(lo: Char, hi: Char): GenT[Char] =
    long(Range.constant(lo.toLong, hi.toLong)).map(_.toChar)

  /**********************************************************************/
  // Combinators - Enumeration

  /**
   * Generates a random boolean.
   *
   * _This generator shrinks to 'False'._
   */
  def boolean: GenT[Boolean] =
    element1(false, true)

  /**********************************************************************/
  // Combinators - Fractional

  def double(range: Range[Double]): GenT[Double] =
    double_(range).shrink(Shrink.towardsFloat(range.origin, _))

  def double_(range: Range[Double]): GenT[Double] =
    Gen.generate((size, seed) => {
      val (x, y) = range.bounds(size)
      seed.chooseDouble(x, y)
    })

  /**********************************************************************/
  // Combinators - Choice

  /**
   * Trivial generator that always produces the same element.
   */
  def constant[A](x: => A): GenT[A] =
    GenT.GenApplicative.point(x)

  /**
   * Randomly selects one of the elements in the list.
   *
   * This generator shrinks towards the first element in the list.
   */
  def element1[A](x: A, xs: A*): GenT[A] =
    element(x, xs.toList)

  /**
   * Randomly selects one of the elements in the list.
   *
   * This generator shrinks towards the first element in the list.
   */
  def element[A](x: A, xs: List[A]): GenT[A] =
    int(Range.constant(0, xs.length)).map(i => (x :: xs)(i))

  /**
   * Randomly selects one of the elements in the list.
   *
   * This generator shrinks towards the first element in the list.
   *
   * WARNING: This may throw an exception if the list is empty,
   * please use one of the other `element` variants if possible
   */
  def elementUnsafe[A](xs: List[A]): GenT[A] =
    xs match {
      case Nil =>
        sys.error("element: used with empty list")
      case h :: t =>
        element(h, t)
    }

  /**
   * Randomly selects one of the generators in the list.
   *
   * This generator shrinks towards the first generator in the list.
   */
  def choice1[A](x: GenT[A], xs: GenT[A]*): GenT[A] =
    choice(x, xs.toList)

  /**
   * Randomly selects one of the generators in the list.
   *
   * This generator shrinks towards the first generator in the list.
   */
  def choice[A](x: GenT[A], xs: List[GenT[A]]): GenT[A] =
    int(Range.constant(0, xs.length)).flatMap(i => (x :: xs)(i))

  /**
   * Uses a weighted distribution to randomly select one of the generators in the list.
   *
   * This generator shrinks towards the first generator in the list.
   */
   def frequency1[A](a: (Int, GenT[A]), l: (Int, GenT[A])*): GenT[A] =
     frequency(a, l.toList)

   /**
    * Uses a weighted distribution to randomly select one of the generators in the list.
    *
    * This generator shrinks towards the first generator in the list.
    */
   def frequency[A](a: (Int, GenT[A]), l: List[(Int, GenT[A])]): GenT[A] = {
     @annotation.tailrec
     def pick(n: Long, x: (Int, GenT[A]), xs: List[(Int, GenT[A])]): GenT[A] =
       if (n <= x._1)
         x._2
       else
         xs match {
           case Nil =>
             sys.error("Invariant: frequency hits an impossible code path")
           case h :: t =>
             pick(n - x._1, h, t)
         }
     val total = (a :: l).map(_._1.toLong).sum
     for {
       n <- long(Range.constant(1, total))
       x <- pick(n, a, l)
     } yield x
   }

  /**
    * Uses a weighted distribution to randomly select one of the generators in the list.
    *
    * This generator shrinks towards the first generator in the list.
    *
    * WARNING: This may throw an exception if the list is empty,
    * please use one of the other `frequency` variants if possible.
    */
  def frequencyUnsafe[A](xs: List[(Int, GenT[A])]): GenT[A] =
    xs match {
      case Nil =>
        sys.error("frequency: used with empty list")
      case h :: t =>
        frequency(h, t)
    }
}

trait MonadGenOps[M[_]] {

  /**********************************************************************/
  // Combinators

  /**
   * Runs a `Option` generator until it produces a `Some`.
   *
   * This is implemented using `filter` and has the same caveats.
   */
  def fromSome[A](gen: M[Option[A]])(implicit F: Monad[M], G: MonadGen[M]): M[A] =
    F.map(filter(gen)(_.isDefined))(
      _.getOrElse(sys.error("fromSome: internal error, unexpected None"))
    )

  /**
   * Construct a generator that depends on the size parameter.
   */
  def generate[A](f: (Size, Seed) => (Seed, A))(implicit G: MonadGen[M]): M[A] =
    G.lift(GenT((size, seed) => {
      val (s2, a) = f(size, seed)
      Tree.TreeApplicative.point((s2, some(a)))
    }))

  /** Generates a list using a 'Range' to determine the length. */
  def list[A](gen: M[A], range: Range[Int])(implicit F: Monad[M], G: MonadGen[M]): M[List[A]] =
    sized(size =>
      ensure(
        G.shrink(
          F.bind(integral_(range, _.toInt))(k => replicateM[M, A](k, gen))
        , Shrink.list
        )
      , Range.atLeast(range.lowerBound(size), _)
      )
    )

  /**********************************************************************/
  // Combinators - Size

  /**
   * Construct a generator that depends on the size parameter.
   */
  def sized[A](f: Size => M[A])(implicit F: Monad[M], G: MonadGen[M]): M[A] =
    F.bind(generate((size, seed) => (seed, size)))(f)

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
  def integral[A : Integral](range: Range[A], fromLong : Long => A)(implicit F: MonadGen[M]): M[A] =
    F.shrink(integral_[A](range, fromLong), Shrink.towards(range.origin, _))

  /**
   * Generates a random integral number in the `[inclusive,inclusive]` range.
   *
   * ''This generator does not shrink.''
   */
  def integral_[A](range: Range[A], fromLong : Long => A)(implicit G: MonadGen[M], I: Integral[A]): M[A] =
    generate((size, seed) => {
      val (x, y) = range.bounds(size)
      val (s2, a) = seed.chooseLong(I.toLong(x), I.toLong(y))
      (s2, fromLong(a))
    })

  /**********************************************************************/
  // Combinators - Conditional

  /**
   * Discards the whole generator.
   */
  def discard[A](implicit G: MonadGen[M]): M[A] =
    G.lift(
      GenT((_, seed) => Tree.TreeApplicative.point((seed, None)))
    )

  /**
   * Generates a value that satisfies a predicate.
   *
   * We keep some state to avoid looping forever.
   * If we trigger these limits then the whole generator is discarded.
   */
  def filter[A](gen: M[A])(p: A => Boolean)(implicit F: Monad[M], G: MonadGen[M]): M[A] = {
    def try_(k: Int): M[A] =
      if (k > 100)
        discard
      else
        F.bind(G.scale(gen, s => Size(2 * k + s.value)))(x =>
          if (p(x))
            F.point(x)
          else
            try_(k + 1)
        )
    try_(0)
  }

  /**
   * Discards the generator if the generated value does not satisfy the predicate.
   */
  def ensure[A](gen: M[A], p: A => Boolean)(implicit F: Monad[M], G: MonadGen[M]): M[A] =
    F.bind(gen)(x => if (p(x)) F.point(x) else discard)
}
