package hedgehog.predef

import hedgehog.StackSafeTest.PF1
import hedgehog._
import hedgehog.core.PropertyT
import hedgehog.runner._

object StateTest extends Properties {

  private val ToProperty = λ[PF1[StateT[Id, Any, *], PropertyT]](stateProperty)

  override def tests: List[Test] =
    List(
      property("tailRecM is stack safe", StackSafeTest.propTailRecMIsStackSafe(ToProperty)(StateT.StateTMonad(IdMonad)))
    )

  private def stateProperty[A](state: StateT[Id, Any, A]): PropertyT[A] =
    Gen.constant(state.run(0)._2).forAll
}
