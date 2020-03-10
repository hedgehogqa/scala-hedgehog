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
case class Tree[A](value: A, children: Identity[LazyList[Tree[A]]]) {

  def map[B](f: A => B): Tree[B] =
    Tree.TreeFunctor.map(this)(f)

  def flatMap[B](f: A => Tree[B]): Tree[B] =
    Tree.TreeMonad.bind(this)(f)

  def expand(f: A => List[A]): Tree[A] =
    Tree(
      this.value, this.children.map(_.map(_.expand(f)) ++ Tree.unfoldForest(identity[A], f, this.value))
    )

  def prune: Tree[A] =
    Tree(this.value, Identity(LazyList()))
}

abstract class TreeImplicits1 {

  implicit def TreeFunctor: Functor[Tree] =
    new Functor[Tree] {
      override def map[A, B](fa: Tree[A])(f: A => B): Tree[B] =
        Tree(f(fa.value), fa.children.map(_.map(_.map(f))))
    }
}

abstract class TreeImplicits2 extends TreeImplicits1 {

  implicit def TreeApplicative: Applicative[Tree] =
    new Applicative[Tree] {
      def point[A](a: => A): Tree[A] =
        Tree(a, Identity(LazyList()))
      def ap[A, B](fa: => Tree[A])(f: => Tree[A => B]): Tree[B] =
        Tree(
          f.value(fa.value)
        , Identity(
             f.children.value.map(fl => ap(fa)(fl))
          ++ fa.children.value.map(fal => ap(fal)(f))
          )
        )
    }
}

object Tree extends TreeImplicits2 {

  implicit def TreeMonad: Monad[Tree] =
    new Monad[Tree] {
      override def map[A, B](fa: Tree[A])(f: A => B): Tree[B] =
        fa.map(f)
      override def point[A](a: => A): Tree[A] =
        TreeApplicative.point(a)
      override def ap[A, B](fa: => Tree[A])(f: => Tree[A => B]): Tree[B] =
        TreeApplicative.ap(fa)(f)
      override def bind[A, B](fa: Tree[A])(f: A => Tree[B]): Tree[B] = {
        val y = f(fa.value)
        Tree(
          y.value, fa.children.flatMap(x => y.children.map(ys => x.map(_.flatMap(f)) ++ ys))
        )
      }
    }

  def unfoldTree[A, B](f: B => A, g: B => List[B], x: B): Tree[A] =
    Tree(f(x), Identity(unfoldForest(f, g, x)))

  def unfoldForest[A, B](f: B => A, g: B => List[B], x: B): LazyList[Tree[A]] =
    LazyList.fromList(g(x).map(y => unfoldTree(f, g, y)))
}

