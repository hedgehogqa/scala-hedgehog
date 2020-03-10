package hedgehog.state

import hedgehog.core._

object Runner {

  def renderParallel[S](p: Parallel[S]): String =
    List(
        List("--- Prefix ---")
      , p.prefix.map(renderAction)
      , List("--- Branch 1 ---")
      , p.branch1.map(renderAction)
      , List("--- Branch 2 ---")
      , p.branch2.map(renderAction)
      ).flatten.mkString("\n")

  def renderActions[S](as: List[Action[S]]): String =
    as.map(renderAction).mkString("\n")

  def renderAction[S](a: Action[S]): String = {
    val prefix0 = s"${a.output.toString} = "
    val prefix = prefix0.map(_ => ' ')
    a.command.renderInput(a.input).split('\n').toList match {
      case Nil =>
        prefix0 + "?"
      case x :: xs =>
        (prefix0 + x :: xs.map(prefix + _)).mkString("\n")
    }
  }

  def executionErrorToResult(e: ExecutionError): Result =
    Result.failure.log(renderExecutionError(e))

  def renderExecutionError(ee: ExecutionError): String =
    ee match {
      case ExecutionError.Environment(e) =>
        e match {
          case EnvironmentError.ValueNotFound(n) =>
            s"Environment value not found for Var(${n.value.toString})"
          case EnvironmentError.TypeError(n, _, cnf) =>
            s"Invalid type for Var(${n.value.toString}): ${cnf.getMessage}"
        }
      case ExecutionError.Execute(e) =>
        "Error running execute: " + e
      case ExecutionError.Unknown(e) =>
        "Error thrown running execute: " + e.getMessage
    }
}
