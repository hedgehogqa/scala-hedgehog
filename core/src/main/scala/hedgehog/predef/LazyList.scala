package hedgehog.predef

/**
 * A _very_ naive lazy-list.
 * Unfortunately using Scala `Stream` results in the head being evaluated prematurely for shrinking.
 */
sealed trait LazyList[A] {

  import LazyList._

  def map[B](f: A => B): LazyList[B] =
    this match {
      case Nil() =>
        nil
      case Cons(h, t) =>
        Cons(() => f(h()), () => t().map(f))
    }

  def ++(b: LazyList[A]): LazyList[A] =
    this match {
      case Nil() =>
        b
      case Cons(h, t) =>
        Cons(h, () => t() ++ b)
    }
}

object LazyList {

  case class Cons[A](head: () => A, tail: () => LazyList[A]) extends LazyList[A]
  case class Nil[A]() extends LazyList[A]

  def nil[A]: LazyList[A] =
    Nil()

  def cons[A](h: => A, t: => LazyList[A]): LazyList[A] =
    Cons(() => h, () => t)

  def apply[A](l: A*): LazyList[A] =
    fromList(l.toList)

  def fromList[A](l: List[A]): LazyList[A] =
    l.foldRight(nil[A])(cons(_, _))
}
