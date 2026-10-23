package hedgehog

import hedgehog.core._
import hedgehog.predef.LazyList
import hedgehog.runner._

import scala.annotation.tailrec

object TreeTest extends Properties {

  def tests: List[Test] =
    List(
      example("applicative", testApplicative)
    , example("monad", testMonad)
    , example("zipAll", testZipAll)
    , property("zipAll matches sequence", testZipAllMatchesSequence)
    , example("zipAll is stack safe", testZipAllStackSafe)
    )

  def testApplicative: Result = {
    TTree.fromTree(100, 100, forTupled(
      TTree(1, List(TTree(2, List()))).toTree
    , TTree(3, List(TTree(4, List()))).toTree
    )) ==== TTree((1, 3), List(
        TTree((2, 3), List(TTree((2, 4), List())))
      , TTree((1, 4), List(TTree((2, 4), List())))
      ))
  }

  def testMonad: Result = {
    TTree.fromTree(100, 100, for {
       a <- TTree(1, List(TTree(2, List()))).toTree
       b <- TTree(3, List(TTree(4, List()))).toTree
    } yield (a, b)
    ) ==== TTree((1, 3), List(
        TTree((2, 3), List(TTree((2, 4), List())))
      , TTree((1, 4), List()))
      )
   }

  def testZipAll: Result = {
    TTree.fromTree(100, 100, Tree.zipAll(Vector(
      TTree(1, List(TTree(2, List()))).toTree
    , TTree(3, List(TTree(4, List()))).toTree
    ))) ==== TTree(List(1, 3), List(
        TTree(List(2, 3), List(TTree(List(2, 4), List())))
      , TTree(List(1, 4), List(TTree(List(2, 4), List())))
      ))
  }

  def genTTree(depth: Int): Gen[TTree[Int]] =
    for {
      v <- Gen.int(Range.linear(0, 100))
      cs <-
        if (depth <= 0)
          Gen.constant(List.empty[TTree[Int]])
        else
          genTTree(depth - 1).list(Range.linear(0, 2))
    } yield TTree(v, cs)

  def testZipAllMatchesSequence: Property =
    for {
      ts <- genTTree(2).list(Range.linear(0, 3)).forAll
    } yield {
      val trees = ts.map(_.toTree)
      TTree.fromTree(100, 100, Tree.zipAll(trees.toVector)) ====
        TTree.fromTree(100, 100, predef.sequence(trees))
    }

  def testZipAllStackSafe: Result = {
    val n = 100000
    val t = Tree.zipAll(Vector.fill(n)(TTree(1, List(TTree(0, List()))).toTree))
    // Follow the first child down; only one child is forced per level.
    @tailrec
    def firstChild[A](t: Tree[A], depth: Int): Tree[A] =
      if (depth <= 0)
        t
      else
        t.children.value match {
          case LazyList.Nil() => t
          case LazyList.Cons(h, _) => firstChild(h(), depth - 1)
        }
    firstChild(t, 10).value.sum ==== n - 10
  }
}
