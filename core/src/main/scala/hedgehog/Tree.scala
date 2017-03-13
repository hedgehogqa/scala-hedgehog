package hedgehog

import scalaz.{ Node => _, Tree => _, _ }, Scalaz.{ unfoldForest => _, _ }

case class Node[M[_], A](value: A, children: List[Tree[M, A]])

case class Tree[M[_], A](run: M[Node[M, A]]) {

  def map[B](f: A => B)(implicit F: Functor[M]): Tree[M, B] =
    Tree(run.map(_.map(f)))

  def expand(f: A => List[A])(implicit F: Monad[M]): Tree[M, A] =
    Tree(run.flatMap(x =>
      F.point(Node(x.value, x.children.map(_.expand(f)) ++ Tree.unfoldForest(identity[A], f, x.value)(F)))
    ))

  def prune(implicit F: Monad[M]): Tree[M, A] =
    Tree(run.flatMap(x => F.point(Node(x.value, List()))))
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

  implicit def TreeApplicative[M[_]](implicit F: Applicative[M]): Applicative[Tree[M, ?]] =
    new Applicative[Tree[M, ?]] {
      def point[A](a: => A): Tree[M, A] =
        Tree(F.point(Node(a, List())))
      def ap[A, B](fa: => Tree[M, A])(f: => Tree[M, A => B]): Tree[M, B] =
        Tree(F.ap(fa.run)(f.run.map(n =>
          n2 => Node(n.value(n2.value), ^(n2.children, n.children)(ap(_)(_)))
        )))
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
        Tree(fa.run
          .flatMap(x => f(x.value).run
          .flatMap(y => Node(y.value, x.children.map(_.flatMap(f)) ++ y.children).point[M])
          ))
    }

  def unfoldTree[M[_], A, B](f: B => A, g: B => List[B], x: B)(implicit F: Applicative[M]): Tree[M, A] =
    Tree(F.point(Node(f(x), unfoldForest(f, g, x))))

  def unfoldForest[M[_], A, B](f: B => A, g: B => List[B], x: B)(implicit F: Applicative[M]): List[Tree[M, A]] =
    g(x).map(y => unfoldTree(f, g, y)(F))
}

