package hedgehog.predef

/**
 * The simplest form of monad that we can use with hedgehog.
 *
 * NOTE: We _must_ use some form of call-by-need value for our M in GenT[M, ?] to avoid
 * stack overflows.
 */
abstract class Identity[A] {

  def value: A

  def map[B](f: A => B): Identity[B] =
    Identity(f(value))

  def flatMap[B](f: A => Identity[B]): Identity[B] =
    Identity(f(value).value)
}

object Identity {

  def apply[A](a: => A): Identity[A] =
    new Identity[A] {
      def value: A =
        a
    }

  implicit def IdentityMonad: Monad[Identity] =
    new Monad[Identity] with StackSafeMonad[Identity] {

      // NOTE: It's critical to override the free Applicative version, otherwise we get stack overflows
      override def map[A, B](fa: Identity[A])(f: A => B) =
        Identity(f(fa.value))

      override def point[A](a: => A): Identity[A] =
        Identity(a)

      override def ap[A, B](fa: => Identity[A])(f: => Identity[A => B]): Identity[B] =
        Identity(f.value(fa.value))

      override def bind[A, B](fa: Identity[A])(f: A => Identity[B]): Identity[B] =
        Identity(f(fa.value).value)

      // FIXME: This is not stack safe.
      override def tailRecM[A, B](a: A)(f: A => Identity[Either[A, B]]): Identity[B] =
        bind(f(a)) {
          case Left(value) => tailRecM(value)(f)
          case Right(value) => point(value)
        }
    }
}
