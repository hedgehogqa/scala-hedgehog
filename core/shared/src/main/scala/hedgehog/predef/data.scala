package hedgehog.predef

trait Functor[F[_]] {

  def map[A, B](fa: F[A])(f: A => B): F[B]
}

object Functor {

  implicit def FunctorEither[L]: Functor[Either[L, *]] =
    new Functor[Either[L, *]] {

      override def map[A, B](fa: Either[L, A])(f: A => B): Either[L, B] =
        fa.rightMap(f)
    }
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

  implicit def ApplicativeEither[L]: Applicative[Either[L, *]] =
    new Applicative[Either[L, *]] {

      override def point[A](a: => A): Either[L, A] =
        Right(a)

      override def ap[A, B](fa: => Either[L, A])(f: => Either[L, A => B]): Either[L, B] =
        fa match {
          case Left(l) =>
            Left(l)
          case Right(a) =>
            f match {
              case Left(l) =>
                Left(l)
              case Right(ab) =>
                Right(ab(a))
            }
        }
   }
}

trait Monad[F[_]] extends Applicative[F] {

  def bind[A, B](fa: F[A])(f: A => F[B]): F[B]
}

object Monad {

  implicit def MonadEither[L]: Monad[Either[L, *]] =
    new Monad[Either[L, *]] {

      override def map[A, B](fa: Either[L, A])(f: A => B): Either[L, B] =
        fa.rightMap(f)

      override def point[A](a: => A): Either[L, A] =
        Right(a)

      override def ap[A, B](fa: => Either[L, A])(f: => Either[L, A => B]): Either[L, B] =
        Applicative.ApplicativeEither.ap(fa)(f)

      override def bind[A, B](fa: Either[L, A])(f: A => Either[L, B]): Either[L, B] =
        fa.rightFlatMap(f)
    }
}
