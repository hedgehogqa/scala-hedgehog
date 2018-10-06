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
    config: PropertyConfig
  , run: GenT[M, (List[Log], Option[A])]
  ) {

  def map[B](f: A => B)(implicit F: Functor[M]): PropertyT[M, B] =
    copy(run = run.map(x => x.copy(_2 = x._2.map(f))))

  def flatMap[B](f: A => PropertyT[M, B])(implicit F: Monad[M]): PropertyT[M, B] =
    copy(run = run.flatMap(x =>
      x._2.fold(
       GenT.GenApplicative(F).point((x._1, Option.empty[B]))
      )(
        a => f(a).run.map(y => (x._1 ++ y._1, y._2))
      )
    ))

  def check(seed: Seed)(implicit F: Monad[M]): M[Report] =
    propertyT.report(Size(0), seed, this)

  def checkRandom(p: PropertyT[M, Unit])(implicit F: Monad[M]): M[Report] =
    // FIX: predef MonadIO
    check(Seed.fromTime())

 def withTests(n: SuccessCount):  PropertyT[M, A] =
   copy(config = config.copy(testLimit = n))
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

  def isFailure[A, B](n: Node[M, Option[(B, Option[A])]]): Boolean =
    n.value.map(_._2) == Some(None)

  def takeSmallest[A](n: ShrinkCount, slimit: ShrinkLimit, t: Node[M, Option[(List[Log], Option[A])]])(implicit F: Monad[M]): M[Status] =
    t.value match {
      case None =>
        F.point(Status.gaveUp)

      case Some((w, None)) =>
        if (n.value >= slimit.value) {
          F.point(Status.failed(n, w))
        } else {
          F.map(findMapM(t.children)(m =>
            F.bind(m.run)(node =>
              if(isFailure(node))
                F.map(takeSmallest(n.inc, slimit, node))(some)
              else
                F.point(Option.empty[Status])
            )
          ))(_.getOrElse(Status.failed(n, w)))
        }

      case Some((_, Some(_))) =>
        F.point(Status.ok)
    }

  def report[A](size0: Size, seed0: Seed, p: PropertyT[M, A])(implicit F: Monad[M]): M[Report] = {
    def loop(successes: SuccessCount, discards: DiscardCount, size: Size, seed: Seed): M[Report] =
      if (size.value > 99)
        loop(successes, discards, Size(0), seed)
      else if (successes.value >= p.config.testLimit.value)
        F.point(Report(successes, discards, OK))
      else if (discards.value >= p.config.discardLimit.value)
        F.point(Report(successes, discards, GaveUp))
      else
        F.bind(p.run.run(size, seed).run)(x =>
          x.value._2 match {
            case None =>
              loop(successes, discards.inc, size.inc, x.value._1)

            case Some((_, None)) =>
              F.map(takeSmallest(ShrinkCount(0), p.config.shrinkLimit, x.map(_._2)))(y => Report(successes, discards, y))

            case Some((_, Some(_))) =>
              // Stop looping if the seed was never used - the property never generated anything
              if (seed == x.value._1)
                F.point(Report(successes.inc, discards, OK))
              else
                loop(successes.inc, discards, size.inc, x.value._1)
          }
        )
    loop(SuccessCount(0), DiscardCount(0), size0, seed0)
  }

  def recheck(size: Size, seed: Seed)(p: PropertyT[M, Unit])(implicit F: Monad[M]): M[Report] =
    report(Size(0), seed, p.withTests(SuccessCount(1)))
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
