package hedgehog

import hedgehog.core.PropertyT
import hedgehog.predef.Monad

import scala.annotation.tailrec
import scala.language.higherKinds

object StackSafeTest {

  type StackTrace = Array[StackTraceElement]

  trait PF1[-F[_], +G[_]] {
    def apply[A](fa: F[A]): G[A]
  }

  // TODO: f ought to be a FunctionK.
  def propTailRecMIsStackSafe[F[_] : Monad](f: PF1[F, PropertyT]): Property = {
    for {
      list <- genLazyStackSizes.forAll
      sizes <- f(genStackSizes(list))
    } yield sameSizes(sizes)
  }

  private def genLazyStackSizes: Gen[List[() => StackTrace]] =
    Gen.list(Gen.constant(stackSize), Range.linear(1000, 1000))

  private def stackSize =
    () => Thread.currentThread.getStackTrace

  private def genStackSizes[F[_]](lazyStackSizes: List[() => StackTrace])(implicit M: Monad[F]): F[List[StackTrace]] = {
    type A = (List[() => StackTrace], List[StackTrace])
    val zero: A = (lazyStackSizes, List.empty[StackTrace])
    M.tailRecM(zero) {
      case (Nil, result) => M.point(Right(result.reverse))
      case (head :: tail, result) => M.point(Left((tail, head() :: result)))
    }
  }

  private def sameSizes(stackTraces: List[StackTrace]): Result = {
    @tailrec
    def loop(baseStackTrace: Option[StackTrace], stackTraces: List[StackTrace]): Result = stackTraces match {
      case Nil => Result.success
      case head :: tail => baseStackTrace match {
        case None => loop(Some(head), tail)
        case Some(stackTrace) => stackTrace ==== head match {
          case Result.Success => loop(baseStackTrace, tail)
          case failure: Result.Failure => failure
        }
      }
    }

    loop(None, stackTraces)
  }
}
