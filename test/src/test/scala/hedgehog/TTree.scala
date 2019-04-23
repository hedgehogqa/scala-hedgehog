package hedgehog

import hedgehog.core._
import hedgehog.predef._

/**
 * A simplified strict tree for testing trees with.
 *
 * FIXME We should consider introducing a parameter for Tree that lets us do this easily.
 */
case class TTree[A](value: A, children: List[TTree[A]]) {

  def toTree: Tree[A] =
    Tree(value, Identity(LazyList.fromList(children.map(_.toTree))))
}

object TTree {

  def fromTree[A](depth: Int, width: Int, t: Tree[A]): TTree[A] =
    if (depth <= 0)
      TTree(t.value, Nil)
    else
      TTree(t.value, t.children.value.toList(width).map(TTree.fromTree(depth - 1, width, _)))
}
