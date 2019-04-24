package hedgehog.predef

trait Functor[F[_]] {

  def map[A, B](fa: F[A])(f: A => B): F[B]
}

trait Applicative[F[_]] extends Functor[F] {

  def point[A](a: => A): F[A]

  def ap[A, B](fa: => F[A])(f: => F[A => B]): F[B]

  override def map[A, B](fa: F[A])(f: A => B): F[B] =
    ap(fa)(point(f))
}

object Applicative {

  def zip[F[_], A, B](fa: => F[A], f: => F[B])(implicit F: Applicative[F]): F[(A, B)] =
    F.ap(fa)(F.map(f)(b => (a: A) => (a, b)))

  def ap[F[_], A, B](fa: => F[A])(f: => F[A => B])(implicit F: Monad[F]): F[B] =
    F.bind(f)(x => F.map(fa)(x))
}

trait Monad[F[_]] extends Applicative[F] {

  def bind[A, B](fa: F[A])(f: A => F[B]): F[B]
}
