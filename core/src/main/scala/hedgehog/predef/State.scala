package hedgehog.predef

case class StateT[M[_], S, A](run: S => M[(S, A)]) {

  def eval(s: S)(implicit F: Functor[M]): M[A] =
    F.map(run(s))(_._2)

  def exec(s: S)(implicit F: Functor[M]): M[S] =
    F.map(run(s))(_._1)

  def map[B](f: A => B)(implicit F: Functor[M]): StateT[M, S, B] =
    StateT(s => F.map(run(s))(x => x._1 -> f(x._2)))

  def flatMap[B](f: A => StateT[M, S, B])(implicit F: Monad[M]): StateT[M, S, B] =
    StateT(s => F.bind(run(s))(x => f(x._2).run(x._1)))

  def hoist[N[_], B](f: M[(S, A)] => N[(S, B)]): StateT[N, S, B] =
    StateT(s => f(run(s)))
}

abstract class StateTImplicits1 {

  implicit def StateTFunctor[M[_], S](implicit F: Functor[M]): Functor[StateT[M, S, ?]] =
    new Functor[StateT[M, S, ?]] {
      override def map[A, B](fa: StateT[M, S, A])(f: A => B): StateT[M, S, B] =
        fa.map(f)
    }
}

abstract class StateTImplicits2 extends StateTImplicits1 {

  implicit def StateTApplicative[M[_], S](implicit F: Monad[M]): Applicative[StateT[M, S, ?]] =
    new Applicative[StateT[M, S, ?]] {

      def point[A](a: => A): StateT[M, S, A] =
        StateT(s => F.point((s, a)))

      def ap[A, B](fa: => StateT[M, S, A])(f: => StateT[M, S, A => B]): StateT[M, S, B] =
        StateT.StateTMonad[M, S].bind(f)(ab =>
        StateT.StateTFunctor[M, S].map(fa)(a =>
          ab(a)
        ))
    }
}

object StateT extends StateTImplicits2 {

  implicit def StateTMonad[M[_], S](implicit F: Monad[M]): Monad[StateT[M, S, ?]] =
    new Monad[StateT[M, S, ?]] with StackSafeMonad[StateT[M, S, ?]] {

      override def map[A, B](fa: StateT[M, S, A])(f: A => B): StateT[M, S, B] =
        fa.map(f)

      override def point[A](a: => A): StateT[M, S, A] =
        StateTApplicative(F).point(a)

      override def ap[A, B](fa: => StateT[M, S, A])(f: => StateT[M, S, A => B]): StateT[M, S, B] =
        StateTApplicative(F).ap(fa)(f)

      override def bind[A, B](fa: StateT[M, S, A])(f: A => StateT[M, S, B]): StateT[M, S, B] =
        fa.flatMap(f)

      // FIXME: This is not stack safe.
      override def tailRecM[A, B](a: A)(f: A => StateT[M, S, Either[A, B]]): StateT[M, S, B] =
        bind(f(a)) {
          case Left(value) => tailRecM(value)(f)
          case Right(value) => point(value)
        }
    }

}

trait StateTOpt[M[_]] {

  // Just to give ol' scala type-inference a helping hand...
  def traverse[S, A, B](l: List[A])(f: A => StateT[M, S, B])(implicit F: Monad[M]): StateT[M, S, List[B]] =
    hedgehog.predef.traverse[StateT[M, S, ?], A, B](l)(f)

  def apply[S, A](f: S => M[(S, A)]): StateT[M, S, A] =
    StateT(f)

  def state[S, A](f: S => (S, A))(implicit F: Applicative[M]): StateT[M, S, A] =
    StateT(s => F.point(f(s)))

  def lift[S, A](m: M[A])(implicit F: Functor[M]): StateT[M, S, A] =
    StateT(s => F.map(m)(a => (s, a)))

  def point[S, A](a: A)(implicit F: Applicative[M]): StateT[M, S, A] =
    StateT(s => F.point((s, a)))

  def get[S](implicit F: Applicative[M]): StateT[M, S, S] =
    StateT(s => F.point((s, s)))

  def put[S](s: S)(implicit F: Applicative[M]): StateT[M, S, Unit] =
    StateT(_ => F.point((s, ())))

  def modify[S](f: S => S)(implicit F: Applicative[M]): StateT[M, S, Unit] =
    StateT(s => F.point((f(s), ())))
}
