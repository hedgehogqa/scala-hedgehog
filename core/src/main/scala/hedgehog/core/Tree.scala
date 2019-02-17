package hedgehog.core

import hedgehog.predef._

case class Node[M[_], A](value: A, children: M[List[Tree[M, A]]]) {

  def map[B](f: A => B)(implicit F: Functor[M]): Node[M, B] =
    Node.NodeFunctor.map(this)(f)
}

/**
 * NOTE: This differs from the Haskell version by not having an effect on the `Node` for performance reasons.
 * See `haskell-difference.md` for more information.
 */
case class Tree[M[_], A](run: Node[M, A]) {

  def map[B](f: A => B)(implicit F: Functor[M]): Tree[M, B] =
    Tree(Node.NodeFunctor[M].map(run)(f))

  def flatMap[B](f: A => Tree[M, B])(implicit F: Monad[M]): Tree[M, B] =
    Tree.TreeMonad[M].bind(this)(f)

  def expand(f: A => List[A])(implicit F: Applicative[M]): Tree[M, A] =
    Tree(
      Node(run.value, F.map(run.children)(_.map(_.expand(f)) ++ Tree.unfoldForest(identity[A], f, run.value)(F)))
    )

  def prune(implicit F: Applicative[M]): Tree[M, A] =
    Tree(Node(run.value, F.point(List())))
}

object Node {

  implicit def NodeFunctor[M[_]](implicit F: Functor[M]): Functor[Node[M, ?]] =
    new Functor[Node[M, ?]] {
      override def map[A, B](fa: Node[M, A])(f: A => B): Node[M, B] =
        Node(f(fa.value), F.map(fa.children)(_.map(_.map(f))))
    }
}

abstract class TreeImplicits1 {

  implicit def TreeFunctor[M[_]](implicit F: Functor[M]): Functor[Tree[M, ?]] =
    new Functor[Tree[M, ?]] {
      override def map[A, B](fa: Tree[M, A])(f: A => B): Tree[M, B] =
        fa.map(f)
    }
}

abstract class TreeImplicits2 extends TreeImplicits1 {

  implicit def TreeApplicative[M[_]](implicit F: Monad[M]): Applicative[Tree[M, ?]] =
    new Applicative[Tree[M, ?]] {
      def point[A](a: => A): Tree[M, A] =
        Tree(Node(a, F.point(List())))
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
        val y = f(fa.run.value).run
        Tree(
          Node(y.value, F.bind(fa.run.children)(x => F.map(y.children)(ys => x.map(_.flatMap(f)) ++ ys)))
        )
      }
    }

  def unfoldTree[M[_], A, B](f: B => A, g: B => List[B], x: B)(implicit F: Applicative[M]): Tree[M, A] =
    Tree(Node(f(x), F.point(unfoldForest(f, g, x))))

  def unfoldForest[M[_], A, B](f: B => A, g: B => List[B], x: B)(implicit F: Applicative[M]): List[Tree[M, A]] =
    g(x).map(y => unfoldTree(f, g, y)(F))
}

