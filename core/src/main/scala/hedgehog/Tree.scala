package hedgehog

import hedgehog.predef._

case class Node[M[_], A](value: A, children: List[Tree[M, A]]) {

  def map[B](f: A => B)(implicit F: Functor[M]): Node[M, B] =
    Node.NodeFunctor.map(this)(f)
}

case class Tree[M[_], A](run: M[Node[M, A]]) {

  def map[B](f: A => B)(implicit F: Functor[M]): Tree[M, B] =
    Tree(F.map(run)(n => Node.NodeFunctor[M].map(n)(f)))

  def flatMap[B](f: A => Tree[M, B])(implicit F: Monad[M]): Tree[M, B] =
    Tree.TreeMonad[M].bind(this)(f)

  def expand(f: A => List[A])(implicit F: Monad[M]): Tree[M, A] =
    Tree(F.bind(run)(x =>
      F.point(Node(x.value, x.children.map(_.expand(f)) ++ Tree.unfoldForest(identity[A], f, x.value)(F)))
    ))

  def prune(implicit F: Monad[M]): Tree[M, A] =
    Tree(F.bind(run)(x => F.point(Node(x.value, List()))))
}

object Node {

  implicit def NodeFunctor[M[_]](implicit F: Functor[M]): Functor[Node[M, ?]] =
    new Functor[Node[M, ?]] {
      override def map[A, B](fa: Node[M, A])(f: A => B): Node[M, B] =
        Node(f(fa.value), fa.children.map(_.map(f)))
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
        Tree(F.point(Node(a, List())))
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
      override def bind[A, B](fa: Tree[M, A])(f: A => Tree[M, B]): Tree[M, B] =
        Tree(F.bind(fa.run)(x =>
          F.bind(f(x.value).run)(y =>
            F.point(Node(y.value, x.children.map(_.flatMap(f)) ++ y.children))
          )))
    }

  def unfoldTree[M[_], A, B](f: B => A, g: B => List[B], x: B)(implicit F: Applicative[M]): Tree[M, A] =
    Tree(F.point(Node(f(x), unfoldForest(f, g, x))))

  def unfoldForest[M[_], A, B](f: B => A, g: B => List[B], x: B)(implicit F: Applicative[M]): List[Tree[M, A]] =
    g(x).map(y => unfoldTree(f, g, y)(F))
}

