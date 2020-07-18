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
  , withExamples: WithExamples
  )

object PropertyConfig {

  def default: PropertyConfig =
    PropertyConfig(SuccessCount(100), DiscardCount(100), ShrinkLimit(1000), WithExamples.NoExamples)
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
  final def takeSmallestG[A, B](n: ShrinkCount, slimit: ShrinkLimit, t: Tree[A])(p: A => Boolean)(e: (ShrinkCount, A) => B): B = {
    if (n.value < slimit.value && p(t.value)) {
      findMap(t.children.value)(m => if (p(m.value)) some(m) else Option.empty) match {
        case None =>
          e(n, t.value)

        case Some(m) =>
          takeSmallestG(n.inc, slimit, m)(p)(e)
      }
    } else {
      e(n, t.value)
    }
  }

  def takeSmallest(n: ShrinkCount, slimit: ShrinkLimit, t: Tree[Option[(Journal, Option[Result])]]): Status =
    takeSmallestG(n, slimit, t) {
      case None =>
        false

      case Some((_, r)) =>
        r.forall(!_.success)
    } {
      case (_, None) =>
        Status.gaveUp

      case (n, Some((j, r))) =>
        if (r.forall(!_.success))
          Status.failed(n, j.logs ++ r.map(_.logs).getOrElse(Nil))
        else
          Status.ok
    }

  def takeSmallestExample(n: ShrinkCount, slimit: ShrinkLimit, name: LabelName, t: Tree[Option[(Journal, Option[Result])]]): List[Log] =
    takeSmallestG(n, slimit, t) {
      case None =>
        false

      case Some((j, r)) =>
        r.exists(_.success) && Coverage.covers(j.coverage, name)
    } {
      case (_, None) =>
        Nil

      case (_, Some((j, _))) =>
        j.logs
    }

  def report(config: PropertyConfig, size0: Option[Size], seed0: Seed, p: PropertyT[Result]): Report = {
    // Increase the size proportionally to the number of tests to ensure better coverage of the desired range
    val sizeInc = Size(Math.max(1, Size.max / config.testLimit.value))
    // Start the size at whatever remainder we have to ensure we run with "max" at least once
    val sizeInit = Size((Size.max % Math.min(config.testLimit.value, Size.max)) + sizeInc.value)
    @annotation.tailrec
    def loop(successes: SuccessCount, discards: DiscardCount, size: Size, seed: Seed, coverage: Coverage[CoverCount], examples: Examples): Report =
      if (successes.value >= config.testLimit.value)
        // we've hit the test limit
        Coverage.split(coverage, successes) match {
          case (_, Nil) =>
            config.withExamples match {
              case WithExamples.WithExamples =>
                if (examples.examples.exists(_._2.isEmpty))
                  Report(successes, discards, coverage, examples, Status.failed(ShrinkCount(0), List("Insufficient examples.")))
                else
                  Report(successes, discards, coverage, examples, OK)
              case WithExamples.NoExamples =>
                Report(successes, discards, coverage, examples, OK)
            }
          case _ =>
            Report(successes, discards, coverage, examples, Status.failed(ShrinkCount(0), List("Insufficient coverage.")))
        }
      else if (discards.value >= config.discardLimit.value)
        Report(successes, discards, coverage, examples, GaveUp)
      else {
        val x =
          try {
            p.run.run(size, seed)
          } catch {
            case e: Exception =>
              Property.error(e).run.run(size, seed)
          }
        val t = x.map(_._2)
        x.value._2 match {
          case None =>
            loop(successes, discards.inc, size.incBy(sizeInc), x.value._1, coverage, examples)

          case Some((j, r)) =>
            if (r.forall(!_.success)) {
              Report(successes, discards, coverage, examples, takeSmallest(ShrinkCount(0), config.shrinkLimit, t))
            } else {
              val coverage2 = Coverage.union(Coverage.count(j.coverage), coverage)(_ + _)
              val examples2 =
                config.withExamples match {
                  case WithExamples.NoExamples =>
                    examples

                  case WithExamples.WithExamples =>
                    Examples.addTo(examples, Coverage.labels(j.coverage)) { name =>
                      if (Coverage.covers(j.coverage, name))
                        takeSmallestExample(ShrinkCount(0), config.shrinkLimit, name, t)
                      else
                        Nil
                    }
                }
              loop(successes.inc, discards, size.incBy(sizeInc), x.value._1, coverage2, examples2)
            }
        }
      }
    loop(SuccessCount(0), DiscardCount(0), size0.getOrElse(sizeInit), seed0, Coverage.empty, Examples.empty)
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

/** Whether the report should include an example for each label. */
sealed trait WithExamples

object WithExamples {

  case object WithExamples extends WithExamples
  case object NoExamples extends WithExamples
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

case class Report(tests: SuccessCount, discards: DiscardCount, coverage: Coverage[CoverCount], examples: Examples, status: Status)
