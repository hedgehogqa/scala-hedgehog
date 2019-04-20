package hedgehog.core

import hedgehog.predef._

/**
 * NOTE: This differs from the Haskell version by not having an effect on the `Node` for performance reasons.
 * See `haskell-difference.md` for more information.
 *
 * FIXME The `LazyList` here is critical to avoid running extra tests during shrinking.
 * The alternative might be something like:
 * https://github.com/hedgehogqa/scala-hedgehog/compare/topic/issue-66-lazy-shrinking
 */
case class Tree[M[_], A](value: A, children: M[LazyList[Tree[M, A]]]) {

  def map[B](f: A => B)(implicit F: Functor[M]): Tree[M, B] =
    Tree.TreeFunctor[M].map(this)(f)

  def flatMap[B](f: A => Tree[M, B])(implicit F: Monad[M]): Tree[M, B] =
    Tree.TreeMonad[M].bind(this)(f)

  def expand(f: A => List[A])(implicit F: Applicative[M]): Tree[M, A] =
    Tree(
      this.value, F.map(this.children)(_.map(_.expand(f)) ++ Tree.unfoldForest(identity[A], f, this.value)(F))
    )

  def prune(implicit F: Applicative[M]): Tree[M, A] =
    Tree(this.value, F.point(LazyList()))
}

abstract class TreeImplicits1 {

  implicit def TreeFunctor[M[_]](implicit F: Functor[M]): Functor[Tree[M, ?]] =
    new Functor[Tree[M, ?]] {
      override def map[A, B](fa: Tree[M, A])(f: A => B): Tree[M, B] =
        Tree(f(fa.value), F.map(fa.children)(_.map(_.map(f))))
    }
}

abstract class TreeImplicits2 extends TreeImplicits1 {

  implicit def TreeApplicative[M[_]](implicit F: Monad[M]): Applicative[Tree[M, ?]] =
    new Applicative[Tree[M, ?]] {
      def point[A](a: => A): Tree[M, A] =
        Tree(a, F.point(LazyList()))
      def ap[A, B](fa: => Tree[M, A])(f: => Tree[M, A => B]): Tree[M, B] =
        // FIX This isn't ideal, but if it's good enough for the Haskell implementation it's good enough for us
        // https://github.com/hedgehogqa/haskell-hedgehog/pull/173
        Tree.TreeMonad[M].bind(f)(ab =>
        Tree.TreeMonad[M].bind(fa)(a =>
          point(ab(a))
        ))
    }
}

object Tree extends TreeImplicits2 {

  implicit def TreeMonad[M[_]](implicit F: Monad[M]): Monad[Tree[M, ?]] =
    new Monad[Tree[M, ?]] {
      override def map[A, B](fa: Tree[M, A])(f: A => B): Tree[M, B] =
        fa.map(f)
      override def point[A](a: => A): Tree[M, A] =
        TreeApplicative(F).point(a)
      override def bind[A, B](fa: Tree[M, A])(f: A => Tree[M, B]): Tree[M, B] = {
        val y = f(fa.value)
        Tree(
          y.value, F.bind(fa.children)(x => F.map(y.children)(ys => x.map(_.flatMap(f)) ++ ys))
        )
      }
    }

  def unfoldTree[M[_], A, B](f: B => A, g: B => List[B], x: B)(implicit F: Applicative[M]): Tree[M, A] =
    Tree(f(x), F.point(unfoldForest(f, g, x)))

  def unfoldForest[M[_], A, B](f: B => A, g: B => List[B], x: B)(implicit F: Applicative[M]): LazyList[Tree[M, A]] =
    LazyList.fromList(g(x).map(y => unfoldTree(f, g, y)(F)))
}

