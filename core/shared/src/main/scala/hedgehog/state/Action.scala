package hedgehog.state

import hedgehog.{Result => _, _}
import hedgehog.core._
import hedgehog.predef._

import scala.concurrent._

/**
 * An instantiation of a 'Command' which can be executed, and its effect evaluated.
 */
trait Action[S] {

  type Input
  type Output

  def input: Input

  def output: Var[Output]

  def command: Command[S, Input, Output]
}

object Action {

  type ActionCheck[S] = State[(S, Environment), Result]

  def action[S, I, O](command: Command[S, I, O], context: Context[S]): StateT[GenT, Context[S], Option[Action[S]]] =
    for {
      input <- command.gen(context.state) match {
        case None =>
          sys.error("Command.gen: internal error, tried to use generator with invalid state.")
        case Some(nb) =>
          stateT.lift[Context[S], I](nb)
      }
      x <-
        if (!command.require(context.state, input)) {
          stateT[GenT].point[Context[S], Option[Action[S]]](None)
        } else {
          val (context2, output) = Context.newVar[S, O](context)
          val context3 = context2.copy(state = command.update(context.state, input, output))
          for {
            _ <- stateT[GenT].put(context3)
            y <- stateT[GenT].point[Context[S], Option[Action[S]]](Some(fromCommand(command, input, output)))
          } yield y
        }
    } yield x

  def action[S](commands: List[CommandIO[S]]): StateT[GenT, Context[S], Action[S]] =
    MonadGen[StateT[GenT, Context[S], *]]
      .fromSome(for {
        context <- stateT[GenT].get[Context[S]]
        cmd <- stateT[GenT].lift(Gen.elementUnsafe(commands.filter(command =>
            command.command.gen(context.state).isDefined
          )))
        a <- action(cmd.command, context)
        } yield a)

  def genActions[S](range: Range[Int], commands: List[CommandIO[S]], ctx: Context[S]): GenT[(Context[S], List[Action[S]])] =
    MonadGen[StateT[GenT, Context[S], *]]
      .list(action(commands), range).eval(ctx)
      .map(xs => dropInvalid(xs).run(ctx).value)

  /** Drops invalid actions from the sequence. */
  def dropInvalid[S](actions: List[Action[S]]): State[Context[S], List[Action[S]]] = {

    def loop(step: Action[S]): State[Context[S], Option[Action[S]]] =
      for {
        c <- State.get[Context[S]]
        x <-
          // Checking that the variables are OK ensures we don't shrink to a state that is not valid
          if (step.command.require(c.state, step.input) && Var.variablesOK(step.command.vars(step.input), c.vars)) {
            for {
              _ <- State.put[Context[S]](Context(
                step.command.update(c.state, step.input, step.output)
              , Var.insert(c.vars, step.output)
              ))
            } yield some(step)
          } else {
            State.point[Context[S], Option[Action[S]]](Option.empty[Action[S]])
          }
      } yield x

    State.traverse(actions)(loop).map(_.flatMap(_.toList))
  }

  def execute[S](action: Action[S]): StateT[Either[ExecutionError, *], Environment, ActionCheck[S]] =
    stateT[Either[ExecutionError, *]](env0 =>
      // Apologies, we're going to assume that environment variables are uncommon enough we don't want to force
      // users to have to pass back the exceptions, instead we'll catch them here. Dodgy.
      (try {
        action.command.execute(env0, action.input)
          .leftMap(ExecutionError.execute)
      } catch {
        case e: EnvironmentError =>
          Left(ExecutionError.environment(e))
        case e: Exception =>
          Left(ExecutionError.unknown(e))
      }).rightMap(output => {
        // NOTE: We need to update the environment in different contexts, once for the original execution
        // and then later again for linearization
        val env = Environment(env0.value + (action.output.name -> output))
        (env, State.state { case (s0, env1) =>
          val env2 = Environment(env1.value + (action.output.name -> output))
          val s = action.command.update(s0, action.input, action.output)
          ((s, env2), action.command.ensure(env2, s0, s, action.input, output))
        })
        }
      )
    )

