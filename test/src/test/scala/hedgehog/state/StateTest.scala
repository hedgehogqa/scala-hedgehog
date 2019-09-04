package hedgehog.state

import java.util.function.UnaryOperator
import java.util.concurrent.atomic.AtomicReference

import hedgehog._
import hedgehog.core.{Result =>_, _}
import hedgehog.runner._

import scala.collection.concurrent
import scala.concurrent.ExecutionContext.Implicits._

object StateTest extends Properties {

  override def tests: List[Prop] =
    List(
      property("registry (sequential)", testRegistrySequential)
    , propertyFails("registry (parallel)", testRegistryParallel)
    , propertyFails("accumulator (sequential)", testAccumulatorSequential)
    , property("get-and-set (parallel) good", testGetAndSetParallelGood)
    , propertyFails("get-and-set (parallel) bad", testGetAndSetParallelBad)
    , property("vars (parallel)", testVarsParallel)
    )

  def testRegistrySequential: Property = {
    import Registry._
    val world = World.create
    sequential(
        Range.linear(1, 100)
      , State.default
      , commands(world)
      , () => world.reset()
      )
  }

  def testRegistryParallel: Property = {
    import Registry._
    val world = World.create
    parallel(
        Range.linear(1, 100)
      , Range.linear(1, 10)
      , State.default
      , commands(world)
      , () => world.reset()
      )
  }

  def testAccumulatorSequential: Property = {
    import Accumulator._
    val ref = new AtomicReference(-1)
    sequential(
        Range.linear(1, 100)
      , -1
      , commands(ref)
      , () => ref.set(-1)
      )
  }

  def testGetAndSetParallelGood: Property = {
    import GetAndSet._
    val ref = new AtomicReference("")
    parallel(
        Range.linear(1, 100)
      , Range.linear(1, 10)
      , ""
      , commands(ref, goodBoy = true)
      , () => ref.set("")
      )
  }

  def testGetAndSetParallelBad: Property = {
    import GetAndSet._
    val ref = new AtomicReference("")
    parallel(
        Range.linear(1, 100)
      , Range.linear(1, 10)
      , ""
      , commands(ref, goodBoy = false)
      , () => ref.set("")
      )
  }

  def testVarsParallel: Property = {
    import Vars._
    parallel(
        Range.linear(1, 100)
      , Range.linear(1, 10)
      , State(Nil)
      , commands
      , () => ()
      )
  }

  def propertyFails(n: String, p: Property): Test =
    example(n, {
      val r = Property.check(PropertyConfig.default.copy(testLimit = 1000), p, Seed.fromTime())
      r.status match {
        case Failed(_, _) =>
          Result.success
        case _ =>
          Result.failure.log(r.toString)
      }
    })
}

object Registry {

  case class Pid(value: Int)
  case class Name(value: String)

  case class Spawn()
  case class Register(name: Name, value: Var[Pid])
  case class Unregister(name: Name)

  case class State(
      pids: Set[Var[Pid]]
    , regs: Map[Name, Var[Pid]]
    )

  object State {

    def default: State =
      State(Set(), Map())
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

  def commands(world: World): List[CommandIO[State]] =
    List(
        spawnCommand(world.pid)
      , registerCommand(world.procTable)
      , unregisterCommand(world.procTable)
      )

  def spawnCommand(pid: AtomicReference[Pid]): CommandIO[State] =
    new Command[State, Spawn, Pid] {

      override def gen(s: State): Option[Gen[Spawn]] =
        Some(Gen.constant(Spawn()))

      override def execute(env: Environment, s: Spawn): Either[String, Pid] =
        Right(pid.getAndUpdate(new UnaryOperator[Pid] {
          def apply(x: Pid): Pid =
            Pid(x.value + 1)
        }))

      def update(s: State, i: Spawn, o: Var[Pid]): State =
        s.copy(pids = s.pids + o)

      def ensure(env: Environment, s0: State, s: State, i: Spawn, o: Pid): Result =
        Result.success
    }

