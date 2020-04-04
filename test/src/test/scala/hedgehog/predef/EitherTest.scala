package hedgehog.predef

import hedgehog.StackSafeTest._
import hedgehog.core.{GenT, PropertyT, Tree}
import hedgehog.predef.Monad._
import hedgehog.runner._

object EitherTest extends Properties {

  private val ToProperty = λ[PF1[Either[Nothing, *], PropertyT]](eitherProperty)

  override def tests: List[Test] =
    List(
      property("tailRecM is stack safe", propTailRecMIsStackSafe(ToProperty))
    )

  private def eitherProperty[A](either: Either[Nothing, A]): PropertyT[A] =
    GenT {
      case (_, seed) => implicitly[Applicative[Tree]].point((seed, either.right.toOption))
    }.forAll
}
