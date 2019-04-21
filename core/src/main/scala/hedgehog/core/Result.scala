package hedgehog.core

sealed trait Result {

  import Result._

  def success: Boolean =
    this match {
      case Success =>
        true
      case Failure(_) =>
        false
    }

  def logs: List[Log] =
    this match {
      case Success =>
        Nil
      case Failure(l) =>
        l
    }

  def and(other: Result): Result =
    this match {
      case Success =>
        other
      case Failure(logs) =>
        other match {
          case Success =>
            Failure(logs)
          case Failure(logs2) =>
            Failure(logs ++ logs2)
        }
    }

  def or(other: Result): Result =
    this match {
      case Success =>
        Success
      case Failure(logs) =>
        other match {
          case Success =>
            Success
          case Failure(logs2) =>
            Failure(logs ++ logs2)
        }
    }

  def log(info: Log): Result =
    this match {
      case Success =>
        Success
      case Failure(l) =>
        Failure(l ++ List(info))
    }
}

object Result {

  case object Success extends Result
  case class Failure(log: List[Log]) extends Result

  def success: Result =
    Success

  def failure: Result =
    Failure(Nil)

  def error(e: Exception): Result =
    failure.log(Error(e))

  def assert(b: Boolean): Result =
    if (b) success else failure

  def all(l: List[Result]): Result =
    l.foldLeft(Result.success)(_.and(_))

  def any(l: List[Result]): Result =
    l.foldLeft(Result.failure)(_.or(_))
}
