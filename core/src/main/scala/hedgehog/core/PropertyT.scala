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

/** A record containing the details of a test run. */
case class Journal(
    logs: List[Log]
  , coverage: Coverage[Cover]
  ) {

  def ++(o: Journal): Journal =
    Journal(logs ++ o.logs, Coverage.union(coverage, o.coverage)(_ ++ _))

  def log(log: Log): Journal =
    copy(logs = logs ++ List(log))
}

object Journal {

  def empty: Journal =
    Journal(Nil, Coverage.empty)
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

case class PropertyT[A](
    run: GenT[(Journal, Option[A])]
  ) {

  def map[B](f: A => B): PropertyT[B] =
    copy(run = run.map(x =>
      try {
        x.copy(_2 = x._2.map(f))
      } catch {
        // Forgive me, I'm assuming this breaks the Functor laws
        // If there's a better and non-law breaking of handling this we should remove this
        case e: Exception =>
          (x._1.log(Error(e)), None)
      }
    ))

  def flatMap[B](f: A => PropertyT[B]): PropertyT[B] =
    copy(run = run.flatMap(x =>
      x._2.fold(
       GenT.GenApplicative.point((x._1, Option.empty[B]))
      )(a =>
        try {
          f(a).run.map(y => (x._1 ++ y._1, y._2))
        } catch {
          // Forgive me, I'm assuming this breaks the Monad laws
          // If there's a better and non-law breaking of handling this we should remove this
          case e: Exception =>
            Gen.constant((x._1.log(Error(e)), None))
        }
      )
    ))

  /** Records the proportion of tests which satisfy a given condition. */
  def cover(minimum: CoverPercentage, name: LabelName, covered: A => Cover): PropertyT[A] =
    flatMap(a =>
      propertyT.cover(Label(name, minimum, covered(a)))
        .map(_ => a)
    )

  /** Records the proportion of tests which satisfy a given condition. */
  def classify(name: LabelName, covered: A => Cover): PropertyT[A] =
    cover(0, name, covered)

  /**
   * Add a label for each test run.
   * It produces a table showing the percentage of test runs that produced each label.
   */
  def label(name: LabelName): PropertyT[A] =
    cover(0, name, _ => true)

  /** Like 'label', but uses the `toString` value as the label. */
  def collect: PropertyT[A] =
    flatMap(a =>
      propertyT.cover(Label(a.toString, 0, Cover.Cover))
        .map(_ => a)
    )
}

object PropertyT {

  implicit def PropertyMonad: Monad[PropertyT] =
    new Monad[PropertyT] {
      override def map[A, B](fa: PropertyT[A])(f: A => B): PropertyT[B] =
        fa.map(f)
      override def point[A](a: => A): PropertyT[A] =
        propertyT.hoist((Journal.empty, a))
      override def ap[A, B](fa: => PropertyT[A])(f: => PropertyT[A => B]): PropertyT[B] =
        PropertyT(Applicative.zip(fa.run, f.run)
          .map { case ((l1, oa), (l2, oab)) => (l2 ++ l1, oab.flatMap(y => oa.map(y(_)))) }
        )
      override def bind[A, B](fa: PropertyT[A])(f: A => PropertyT[B]): PropertyT[B] =
        fa.flatMap(f)
    }
}

trait PropertyTReporting {

  @annotation.tailrec
  final def takeSmallest(n: ShrinkCount, slimit: ShrinkLimit, t: Tree[Option[(List[Log], Option[Result])]]): Status =
    t.value match {
      case None =>
        Status.gaveUp

      case Some((w, r)) =>
        if (r.forall(!_.success)) {
          if (n.value >= slimit.value) {
            Status.failed(n, w ++ r.map(_.logs).getOrElse(Nil))
          } else {
            findMap(t.children.value)(m =>
              m.value match {
                case Some((_, None)) =>
                  some(m)
                case Some((_, Some(r2))) =>
                  if (!r2.success)
                    some(m)
                  else
                    Option.empty
                case None =>
                  Option.empty
              }
            ) match {
              case Some(m) =>
                takeSmallest(n.inc, slimit, m)
              case None =>
                Status.failed(n, w ++ r.map(_.logs).getOrElse(Nil))
            }
          }
        } else {
          Status.ok
        }
    }

  def report(config: PropertyConfig, size0: Option[Size], seed0: Seed, p: PropertyT[Result]): Report = {
    // Increase the size proportionally to the number of tests to ensure better coverage of the desired range
    val sizeInc = Size(Math.max(1, Size.max / config.testLimit.value))
    // Start the size at whatever remainder we have to ensure we run with "max" at least once
    val sizeInit = Size(Size.max % Math.min(config.testLimit.value, Size.max)).incBy(sizeInc)
    @annotation.tailrec
    def loop(successes: SuccessCount, discards: DiscardCount, size: Size, seed: Seed, coverage: Coverage[CoverCount]): Report =
      if (size.value > Size.max)
        loop(successes, discards, sizeInit, seed, coverage)
      else if (successes.value >= config.testLimit.value)
        // we've hit the test limit
        Coverage.split(coverage, successes) match {
          case (_, Nil) =>
            Report(successes, discards, coverage, OK)
          case _ =>
            Report(successes, discards, coverage, Status.failed(ShrinkCount(0), List("Insufficient coverage.")))
        }
      else if (discards.value >= config.discardLimit.value)
        Report(successes, discards, coverage, GaveUp)
      else {
        val x = p.run.run(size, seed)
        val t = x.map(_._2.map { case (l, r) => (l.logs, r) })
        x.value._2 match {
          case None =>
            loop(successes, discards.inc, size.incBy(sizeInc), x.value._1, coverage)

          case Some((_, None)) =>
            Report(successes, discards, coverage, takeSmallest(ShrinkCount(0), config.shrinkLimit, t))

          case Some((j, Some(r))) =>
            if (!r.success){
              Report(successes, discards, coverage, takeSmallest(ShrinkCount(0), config.shrinkLimit, t))
            } else
              loop(successes.inc, discards, size.incBy(sizeInc), x.value._1,
                Coverage.union(Coverage.count(j.coverage), coverage)(_ + _))
        }
      }
    loop(SuccessCount(0), DiscardCount(0), size0.getOrElse(sizeInit), seed0, Coverage.empty)
  }

  def recheck(config: PropertyConfig, size: Size, seed: Seed)(p: PropertyT[Result]): Report =
    report(config.copy(testLimit = SuccessCount(1)), Some(size), seed, p)
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

case class Report(tests: SuccessCount, discards: DiscardCount, coverage: Coverage[CoverCount], status: Status)
