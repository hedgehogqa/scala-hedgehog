package hedgehog.examples.state

import java.util.function.UnaryOperator
import java.util.concurrent.atomic.AtomicReference

import hedgehog._
import hedgehog.runner._
import hedgehog.state._

import scala.concurrent.ExecutionContext.Implicits._

/**
 * An effectful turnstile
 *
 *             Coin
 *    Push    +-------------+
 *  +-----+   |             |     +-----+
 *  |     |   |             v     v     |
 *  |   +-+---+--+       +--+-----+-+   |
 *  |   |        |       |          |   |
 *  |   | Locked |       | Unlocked |   |
 *  |   |        |       |          |   |
 *  |   +-+---+--+       +--+-----+-+   |
 *  |     ^   ^             |     |     |
 *  +-----+   |             |     +-----+
 *            +-------------+      Coin
 *                      Push
 *
 * - States: Locked/Unlocked (represented by the boxes)
 * - Transitions: Coin/Push (represented by the arrows)
 *
 * https://teh.id.au/posts/2017/07/15/state-machine-testing/index.html
 */
object TurnstileTest extends Properties {

  override def tests: List[Prop] =
    List(
      property("sequential", testSequential)

    /**
     * NOTE: This currently fails, which is expected.
     *
     * {{{
     * --- Prefix ---
     * Var(Name(0)) = Coin
     * --- Branch 1 ---
     * Var(Name(1)) = PushUnlocked
     * --- Branch 2 ---
     * Var(Name(2)) = PushUnlocked
     * }}}
     *
     * {{{
     * --- Prefix ---
     * --- Branch 1 ---
     * Var(Name(1)) = Coin
     * --- Branch 2 ---
     * Var(Name(2)) = PushLocked
     * }}}
     */
    , property("parallel", testParallel)
    )

  def commands(turnstile: Turnstile): List[CommandIO[State]] =
    List(
      commandCoin(turnstile)
    , commandPushLocked(turnstile)
    , commandPushUnlocked(turnstile)
    )

  def testSequential: Property = {
    val turnstile = Turnstile.create
    sequential(
        Range.linear(1, 100)
      , State.default
      , commands(turnstile)
      , () => turnstile.reset()
      )
  }

  def testParallel: Property = {
    val turnstile = Turnstile.create
    parallel(
        Range.linear(1, 100)
      , Range.linear(1, 10)
      , State.default
      , commands(turnstile)
      , () => turnstile.reset()
      )
  }

  def commandCoin(turnstile: Turnstile): CommandIO[State] =
    new Command[State, Unit, Unit] {

      override def gen(s: State): Option[Gen[Input]] =
        Some(Gen.constant(()))

      override def execute(env: Environment, s: Input): Either[String, Output] = {
        turnstile.insertCoin()
        Right(())
      }

      override def update(s: State, i: Input, o: Var[Output]): State =
        s.copy(state = TurnstileState.Unlocked)

      override def ensure(env: Environment, s0: State, s: State, i: Input, o: Output): Result =
        s.state ==== TurnstileState.Unlocked

      override def renderInput(_i: Unit): String =
        "Coin"
    }

  def commandPushLocked(turnstile: Turnstile): CommandIO[State] =
    new Command[State, Unit, Boolean] {

      override def gen(s: State): Option[Gen[Input]] =
        s.state match {
          case TurnstileState.Locked =>
            Some(Gen.constant(()))
          case TurnstileState.Unlocked =>
            None
        }

      override def require(s: State, i: Input): Boolean =
        s.state == TurnstileState.Locked

      override def execute(env: Environment, s: Input): Either[String, Boolean] =
        Right(turnstile.push())

      override def update(s: State, i: Input, o: Var[Output]): State =
        s.copy(state = TurnstileState.Locked)

      override def ensure(env: Environment, s0: State, s: State, i: Input, o: Output): Result =
        Result.all(List(
          s0.state ==== TurnstileState.Locked
        , Result.assert(!o).log("push")
        , s.state ==== TurnstileState.Locked
        ))

      override def renderInput(_i: Unit): String =
        "PushLocked"
    }

  def commandPushUnlocked(turnstile: Turnstile): CommandIO[State] =
    new Command[State, Unit, Boolean] {

      override def gen(s: State): Option[Gen[Input]] =
        s.state match {
          case TurnstileState.Locked =>
            None
          case TurnstileState.Unlocked =>
            Some(Gen.constant(()))
        }

      override def require(s: State, i: Input): Boolean =
        s.state == TurnstileState.Unlocked

      override def execute(env: Environment, s: Input): Either[String, Boolean] =
        Right(turnstile.push())

      override def update(s: State, i: Input, o: Var[Output]): State =
        s.copy(state = TurnstileState.Locked)

      override def ensure(env: Environment, s0: State, s: State, i: Input, o: Output): Result =
        Result.all(List(
          s0.state ==== TurnstileState.Unlocked
        , Result.assert(o).log("push")
        , s.state ==== TurnstileState.Locked
        ))

      override def renderInput(_i: Unit): String =
        "PushUnlocked"
    }

  case class State(state: TurnstileState)

  object State {

    def default: State =
      State(TurnstileState.initialState)
  }
}

sealed trait TurnstileState

object TurnstileState {

  case object Locked extends TurnstileState
  case object Unlocked extends TurnstileState

  def initialState: TurnstileState =
    Locked
}

case class Turnstile(
    state: AtomicReference[TurnstileState]
  ) {

  def reset(): Unit = {
    state.set(TurnstileState.initialState)
  }

  def insertCoin(): Unit = {
    state.getAndUpdate(new UnaryOperator[TurnstileState] {
      def apply(_x: TurnstileState): TurnstileState =
        TurnstileState.Unlocked
    })
    ()
  }

  def push(): Boolean =
    state.getAndUpdate(new UnaryOperator[TurnstileState] {
      def apply(_x: TurnstileState): TurnstileState =
        TurnstileState.Locked
    }) match {
      case TurnstileState.Locked =>
        false
      case TurnstileState.Unlocked =>
        true
    }
}

object Turnstile {

  def create: Turnstile =
    Turnstile(new AtomicReference(TurnstileState.initialState))
}
