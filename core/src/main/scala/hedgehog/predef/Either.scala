package hedgehog.predef

// NOTE: This is purely to avoid deprecation warnings in scala 2.13 :(
class EitherOps[L, R](e: Either[L, R]) {

  def leftMap[B](f: L => B): Either[B, R] =
    e match {
      case Left(l) =>
        Left(f(l))
      case Right(r) =>
        Right(r)
    }

  def rightMap[B](f: R => B): Either[L, B] =
    rightFlatMap(r => Right(f(r)))

  def rightFlatMap[B](f: R => Either[L, B]): Either[L, B] =
    e match {
      case Left(l) =>
        Left(l)
      case Right(r) =>
        f(r)
    }
}
