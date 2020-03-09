package hedgehog.core

import hedgehog.Size
import hedgehog.predef._

trait MonadGenT[M[_]] {

  def lift[A](gen: GenT[A]): M[A]

  def scale[A](gen: M[A], f: Size => Size): M[A]

  def shrink[A](gen: M[A], f: A => List[A]): M[A]
}

object MonadGenT {

  implicit def GenMonadGenT: MonadGenT[GenT] =
    new MonadGenT[GenT] {

      def lift[A](gen: GenT[A]): GenT[A] =
        gen

      def scale[A](gen: GenT[A], f: Size => Size): GenT[A] =
        gen.scale(f)

      def shrink[A](gen: GenT[A], f: A => List[A]): GenT[A] =
        gen.shrink(f)
    }

  implicit def StateTMonadGenT[M[_], S](implicit F: Functor[M], G: MonadGenT[M]): MonadGenT[StateT[M, S, ?]] =
    new MonadGenT[StateT[M, S, ?]] {

      def lift[A](gen: GenT[A]): StateT[M, S, A] =
        StateT(s => F.map(G.lift(gen))(a => (s, a)))

      def scale[A](gen: StateT[M, S, A], f: Size => Size): StateT[M, S, A] =
        gen.hoist(a =>  G.scale(a, f))

      def shrink[A](gen: StateT[M, S, A], f: A => List[A]): StateT[M, S, A] =
        gen.hoist(s => G.shrink(s, x => f(x._2).map(x._1 -> _)))
    }
}
