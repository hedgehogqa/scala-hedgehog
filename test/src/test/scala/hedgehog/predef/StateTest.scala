package hedgehog.predef

import hedgehog._
import hedgehog.core.PropertyT
import hedgehog.runner._

object StateTest extends Properties {

  override def tests: List[Test] =
    List(
      property("tailRecM is stack safe", StackSafeTest.propTailRecMIsStackSafe[StateT[Id, Any, ?]](stateProperty)(StateT.StateTMonad(IdMonad)))
    )

  private def stateProperty(state: StateT[Id, Any, List[Int]]): PropertyT[List[Int]] =
    Gen.constant(state.run(0)._2).forAll
}