  def executeUpdateEnsure[S](action: Action[S]): State[(S, Environment), Result] =
    stateT.state { case (state0, env0) =>
      execute(action).eval(env0).rightMap { check =>
        check.run((state0, env0)).value
      }.fold(e => ((state0, env0), Runner.executionErrorToResult(e)), identity)
    }

  def executeSequential[S](initial: S, actions: List[Action[S]]): Result =
    stateT
      .traverse(actions)(executeUpdateEnsure)
      .eval((initial, Environment(Map())))
      .map(Result.all)
      .value

  /**
   * Given the initial model state and set of commands, generates prefix
   * actions to be run sequentially, followed by two branches to be run in
   * parallel.
   */
  def genParallel[S](prefixN: Range[Int], parallelN: Range[Int], initial: S, commands: List[CommandIO[S]]): Gen[Parallel[S]] =
    for {
      ctx0 <- genActions(prefixN, commands, Context.create(initial))
      ctx1 <- genActions(parallelN, commands, ctx0._1)
      ctx2 <- genActions(parallelN, commands, ctx1._1.copy(state = ctx0._1.state))
    } yield Parallel(ctx0._2, ctx1._2, ctx2._2)

  /**
   * Executes the prefix actions sequentially, then executes the two branches
   * in parallel, verifying that no exceptions are thrown and that there is at
   * least one sequential interleaving where all the post-conditions are met.
   *
   * To generate parallel actions to execute, see the 'Hedgehog.Gen.parallel'
   * combinator in the "Hedgehog.Gen" module.
   */
  def executeParallel[S](initial: S, parallel: Parallel[S])(implicit E: ExecutionContext): Future[Result] = {
    val (e, r) = stateT.traverse(parallel.prefix)(executeUpdateEnsure)
      .run((initial, Environment(Map())))
      .value
    Future(stateT[Either[ExecutionError, *]].traverse(parallel.branch1)(execute).eval(e._2))
      .zip(Future(stateT[Either[ExecutionError, *]].traverse(parallel.branch2)(execute).eval(e._2)))
      .map { case (xs, ys) =>
        Applicative.zip[Either[ExecutionError, *], List[ActionCheck[S]], List[ActionCheck[S]]](xs, ys)
          .fold[Result](
            e => Runner.executionErrorToResult(e)
          , x => Result.all(r).and(linearize(e._1, e._2, x._1, x._2))
          )
      }
  }

  def interleave[A](xs00: List[A], ys00: List[A]): List[List[A]] =
    (xs00, ys00) match {
      case (Nil, Nil) =>
        Nil
      case (xs, Nil) =>
        List(xs)
      case (Nil, ys) =>
        List(ys)
      case (xs0@x :: xs, ys0@y :: ys) =>
        List(
          interleave(xs, ys0).map(x :: _)
        , interleave(xs0, ys).map(y :: _)
        ).flatten
    }

  def linearize[S](initial: S, env: Environment, branch1: List[ActionCheck[S]], branch2: List[ActionCheck[S]]): Result =
    Result.any(
      interleave(branch1, branch2).zipWithIndex.map { case (bs, i) =>
        Result.all(
          State.traverse(bs)(identity).eval((initial, env)).value
        ).log(s"=== Counterexample ${i.toString} ===")
      }
    ).log("no valid interleaving")

  def fromCommand[S, I, O](c: Command[S, I, O], i: I, o: Var[O]): Action[S] =
    new Action[S] {
      override type Input = I
      override type Output = O
      override def input: Input = i
      override def output: Var[Output] = o
      override def command: Command[S, Input, Output] = c
    }
}

/**
 * A sequential prefix of actions to execute, with two branches to execute in parallel.
 */
case class Parallel[S](

    prefix: List[Action[S]]

  , branch1: List[Action[S]]

  , branch2: List[Action[S]]
  )

sealed trait ExecutionError extends Exception

object ExecutionError {

  case class Environment(e: EnvironmentError) extends ExecutionError

  case class Execute(e: String) extends ExecutionError

  case class Unknown(e: Exception) extends ExecutionError

  def environment(e: EnvironmentError): ExecutionError =
    Environment(e)

  def execute(e: String): ExecutionError =
    Execute(e)

  def unknown(e: Exception): ExecutionError =
    Unknown(e)
}
