package hedgehog.core

import hedgehog._
import hedgehog.predef._

case class Name(value: String)

object Name {

  // Yes, ugly, but makes for a nicer API
  implicit def Name2String(s: String): Name =
    Name(s)
}

sealed trait Log
case class ForAll(name: Name, value: String) extends Log
case class Info(value: String) extends Log
case class Error(value: Exception) extends Log

object Log {

  implicit def String2Log(s: String): Log =
    Info(s)
}

case class PropertyConfig(
    testLimit: SuccessCount
  , discardLimit: DiscardCount
  , shrinkLimit: ShrinkLimit
  )

object PropertyConfig {

  def default: PropertyConfig =
    PropertyConfig(SuccessCount(100), DiscardCount(100), ShrinkLimit(1000))
}

case class PropertyT[M[_], A](
    run: GenT[M, (List[Log], Option[A])]
  ) {

  def map[B](f: A => B)(implicit F: Monad[M]): PropertyT[M, B] =
    copy(run = run.map(x =>
      try {
        x.copy(_2 = x._2.map(f))
      } catch {
        // Forgive me, I'm assuming this breaks the Functor laws
        // If there's a better and non-law breaking of handling this we should remove this
        case e: Exception =>
          (x._1 ++ List(Error(e)), None)
      }
    ))

  def flatMap[B](f: A => PropertyT[M, B])(implicit F: Monad[M]): PropertyT[M, B] =
    copy(run = run.flatMap(x =>
      x._2.fold(
       GenT.GenApplicative(F).point((x._1, Option.empty[B]))
      )(a =>
        try {
          f(a).run.map(y => (x._1 ++ y._1, y._2))
        } catch {
          // Forgive me, I'm assuming this breaks the Monad laws
          // If there's a better and non-law breaking of handling this we should remove this
          case e: Exception =>
            genT.constant((x._1 ++ List(Error(e)), None))
        }
      )
    ))
}

object PropertyT {

  implicit def PropertyMonad[M[_]](implicit F: Monad[M]): Monad[PropertyT[M, ?]] =
    new Monad[PropertyT[M, ?]] {
      override def map[A, B](fa: PropertyT[M, A])(f: A => B): PropertyT[M, B] =
        fa.map(f)
      override def point[A](a: => A): PropertyT[M, A] =
        propertyT.hoist((Nil, a))
      override def bind[A, B](fa: PropertyT[M, A])(f: A => PropertyT[M, B]): PropertyT[M, B] =
        fa.flatMap(f)
    }
}

trait PropertyTReporting[M[_]] {

  def takeSmallest[A](n: ShrinkCount, slimit: ShrinkLimit, run: A => Result, t: Tree[M, Option[(List[Log], Option[A])]])(implicit F: Monad[M]): M[Status] = {
    t.value match {
      case None =>
        F.point(Status.gaveUp)

      case Some((w, r3)) =>
        // NOTE: It's _really_ important we only run this once, these are our test side effects
        val r = r3.map(run)
        if (r.forall(!_.success)) {
          if (n.value >= slimit.value) {
            F.point(Status.failed(n, w ++ r.map(_.logs).getOrElse(Nil)))
          } else {
            F.map(F.bind(t.children)(x => {
              findMapM(x)(m =>
                F.map(takeSmallest(n.inc, slimit, run, m)) {
                  case y@Failed(_, _) =>
                    Some(y)
                  case GaveUp =>
                    // We only want errors
                    Option.empty[Status]
                  case OK =>
                    // We only want errors
                    Option.empty[Status]
                }
            )
            }))(_.getOrElse(Status.failed(n, w ++ r.map(_.logs).getOrElse(Nil))))
          }
        } else {
          F.point(Status.ok)
        }
    }
  }

  def report[A](config: PropertyConfig, size0: Option[Size], seed0: Seed, run: A => Result, p: PropertyT[M, A])(implicit F: Monad[M]): M[Report] = {
    // Increase the size proportionally to the number of tests to ensure better coverage of the desired range
    val sizeInc = Size(Math.max(1, Size.max / config.testLimit.value))
    // Start the size at whatever remainder we have to ensure we run with "max" at least once
    val sizeInit = Size(Size.max % Math.min(config.testLimit.value, Size.max)).incBy(sizeInc)
    def loop(successes: SuccessCount, discards: DiscardCount, size: Size, seed: Seed): M[Report] =
      if (size.value > Size.max)
        loop(successes, discards, sizeInit, seed)
      else if (successes.value >= config.testLimit.value)
        F.point(Report(successes, discards, OK))
      else if (discards.value >= config.discardLimit.value)
        F.point(Report(successes, discards, GaveUp))
      else {
        val x = p.run.run(size, seed)
        F.bind(takeSmallest(ShrinkCount(0), config.shrinkLimit, run, x.map(_._2))) {
          case y@Failed(_, _) =>
            F.point(Report(successes, discards, y))
          case GaveUp =>
            loop(successes, discards.inc, size.incBy(sizeInc), x.value._1)
          case OK =>
            loop(successes.inc, discards, size.incBy(sizeInc), x.value._1)
        }
      }
    loop(SuccessCount(0), DiscardCount(0), size0.getOrElse(sizeInit), seed0)
  }
}

/**********************************************************************/
// Reporting

/** The numbers of times a property was able to shrink after a failing test. */
case class ShrinkCount(value: Int) {

  def inc: ShrinkCount =
    ShrinkCount(value + 1)
}

/** The number of shrinks to try before giving up on shrinking. */
case class ShrinkLimit(value: Int)

/** The number of tests a property ran successfully. */
case class SuccessCount(value: Int) {

  def inc: SuccessCount =
    SuccessCount(value + 1)
}

object SuccessCount {

  implicit def Int2SuccessCount(i: Int): SuccessCount =
    SuccessCount(i)
}

/** The number of tests a property had to discard. */
case class DiscardCount(value: Int) {

  def inc: DiscardCount =
    DiscardCount(value + 1)
}

/**
 * The status of a property test run.
 *
 * In the case of a failure it provides the seed used for the test, the
 * number of shrinks, and the execution log.
 */
sealed trait Status
case class Failed(shrinks: ShrinkCount, log: List[Log]) extends Status
case object GaveUp extends Status
case object OK extends Status

object Status {

  def failed(count: ShrinkCount, log: List[Log]): Status =
    Failed(count, log)

  val gaveUp: Status =
    GaveUp

  val ok: Status =
    OK
}

case class Report(tests: SuccessCount, discards: DiscardCount, status: Status)
