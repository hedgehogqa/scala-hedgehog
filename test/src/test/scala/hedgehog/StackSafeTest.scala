package hedgehog

import hedgehog.core.PropertyT
import hedgehog.predef.Monad

import scala.annotation.tailrec
import scala.language.higherKinds

object StackSafeTest {

  def propTailRecMIsStackSafe[F[_] : Monad](f: F[List[Int]] => PropertyT[List[Int]]): Property = {
    for {
      list <- genLazyStackSizes.forAll
      sizes <- f(genStackSizes(list))
    } yield sameSizes(sizes)
  }

  private def genLazyStackSizes: Gen[List[() => Int]] =
    Gen.list(Gen.constant(stackSize), Range.linear(0, 10000))

  private def stackSize =
    () => Thread.currentThread.getStackTrace.length

  private def genStackSizes[F[_]](lazyStackSizes: List[() => Int])(implicit M: Monad[F]): F[List[Int]] = {
    type A = (List[() => Int], List[Int])
    val zero: A = (lazyStackSizes, List.empty[Int])
    M.tailRecM(zero) {
      case (Nil, result) => M.point(Right(result))
      case (head :: tail, result) => M.point(Left((tail, head() :: result)))
    }
  }

  private def sameSizes(sizes: List[Int]): Result = {
    @tailrec
    def loop(baseSize: Option[Int], sizes: List[Int]): Result = sizes match {
      case Nil => Result.success
      case head :: tail => baseSize match {
        case None => loop(Some(head), tail)
        case Some(size) => size ==== head match {
          case Result.Success => loop(baseSize, tail)
          case failure: Result.Failure => failure
        }
      }
    }

    loop(None, sizes)
  }
}
