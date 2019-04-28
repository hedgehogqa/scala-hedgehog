/**
 * Translated from:
 *
 * https://github.com/hedgehogqa/haskell-hedgehog/blob/master/hedgehog-example/src/Test/Example/Registry.hs
 */
package hedgehog.examples.state

import java.util.function.UnaryOperator
import java.util.concurrent.atomic.AtomicReference

import hedgehog._
import hedgehog.runner._
import hedgehog.state._

import scala.collection.concurrent
import scala.concurrent.ExecutionContext.Implicits._

object RegistryTest extends Properties {

  override def tests: List[Prop] =
    List(
      property("sequential", testSequential)
    // NOTE: This currently fails, which is expected
    , property("parallel", testParallel)
    )

  def commands(world: World): List[CommandIO[State]] =
    List(
      Spawn.command(world.pid)
    , Register.command(world.procTable)
    , Unregister.command(world.procTable)
    )

  def testSequential: Property = {
    val world = World.create
    sequential(
        Range.linear(1, 100)
      , State.default
      , commands(world)
      , () => world.reset()
      )
  }

  def testParallel: Property = {
    val world = World.create
    parallel(
        Range.linear(1, 100)
      , Range.linear(1, 10)
      , State.default
      , commands(world)
      , () => world.reset()
      )
  }
}

case class World(
    procTable: concurrent.Map[Name, Pid]
  , pid: AtomicReference[Pid]
  ) {

  def reset(): Unit = {
    procTable.clear()
    pid.set(Pid(0))
  }
}

object World {

  def create: World =
    World(
        concurrent.TrieMap[Name, Pid]()
      , new AtomicReference[Pid](Pid(0))
      )
}

case class Pid(value: Int)
case class Name(value: String)

case class State(
    pids: Set[Var[Pid]]
  , regs: Map[Name, Var[Pid]]
  )

object State {

  def default: State =
    State(Set(), Map())
}

case class Spawn()

object Spawn {

  def command(pid: AtomicReference[Pid]): CommandIO[State] =
    new Command[State, Spawn, Pid] {

      override def vars(i: Spawn): List[Var[_]] =
        Nil

      override def gen(s: State): Option[Gen[Spawn]] =
        Some(Gen.constant(Spawn()))

      override def execute(env: Environment, s: Spawn): Either[String, Pid] =
        Right(pid.getAndUpdate(new UnaryOperator[Pid] {
          def apply(x: Pid): Pid =
            Pid(x.value + 1)
        }))

      def require(state: State, input: Spawn): Boolean =
        true

      def update(s: State, i: Spawn, o: Var[Pid]): State =
        s.copy(pids = s.pids + o)

      def ensure(env: Environment, s0: State, s: State, i: Spawn, o: Pid): Result =
        Result.success
    }
}

case class Register(name: Name, value: Var[Pid])

object Register {

  def command(procTable: concurrent.Map[Name, Pid]): CommandIO[State] =
    new Command[State, Register, Unit] {

      override def vars(i: Register): List[Var[_]] =
        i match {
          case Register(_, v) =>
            List(v)
        }

      override def gen(s: State): Option[Gen[Register]] =
        s.pids.toList match {
          case Nil =>
            None
          case xs =>
            Some(for {
              n <- Gen.element("a", List("b", "c", "d")).map(Name(_))
              v <- Gen.elementUnsafe(xs)
            } yield Register(n, v))
        }

      override def execute(env: Environment, s: Register): Either[String, Unit] =
        procTable.putIfAbsent(s.name, s.value.get(env)) match {
          case Some(_) =>
            Left("already registered")
          case None =>
            Right(())
        }

      def require(state: State, input: Register): Boolean =
        !state.regs.contains(input.name) && !state.regs.exists(x => x._2 == input.value)

      def update(s: State, i: Register, o: Var[Unit]): State =
        s.copy(regs = s.regs + (i.name -> i.value))

      def ensure(env: Environment, s0: State, s: State, i: Register, o: Unit): Result =
        Result.success
    }
}

case class Unregister(name: Name)

object Unregister {

  def command(procTable: concurrent.Map[Name, Pid]): CommandIO[State] =
    new Command[State, Unregister, Unit] {

      override def vars(i: Unregister): List[Var[_]] =
        Nil

      override def gen(s: State): Option[Gen[Unregister]] =
        s.regs.keys.toList match {
          case Nil =>
            None
          case xs =>
            Some(Gen.elementUnsafe(xs).map(Unregister(_)))
        }

      override def execute(env: Environment, s: Unregister): Either[String, Unit] =
        procTable.remove(s.name) match {
          case None =>
            Left("not registered")
          case Some(_) =>
            Right(())
        }

      def require(state: State, input: Unregister): Boolean =
        state.regs.contains(input.name)

      def update(s: State, i: Unregister, o: Var[Unit]): State =
        s.copy(regs = s.regs - i.name)

      def ensure(env: Environment, s0: State, s: State, i: Unregister, o: Unit): Result =
        Result.success
    }
}