  def registerCommand(procTable: concurrent.Map[Name, Pid]): CommandIO[State] =
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

      override def require(state: State, input: Register): Boolean =
        !state.regs.contains(input.name) && !state.regs.exists(x => x._2 == input.value)

      def update(s: State, i: Register, o: Var[Unit]): State =
        s.copy(regs = s.regs + (i.name -> i.value))

      def ensure(env: Environment, s0: State, s: State, i: Register, o: Unit): Result =
        Result.success
    }

  def unregisterCommand(procTable: concurrent.Map[Name, Pid]): CommandIO[State] =
    new Command[State, Unregister, Unit] {

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

      override def require(state: State, input: Unregister): Boolean =
        state.regs.contains(input.name)

      def update(s: State, i: Unregister, o: Var[Unit]): State =
        s.copy(regs = s.regs - i.name)

      def ensure(env: Environment, s0: State, s: State, i: Unregister, o: Unit): Result =
        Result.success
    }
}

object Accumulator {

  case class Register(name: Int)

  def commands(ref: AtomicReference[Int]): List[CommandIO[Int]] =
    List(
        command(0, 2, ref)
      , command(1, 2, ref)
      , command(2, 2, ref)
      )

  def command(name: Int, expected: Int, ref: AtomicReference[Int]): CommandIO[Int] =
    new Command[Int, Register, Boolean] {

      override def gen(s: Int): Option[Gen[Register]] =
        Some(Gen.constant(Register(name)))

      override def execute(env: Environment, s: Register): Either[String, Boolean] =
        Right(
          ref.getAndUpdate(new UnaryOperator[Int] {
            def apply(x: Int): Int =
              if (x + 1 == s.name) s.name else x
          }) == s.name
        )

      def update(s: Int, i: Register, o: Var[Boolean]): Int =
        if (s + 1 == i.name) i.name else s

      def ensure(env: Environment, s0: Int, s: Int, i: Register, o: Boolean): Result =
        Result.assert(s != expected)
          .log(s"${s.toString} == ${expected.toString}")
    }
}

object GetAndSet {

  def commands(ref: AtomicReference[String], goodBoy: Boolean): List[CommandIO[String]] =
    List(
        command(ref, goodBoy = goodBoy)
      )

  def command(ref: AtomicReference[String], goodBoy: Boolean): CommandIO[String] =
    new Command[String, String, String] {

      override def gen(s: String): Option[Gen[String]] =
        Some(Gen.element1("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"))

      override def execute(env: Environment, s: String): Either[String, String] = {
        if (goodBoy) {
          Right(ref.getAndUpdate(new UnaryOperator[String] {
            def apply(s0: String): String =
              if (s0 < s) s else s0
          }))
        } else {
          val s0 = ref.get
          if (s0 < s) {
            // A little helping hand for Travis
            Thread.sleep(1)
            ref.set(s)
          }
          Right(s0)
        }
      }

      def update(s: String, i: String, o: Var[String]): String =
        if (s < i) i else s

      def ensure(env: Environment, s0: String, s: String, i: String, o: String): Result =
        Result.assert(s0 == o)
          .log(s"$s0 $s $i $o")
    }
  }

object Vars {

  case class State(vars: List[Var[Unit]])

  def commands: List[CommandIO[State]] =
    List(
      command
    )

  def command: CommandIO[State] =
    new Command[State, Unit, Unit] {

      override def gen(s: State): Option[Gen[Input]] =
        Some(Gen.constant(()))

      override def execute(env: Environment, s: Input): Either[String, Output] =
        Right(s)

      override def update(s: State, i: Input, o: Var[Output]): State =
        s.copy(vars = s.vars ++ List(o))

      override def ensure(env: Environment, s0: State, s: State, i: Input, o: Output): Result = {
        // Make sure we don't throw an exception accessing all the vars
        s.vars.foreach(_.get(env))
        Result.success
      }
    }
}
