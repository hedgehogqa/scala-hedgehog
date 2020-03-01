package hedgehog.predef

import hedgehog._
import hedgehog.core.{GenT, PropertyT, Tree}
import hedgehog.predef.Monad._
import hedgehog.runner._

object EitherTest extends Properties {

  override def tests: List[Test] =
    List(
      property("tailRecM is stack safe", StackSafeTest.propTailRecMIsStackSafe[Either[Nothing, ?]](eitherProperty))
    )

  private def eitherProperty(either: Either[Nothing, List[Int]]): PropertyT[List[Int]] =
    GenT {
      case (_, seed) => implicitly[Applicative[Tree]].point((seed, either.right.toOption))
    }.forAll
}
