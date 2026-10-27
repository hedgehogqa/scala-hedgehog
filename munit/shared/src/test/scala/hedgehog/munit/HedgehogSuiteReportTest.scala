package hedgehog
package munit

import hedgehog.core.{DiscardCount, Info, SourcePos}

import _root_.munit.{Exceptions, FailException}

import scala.concurrent.Future

/** Checks the failure message which `HedgehogSuite` gives to munit.
  *
  * Each test builds a `HedgehogSuite` with one property, runs the munit test
  * which `property` registers, and checks the message of the failure. The
  * suites are anonymous classes, so that munit does not find and run them.
  */
class HedgehogSuiteReportTest extends _root_.munit.FunSuite {

  private implicit val ec: scala.concurrent.ExecutionContext =
    munitExecutionContext

  private val seedLog =
    "Using (random seed|seed from environment variable HEDGEHOG_SEED): -?\\d+".r

  /** munit puts the location and the source lines of the `property` call in
    * front of the message, so only the end of the message is checked.
    */
  private def assertFailureMessage(
      suite: HedgehogSuite,
      expectedReport: String
  ): Future[Unit] =
    runOnlyTest(suite).map { message =>
      val lastNewline = message.lastIndexOf('\n')
      val report = message.substring(0, lastNewline)
      val seedLine = message.substring(lastNewline + 1)
      assertEquals(report.takeRight(expectedReport.length), expectedReport)
      assert(seedLog.pattern.matcher(seedLine).matches(), seedLine)
    }

  private def runOnlyTest(suite: HedgehogSuite): Future[String] =
    suite.munitTests().toList match {
      case t :: Nil =>
        /* A `Future` boxes an `Error`, and `FailException` is one, so the
         * failure is unwrapped as munit does it.
         */
        t.body().failed.map(Exceptions.rootCause).map {
          case e: FailException => e.message
          case e => throw new AssertionError(s"Expected a FailException but got $e", e)
        }
      case ts =>
        Future.failed(new AssertionError(s"Expected one test but got ${ts.length.toString}"))
    }

  test("a failed property fails with the report and the seed") {
    val suite = new HedgehogSuite {
      property("fails") {
        Property.point(
          Result
            .failure(SourcePos("x/Spec.scala", "Spec.scala", 7))
            .log(Info("boom"))
        )
      }
    }
    val qualifiedName = suite.getClass.getName + ".fails"
    assertFailureMessage(
      suite,
      s"${Console.RED}-${Console.RESET} $qualifiedName: Falsified after 0 passed tests\n" +
        "> x/Spec.scala:7\n" +
        s"> at $qualifiedName(Spec.scala:7)\n" +
        "> boom"
    )
  }

  test("a property which gave up fails with the report and the seed") {
    val suite = new HedgehogSuite {
      property("gives up", _.copy(discardLimit = DiscardCount(3))) {
        Property.discard.map(_ => Result.success)
      }
    }
    val qualifiedName = suite.getClass.getName + ".gives up"
    assertFailureMessage(
      suite,
      s"${Console.RED}-${Console.RESET} $qualifiedName: Gave up after only 0 passed test. 3 were discarded"
    )
  }

  test("a passed property does not fail") {
    val suite = new HedgehogSuite {
      property("passes") {
        Property.point(Result.success)
      }
    }
    suite.munitTests().toList match {
      case t :: Nil =>
        t.body().map(_ => ())
      case ts =>
        Future.failed(new AssertionError(s"Expected one test but got ${ts.length.toString}"))
    }
  }
}
