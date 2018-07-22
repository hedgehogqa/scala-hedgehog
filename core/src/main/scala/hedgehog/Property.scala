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

case class PropertyT[M[_], A](run: GenT[M, (List[Log], Option[A])]) {

  def map[B](f: A => B)(implicit F: Functor[M]): PropertyT[M, B] =
    PropertyT(run.map(_.map(_.map(f))))

  def flatMap[B](f: A => PropertyT[M, B])(implicit F: Monad[M]): PropertyT[M, B] =
    PropertyT(run.flatMap(x =>
      x._2.cata(
        a => f(a).run.map(y => (x._1 ++ y._1, y._2))
      , GenT.GenApplicative(F).point((x._1, None))
      )
    ))

  def check(seed: Seed)(implicit F: Monad[M]): M[Report] =
    propertyT.report(SuccessCount(100), Size(0), seed, this)

  def checkRandom(p: PropertyT[M, Unit])(implicit F: MonadIO[M]): M[Report] =
    Seed.fromTime.liftIO.flatMap(s => check(s))
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

trait PropertyTOps[M[_]] {

  def fromGen[A](gen: GenT[M, A])(implicit F: Monad[M]): PropertyT[M, A] =
    PropertyT(gen.map(x => (Nil, Some(x))))

  def hoist[A](a: (List[Log], A))(implicit F: Monad[M]): PropertyT[M, A] =
    PropertyT(GenT.GenApplicative(F).point(a.map(some)))

  def writeLog(log: Log)(implicit F: Monad[M]): PropertyT[M, Unit] =
    hoist((List(log), ()))

  def info(log: String)(implicit F: Monad[M]): PropertyT[M, Unit] =
    writeLog(Info(log))

  def discard(implicit F: Monad[M]): PropertyT[M, Unit] =
    fromGen(genT.discard)

  def failure[A](implicit F: Monad[M]): PropertyT[M, A] =
    PropertyT(GenT.GenApplicative(F).point((Nil, None)))

  def success(implicit F: Monad[M]): PropertyT[M, Unit] =
    hoist((Nil, ()))

  def assert(b: Boolean)(implicit F: Monad[M]): PropertyT[M, Unit] =
    if (b) success else failure

  /**********************************************************************/
  // Reporting

  def isFailure[A, B](n: Node[M, Option[(B, Option[A])]]): Boolean =
    n.value.map(_._2) == Some(None)

  def takeSmallest[A](n: ShrinkCount, t: Node[M, Option[(List[Log], Option[A])]])(implicit F: Monad[M]): M[Status] =
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

  def report[A](n : SuccessCount, size0: Size, seed0: Seed, p: PropertyT[M, A])(implicit F: Monad[M]): M[Report] = {
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

  def recheck(size: Size, seed: Seed)(p: PropertyT[M, Unit])(implicit F: Monad[M]): M[Report] =
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
