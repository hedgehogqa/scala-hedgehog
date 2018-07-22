package hedgehog

import scalaz._, Scalaz._
import scalaz.effect._

case class Name(value: String)

object Name {

  // Yes, ugly, but makes for a nicer API
  implicit def Name2String(s: String): Name =
    Name(s)
}

sealed trait Log
case class ForAll(name: Name, value: String) extends Log
case class Info(value: String) extends Log

case class Property[M[_], A](run: GenT[M, (List[Log], Option[A])]) {

  def map[B](f: A => B)(implicit F: Functor[M]): Property[M, B] =
    Property(run.map(_.map(_.map(f))))

  def flatMap[B](f: A => Property[M, B])(implicit F: Monad[M]): Property[M, B] =
    Property(run.flatMap(x =>
      x._2.cata(
        a => f(a).run.map(y => (x._1 ++ y._1, y._2))
      , GenT.GenApplicative(F).point((x._1, None))
      )
    ))

  def check(seed: Seed)(implicit F: Monad[M]): M[Report] =
    Property.report(SuccessCount(100), Size(0), seed, this)

  def checkRandom(p: Property[M, Unit])(implicit F: MonadIO[M]): M[Report] =
    Seed.fromTime.liftIO.flatMap(s => check(s))
}

object Property {

  implicit def PropertyMonad[M[_]](implicit F: Monad[M]): Monad[Property[M, ?]] =
    new Monad[Property[M, ?]] {
      override def map[A, B](fa: Property[M, A])(f: A => B): Property[M, B] =
        fa.map(f)
      override def point[A](a: => A): Property[M, A] =
        hoist((Nil, a))
      override def bind[A, B](fa: Property[M, A])(f: A => Property[M, B]): Property[M, B] =
        fa.flatMap(f)
    }

  def fromGen[M[_] : Monad, A](gen: GenT[M, A]): Property[M, A] =
    Property(gen.map(x => (Nil, Some(x))))

  def hoist[M[_], A](a: (List[Log], A))(implicit F: Monad[M]): Property[M, A] =
    Property(GenT.GenApplicative(F).point(a.map(some)))

  def writeLog[M[_] : Monad](log: Log): Property[M, Unit] =
    hoist((List(log), ()))

  def info[M[_] : Monad](log: String): Property[M, Unit] =
    writeLog(Info(log))

  def discard[M[_] : Monad]: Property[M, Unit] =
    fromGen(genT.discard)

  def failure[M[_], A](implicit F: Monad[M]): Property[M, A] =
    Property(GenT.GenApplicative(F).point((Nil, None)))

  def success[M[_] : Monad]: Property[M, Unit] =
    hoist((Nil, ()))

  def assert[M[_] : Monad](b: Boolean): Property[M, Unit] =
    if (b) success else failure

  /**********************************************************************/
  // Reporting

  def isFailure[M[_], A, B](n: Node[M, Option[(B, Option[A])]]): Boolean =
    n.value.map(_._2) == Some(None)

  def takeSmallest[M[_] : Monad, A](n: ShrinkCount, t: Node[M, Option[(List[Log], Option[A])]]): M[Status] =
    t.value match {
      case None =>
        Status.gaveUp.point[M]

      case Some((w, None)) =>
        t.children.findMapM[M, Status](m =>
            m.run.flatMap(node =>
              if(isFailure(node))
                takeSmallest(n.inc, node).map(some)
              else
                none.point[M]
            )
          )
          .map(_.getOrElse(Status.failed(n, w)))

      case Some((_, Some(_))) =>
        Status.ok.point[M]
    }

  def report[M[_] : Monad, A](n : SuccessCount, size0: Size, seed0: Seed, p: Property[M, A]): M[Report] = {
    def loop(successes: SuccessCount, discards: DiscardCount, size: Size, seed: Seed): M[Report] =
      if (size.value > 99)
        loop(successes, discards, Size(0), seed)
      else if (successes == n)
        Report(successes, discards, OK).point[M]
      else if (discards.value >= 100)
        Report(successes, discards, GaveUp).point[M]
      else
        p.run.run(size, seed).run.flatMap(x =>
          x.value._2 match {
            case None =>
              loop(successes, discards.inc, size.inc, x.value._1)

            case Some((_, None)) =>
              takeSmallest(ShrinkCount(0), x.map(_._2)).map(y => Report(successes, discards, y))

            case Some((m, Some(_))) =>
              loop(successes.inc, discards, size.inc, x.value._1)
          }
        )
    loop(SuccessCount(0), DiscardCount(0), size0, seed0)
  }

  def recheck[M[_] : Monad](size: Size, seed: Seed)(p: Property[M, Unit]): M[Report] =
    report(SuccessCount(1), Size(0), seed, p)
}

/**********************************************************************/
// Reporting

/** The numbers of times a property was able to shrink after a failing test. */
case class ShrinkCount(value: Int) {

  def inc: ShrinkCount =
    ShrinkCount(value + 1)
}

/** The number of tests a property ran successfully. */
case class SuccessCount(value: Int) {

  def inc: SuccessCount =
    SuccessCount(value + 1)
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
