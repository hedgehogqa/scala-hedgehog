package hedgehog.predef

/**
 * The simplest form of monad that we can use with hedgehog.
 *
 * NOTE: We _must_ use some form of call-by-need value for our M in GenT[M, ?] to avoid
 * stack overflows.
 */
abstract class Identity[A] {

  def value: A
}

object Identity {

  def apply[A](a: => A): Identity[A] =
    new Identity[A] {
      def value: A =
        a
    }

  implicit def IdentityMonad: Monad[Identity] =
    new Monad[Identity] {

      // NOTE: It's critical to override the free Applicative version, otherwise we get stack overflows
      override def map[A, B](fa: Identity[A])(f: A => B) =
        Identity(f(fa.value))

      override def point[A](a: => A): Identity[A] =
        Identity(a)

      override def bind[A, B](fa: Identity[A])(f: A => Identity[B]): Identity[B] =
        Identity(f(fa.value).value)
    }
}
