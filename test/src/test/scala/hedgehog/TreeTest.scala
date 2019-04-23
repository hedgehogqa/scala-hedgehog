package hedgehog

import hedgehog.runner._

object TreeTest extends Properties {

  def tests: List[Test] =
    List(
      example("applicative", testApplicative)
    , example("monad", testMonad)
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
}
