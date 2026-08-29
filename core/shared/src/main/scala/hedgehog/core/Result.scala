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

  def and(other: => Result): Result =
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

  def or(other: => Result): Result =
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

  def log(info: => Log): Result =
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

  def failure(implicit pos: SourcePos): Result =
    Failure(List(SourceLocation(pos)))

  /**
    * Unlike the other failure constructors this does not record a source location,
    * because the exception already carries its own stack trace.
    */
  def error(e: Exception): Result =
    Failure(List(Error(e)))

  def assert(b: Boolean)(implicit pos: SourcePos): Result =
    if (b) Success else Failure(List(SourceLocation(pos)))

  def all(l: List[Result]): Result =
    l.foldLeft(Result.success)(_.and(_))

  /**
    * The seed is a bare `Failure(Nil)` rather than `Result.failure` so that an empty
    * or an all-successful `any` does not pick up a source location of its own.
    */
  def any(l: List[Result]): Result =
    l.foldLeft(Failure(Nil): Result)(_.or(_))

  /**
    * Compare two arguments with the comparison function and return Result.success
    * if the function return true. Otherwise, it returns Result.failure with
    * the Log containing the argument values.
    *
    * @example
    * {{{
    *   val a1 = "abc"
    *   val a2 = "abc"
    *   Result.diff(a1, a2)(_ == _)
    *   // Result.success
    *
    *   Result.diff(123, 456)(_ != _).log("It must be different.")
    *   // Result.success
    *
    *   val x = 'q'
    *   val y = 80
    *   Result.diff(x, y)((x, y) => y < 87 && x <= 'r')
    *   // Result.success
    * }}}
    * @example
    * {{{
    *   val a1 = "abc"
    *   val a2 = "xyz"
    *   Result.diff(a1, a2)(_ == _)
    *   // Result.failure
    *   > file:///home/username/proj/src/test/scala/MySpec.scala:12
    *   > === Failed ===
    *   > --- lhs ---
    *   > abc
    *   > --- rhs ---
    *   > xyz
    *
    *   Result.diff(123, 123)(_ != _).log("It must be different.")
    *   // Result.failure
    *   > file:///home/username/proj/src/test/scala/MySpec.scala:18
    *   > === Failed ===
    *   > --- lhs ---
    *   > 123
    *   > --- rhs ---
    *   > 123
    *   > It must be different.
    *
    *   val x = 'z'
    *   val y = 100
    *   Result.diff(x, y)((x, y) => y < 87 && x <= 'r')
    *   // Result.failure
    *   > file:///home/username/proj/src/test/scala/MySpec.scala:27
    *   > === Failed ===
    *   > --- lhs ---
    *   > z
    *   > --- rhs ---
    *   > 100
    * }}}
    *
    * @see https://github.com/hedgehogqa/haskell-hedgehog/blob/921e4af72a181f01d90816fd7055b823bf885b3b/hedgehog/src/Hedgehog/Internal/Property.hs#L707
    */
  def diff[A, B](a: A, b: B)(f: (A, B) => Boolean)(implicit pos: SourcePos): Result =
    diffNamed("=== Failed ===", a, b)(f)

  def diffNamed[A, B](logName: String, a: A, b: B)(f: (A, B) => Boolean)(implicit pos: SourcePos): Result =
    if (f(a, b))
      Success
    else
      Failure(List(
        SourceLocation(pos)
      , Info(logName)
      , Info("--- lhs ---")
      , Info(String.valueOf(a))
      , Info("--- rhs ---")
      , Info(String.valueOf(b))
      ))

}
