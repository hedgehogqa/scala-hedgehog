package hedgehog

import scalaz._, Scalaz._

case class Gen[M[_], A](run: Seed => M[(Seed, A)]) {

  def map[B](f: A => B)(implicit F: Functor[M]): Gen[M, B] =
    Gen(s => run(s).map(_.map(f)))

  def flatMap[B](f: A => Gen[M, B])(implicit F: Monad[M]): Gen[M, B] =
    Gen(s1 => run(s1).flatMap(a => f(a._2).run(a._1)))

  // TODO Name?
  def mapM[N[_], B](f: M[(Seed, A)] => N[(Seed, B)]): Gen[N, B] =
    Gen(s => f(run(s)))

  def mapN[N[_], B](f: M ~> N): Gen[N, A] =
    Gen(s => f(run(s)))
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
        Gen(s => F.point((s, a)))
      def ap[A, B](fa: => Gen[M, A])(f: => Gen[M, A => B]): Gen[M, B] =
        Gen(sb => F.bind(fa.run(sb))(sa =>
          f.run(sa._1).map(sab => (sab._1, sab._2(sa._2)))
        ))
    }
}

object Gen extends GenImplicits2 {

  implicit def GenMonad[M[_]](implicit F: Monad[M]): Monad[Gen[M, ?]] =
    new Monad[Gen[M, ?]] {
      override def map[A, B](fa: Gen[M, A])(f: A => B): Gen[M, B] =
        fa.map(f)
      override def point[A](a: => A): Gen[M, A] =
        GenApplicative(F).point(a)
      override def bind[A, B](fa: Gen[M, A])(f: A => Gen[M, B]): Gen[M, B] =
        fa.flatMap(f)
    }

  implicit def GenMonadTrans: MonadTrans[Gen] =
    new MonadTrans[Gen] {
      def liftM[G[_] : Monad, A](g: G[A]): Gen[G, A] =
        lift(g)
      def apply[G[_] : Monad]: Monad[Gen[G, ?]] =
        GenMonad
    }

  def lift[G[_] : Monad, A](g: G[A]): Gen[G, A] =
    Gen(s => g.map(a => (s, a)))
}

