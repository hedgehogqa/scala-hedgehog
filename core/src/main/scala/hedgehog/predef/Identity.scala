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
}
