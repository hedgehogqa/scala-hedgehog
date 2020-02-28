package hedgehog

/**
 * We have our own FP predef for 2 reasons.
 *
 * 1. The obvious political reasons. I don't think there are any really good reasons to need more than one
 * implementation of this library (if we do our job correctly).
 *
 * Probably more importantly:
 *
 * 2. Library dependencies _do_ have a cost. Especially in the JVM world where we insist of relying on binary
 * compatibility.
 */
package object predef {

  implicit def EitherOps[L, R](e: Either[L, R]): EitherOps[L, R] =
    new EitherOps(e)

  type State[S, A] = StateT[Identity, S, A]

  def State: StateTOpt[Identity] =
    new StateTOpt[Identity] {}

  def stateT[M[_]]: StateTOpt[M] =
    new StateTOpt[M] {}

  def some[A](a: A): Option[A] =
    Some(a)

  def findMap[A, B](fa: LazyList[A])(f: A => Option[B]): Option[B] = {
    // FIXME This should be tailrec but we seem to hit this bug
    // https://github.com/scala/bug/issues/9647
    var l = fa
    var o: Option[B] = null
    while (o == null) {
      l match {
        case LazyList.Nil() =>
          o = None
        case LazyList.Cons(h, t) =>
          f(h()) match {
            case Some(b) =>
              o = Some(b)
            case None =>
              l = t()
          }
      }
    }
    o
  }

  /** Performs the action `n` times, returning the list of results. */
  def replicateM[M[_], A](n: Int, fa: M[A])(implicit F: Applicative[M]): M[List[A]] =
    sequence(List.fill(n)(fa))

  /** Strict sequencing in an applicative functor `M` that ignores the value in `fa`. */
  def sequence[M[_], A](fa: List[M[A]])(implicit F: Applicative[M]): M[List[A]] =
    traverse(fa)(identity)

  def traverse[M[_], A, B](fa: List[A])(f: A => M[B])(implicit F: Applicative[M]): M[List[B]] =
    fa match {
      case Nil =>
        F.point(Nil)
      case h :: t =>
        F.ap(traverse(t)(f))(F.ap(f(h))(F.point((h2 : B) => (t2 : List[B]) => h2 :: t2)))
    }

  type Id[A] = A

  implicit val IdMonad: Monad[Id] =
    new Monad[Id] with StackSafeMonad[Id] {
      override def bind[A, B](fa: Id[A])(f: A => Id[B]): Id[B] = f(fa)
      override def point[A](a: => A): Id[A] = a
      override def ap[A, B](fa: => Id[A])(f: => Id[A => B]): Id[B] = f(fa)
    }
}
