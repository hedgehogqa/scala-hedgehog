package hedgehog

import scalaz.{ Node => _, Tree => _, _ }

object GenTree {

  type T[M[_], A] = Gen[Tree[M, ?], A]


  /**
   * When all hope of type inference is gone
   */
  def applicative[M[_]](implicit F: Monad[M]): Applicative[Gen[Tree[M, ?], ?]] =
    Gen.GenApplicative[Tree[M, ?]](Tree.TreeMonad[M](F))

  /****** Shrink *******/

  def shrink[M[_] : Monad, A](f: A => List[A], g: Gen[Tree[M, ?], A]): Gen[Tree[M, ?], A] =
    // TODO a way to avoid the seed passing?
    g.mapM[Tree[M, ?], A](_.expand(x => f(x._2).map(y => (x._1, y))))

  def noShrink[M[_] : Monad, A](g: Gen[Tree[M, ?], A]): Gen[Tree[M, ?], A] =
    g.mapM[Tree[M, ?], A](_.prune)

  /****** Combinators *******/

  def integral_[M[_]](lo: Long, hi: Long)(implicit F: Applicative[M]): Gen[M, Long] =
    Gen(s => F.point(s.nextLong(lo, hi)))

  def integral[M[_] : Monad](lo: Long, hi: Long): Gen[Tree[M, ?], Long] =
    shrink(x => Shrink.towards(lo, x), integral_[Tree[M, ?]](lo, hi))

  def char[M[_] : Monad](lo: Char, hi: Char): Gen[Tree[M, ?], Char] =
    integral[M](lo.toLong, hi.toLong).map(_.toChar)

  def element[M[_], A](x: A, xs: List[A])(implicit F: Monad[M]): Gen[Tree[M, ?], A] =
    // TODO Need Int version of integral to avoid to toInt
    integral(0, xs.length)(F).map(i => (x :: xs)(i.toInt))

  def choice[M[_], A](x: Gen[Tree[M, ?], A], xs: List[Gen[Tree[M, ?], A]])(implicit F: Monad[M]): Gen[Tree[M, ?], A] =
    // TODO Need Int version of integral to avoid to toInt
    integral(0, xs.length)(F).flatMap(i => (x :: xs)(i.toInt))

  def list[M[_]: Monad, A](lo: Int, hi: Int, gen: GenTree.T[M, A]): GenTree.T[M, List[A]] =
    // TODO filterM, needs a MonadPlus for GenTree
    shrink[M, List[A]](Shrink.list, integral_[Tree[M, ?]](lo.toLong, hi.toLong).flatMap(k => applicative[M].replicateM(k.toInt, gen)))
}

