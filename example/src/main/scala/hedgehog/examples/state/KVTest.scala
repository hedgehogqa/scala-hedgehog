package hedgehog.examples.state

import hedgehog._
import hedgehog.runner._
import hedgehog.state._

import scala.concurrent.ExecutionContext.Implicits._

import java.io.{File, FileWriter}

trait KV {

  def put(k: String, v: String): Unit

  def get(k: String): Option[String]
}

/**
 * This example has a companion tutorial.
 *
 * https://github.com/hedgehogqa/scala-hedgehog/blob/master/doc/state-tutorial.md
 */
object KVTest extends Properties {

  override def tests: List[Prop] =
    List(
      property("test file KV (sequential) - broken", testSequential(KV.fileBadNamesSequential))
    , property("test file KV (sequential)", testSequential(KV.fileSequential))
    , property("test file KV (parallel) - broken", testParallel(KV.fileSequential))
    , property("test file KV (parallel)", testParallel(KV.fileParallel))
    )

  def commands(kv: KV): List[CommandIO[State]] =
    List(
      commandPut(kv)
    , commandGet(kv)
    )

  def testSequential(newKV: File => KV): Property = {
    val root = new File("tmpdir")
    val kv = newKV(root)
    sequential(
        Range.linear(1, 100)
      , State.default
      , commands(kv)
      , () => deleteRecursively(root)
      )
  }

  def testParallel(newKV: File => KV): Property = {
    val root = new File("tmpdir")
    val kv = newKV(root)
    parallel(
        Range.linear(0, 10)
      , Range.linear(1, 10)
      , State.default
      , commands(kv)
      , () => deleteRecursively(root)
      )
  }

  case class Put(key: String, value: String)
  case class Get(key: String)

  case class State(map: Map[String, String])

  object State {

    def default: State =
      State(Map())
  }

  def genKey : Gen[String] =
    for {
      c <- Gen.lower
      s <- Gen.string(Gen.choice1(Gen.lower, Gen.constant('/')), Range.linear(0, 10))
    } yield c +: s

  def commandPut(kv: KV): CommandIO[State] =
    new Command[State, Put, Unit] {

      override def gen(s: State): Option[Gen[Put]] =
        Some(for {
          k <- s.map.keys.toList match {
            case Nil =>
              genKey
            case h :: t =>
              // Choice between known and unknown keys
              Gen.frequency1(
                50 -> genKey
              , 50 -> Gen.element(h, t)
              )
          }
          // Always generate a unique value
          v <- Gen.string(Gen.upper, Range.linear(1, 10))
        } yield Put(k, v))

      override def execute(env: Environment, i: Put): Either[String, Unit] =
        Right(kv.put(i.key, i.value))

      override def update(s: State, i: Put, o: Var[Unit]): State =
        s.copy(map = s.map + (i.key -> i.value))

      override def ensure(env: Environment, s0: State, s: State, i: Put, o: Unit): Result =
        Result.success
    }

  def commandGet(kv: KV): CommandIO[State] =
    new Command[State, Get, Option[String]] {

      override def gen(s: State): Option[Gen[Get]] =
        Some((s.map.keys.toList match {
          case Nil =>
            genKey
          case h :: t =>
            // Mostly get values we know to exist, but also try values that don't
            Gen.frequency1(
              80 -> Gen.element(h, t)
            , 20 -> genKey
            )
        }).map(Get(_)))

      override def execute(env: Environment, i: Get): Either[String, Option[String]] =
        Right(kv.get(i.key))

      override def update(s: State, i: Get, o: Var[Option[String]]): State =
        s

      override def ensure(env: Environment, s0: State, s1: State, i: Get, o: Option[String]): Result =
        s1.map.get(i.key) ==== o
    }

  def deleteRecursively(file: File): Unit = {
    if (file.isDirectory) {
      file.listFiles.foreach(deleteRecursively)
    }
    file.delete
    ()
  }
}

object KV {

  def fileBadNamesSequential(root: File): KV =
    new KV {

      override def put(k: String, v: String): Unit = {
        val f = new File(root, k)
        f.getParentFile.mkdirs()
        val w = new FileWriter(f)
        w.write(v)
        w.close()
      }

      override def get(k: String): Option[String] = {
        val f = new File(root, k)
        if (!f.exists()) {
          None
        } else {
          Some(scala.io.Source.fromFile(f).mkString)
        }
      }
    }

  def fileSequential(root: File): KV =
    new KV {

      def getPath(k: String): File =
        new File(root, java.net.URLEncoder.encode(k, "UTF-8"))

      override def put(k: String, v: String): Unit = {
        val f = getPath(k)
        f.getParentFile.mkdirs()
        val w = new FileWriter(f)
        w.write(v)
        w.close()
      }

      override def get(k: String): Option[String] = {
        val f = getPath(k)
        if (!f.exists()) {
          None
        } else {
          Some(scala.io.Source.fromFile(f).mkString)
        }
      }
    }

  def fileParallel(root: File): KV =
    new KV {

      def getPath(k: String): File =
        new File(root, java.net.URLEncoder.encode(k, "UTF-8"))

      override def put(k: String, v: String): Unit = {
        val f = getPath(k)
        f.getParentFile.mkdirs()
        val g = new File(f.getParentFile, "." + f.getName + "_" + System.nanoTime.toString)
        val w = new FileWriter(g)
        w.write(v)
        w.close()
        g.renameTo(f)
        ()
      }

      override def get(k: String): Option[String] = {
        val f = getPath(k)
        if (!f.exists()) {
          None
        } else {
          Some(scala.io.Source.fromFile(f).mkString)
        }
      }
    }
}
