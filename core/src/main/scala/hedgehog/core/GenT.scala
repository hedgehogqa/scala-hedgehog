package hedgehog.core

import hedgehog._
import hedgehog.predef._

/**
 * Generator for random values of `A`.
 */
case class GenT[A](run: (Size, Seed) => Tree[(Seed, Option[A])]) {

  def map[B](f: A => B): GenT[B] =
    GenT((size, seed) => run(size, seed).map(t => t.copy(_2 = t._2.map(f))))

  def flatMap[B](f: A => GenT[B]): GenT[B] =
    GenT((size, seed) => run(size, seed).flatMap(x =>
      x._2.fold(Tree.TreeApplicative.point(x.copy(_2 = Option.empty[B])))(a => f(a).run(size, x._1))
    ))

  def mapTree[B](f: Tree[(Seed, Option[A])] => Tree[(Seed, Option[B])]): GenT[B] =
    GenT((size, seed) => f(run(size, seed)))

  /**********************************************************************/
  // Shrinking

  /**
   * Apply a shrinking function to a generator.
   */
  def shrink(f: A => List[A]): GenT[A] =
    mapTree(_.expand(x =>
      x._2.fold(List.empty[(Seed, Option[A])])(a => f(a).map(y => (x._1, Some(y))))
    ))

  /**
   * Throw away a generator's shrink tree.
   */
  def prune: GenT[A] =
    mapTree(_.prune)

  /**********************************************************************/
  // Combinators - Property

  def log(name: Name): PropertyT[A] =
    // TODO Add better render, although I don't really like Show
    forAllWithLog(x => ForAll(name, x.toString))

  def forAll: PropertyT[A] =
    // TODO Add better render, although I don't really like Show
    forAllWithLog(x => x.toString)

  def forAllWithLog(f: A => Log): PropertyT[A] =
    for {
      x <- propertyT.fromGen(this)
      _ <- propertyT.writeLog(f(x))
    } yield x

  // Different from Haskell version, which uses the MonadGen typeclass
  def lift: PropertyT[A] =
    propertyT.fromGen(this)

  /**********************************************************************/
  // Combinators - Size

  /**
   * Override the size parameter. Returns a generator which uses the given size
   * instead of the runtime-size parameter.
   */
  def resize(size: Size): GenT[A] =
    if (size.value < 0)
      sys.error("Hedgehog.Random.resize: negative size")
    else
      GenT((_, seed) => run(size, seed))

  /**
   * Adjust the size parameter by transforming it with the given function.
   */
  def scale(f: Size => Size): GenT[A] =
    Gen.sized(n => resize(f(n)))

  /**
   * Make a generator smaller by scaling its size parameter.
   */
  def small: GenT[A] =
    scale(_.golden)

  /**********************************************************************/
  // Combinators - Conditional

  /**
   * Discards the generator if the generated value does not satisfy the predicate.
   */
  def ensure(p: A => Boolean): GenT[A] =
    this.flatMap(x => if (p(x)) Gen.constant(x) else Gen.discard)

  /**
   * Generates a value that satisfies a predicate.
   *
   * We keep some state to avoid looping forever.
   * If we trigger these limits then the whole generator is discarded.
   */
  def filter(p: A => Boolean): GenT[A] = {
    def try_(k: Int): GenT[A] =
      if (k > 100)
        Gen.discard
      else
        this.scale(s => Size(2 * k + s.value)).flatMap(x =>
          if (p(x))
            Gen.constant(x)
          else
            try_(k + 1)
        )
    try_(0)
  }

  /**********************************************************************/
  // Combinators - Collections

  /** Generates a 'None' some of the time. */
  def option: GenT[Option[A]] =
    Gen.sized(size =>
      Gen.frequency1(
        2 -> Gen.constant(Option.empty[A])
      , 1 + size.value -> this.map(some)
      )
    )

  /** Generates a list using a 'Range' to determine the length. */
  def list(range: Range[Int]): GenT[List[A]] =
    Gen.sized(size =>
      Gen.integral_(range, _.toInt).flatMap(k => replicateM[GenT, A](k, this))
        .shrink(Shrink.list)
        .ensure(Range.atLeast(range.lowerBound(size), _))
    )
}

abstract class GenImplicits1 {

  implicit def GenFunctor: Functor[GenT] =
    new Functor[GenT] {
      override def map[A, B](fa: GenT[A])(f: A => B): GenT[B] =
        fa.map(f)
    }
}

abstract class GenImplicits2 extends GenImplicits1 {

  implicit def GenApplicative: Applicative[GenT] =
    new Applicative[GenT] {
      def point[A](a: => A): GenT[A] =
        GenT((_, s) => Tree.TreeApplicative.point((s, Some(a))))
      override def ap[A, B](fa: => GenT[A])(f: => GenT[A => B]): GenT[B] =
        GenT((size, seed) => {
          val f2 = f.run(size, seed)
          val fa2 = fa.run(size, f2.value._1)
          Applicative.zip(fa2, f2).map { case ((seed2, oa), (_, o)) =>
            (seed2, o.flatMap(y => oa.map(y(_))))
          }
        })
    }
}

object GenT extends GenImplicits2 {

  implicit def GenMonad: Monad[GenT] =
    new Monad[GenT] {

     override def map[A, B](fa: GenT[A])(f: A => B): GenT[B] =
       fa.map(f)

     override def point[A](a: => A): GenT[A] =
       GenApplicative.point(a)

     override def ap[A, B](fa: => GenT[A])(f: => GenT[A => B]): GenT[B] =
       GenApplicative.ap(fa)(f)

      override def bind[A, B](fa: GenT[A])(f: A => GenT[B]): GenT[B] =
        fa.flatMap(f)
    }
}
