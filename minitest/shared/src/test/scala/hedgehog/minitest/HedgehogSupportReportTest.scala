package hedgehog.minitest

import minitest.SimpleTestSuite
import minitest.api.{DefaultExecutionContext, Result => MResult}
import hedgehog.core.{DiscardCount, Info, SourcePos}
import hedgehog._

import scala.concurrent.{ExecutionContext, Future}

/** Checks the failure message which `HedgehogSupport` gives to minitest.
  *
  * Each test builds a suite with one property or example, runs the minitest
  * test which it registers, and checks the message of the failure. The suites
  * are anonymous classes, so that minitest does not find and run them.
  */
object HedgehogSupportReportTest extends SimpleTestSuite {

  private implicit val ec: ExecutionContext = DefaultExecutionContext

  private val seedLog =
    "Using (random seed|seed from environment variable HEDGEHOG_SEED): -?\\d+".r

  private def runOnlyTest(suite: SimpleTestSuite): Future[MResult[Unit]] =
    suite.properties.toList match {
      case spec :: Nil =>
        spec(())
      case specs =>
        Future.failed(new AssertionError(s"Expected one test but got ${specs.length.toString}"))
    }

  private def assertFailureMessage(suite: SimpleTestSuite, expectedReport: String): Future[Unit] =
    runOnlyTest(suite).map {
      case MResult.Failure(message, _, _) =>
        val lastNewline = message.lastIndexOf('\n')
        val report = message.substring(0, lastNewline)
        val seedLine = message.substring(lastNewline + 1)
        assertEquals(report, expectedReport)
        assert(seedLog.pattern.matcher(seedLine).matches(), seedLine)
      case other =>
        fail(s"Expected a failure but got $other")
    }

  testAsync("a failed property fails with the report and the seed") {
    val suite = new SimpleTestSuite with HedgehogSupport {
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

  testAsync("a failed example fails with the report and the seed") {
    val suite = new SimpleTestSuite with HedgehogSupport {
      example("fails") {
        Result
          .failure(SourcePos("x/Spec.scala", "Spec.scala", 7))
          .log(Info("boom"))
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

  testAsync("a property which gave up fails with the report and the seed") {
    val suite = new SimpleTestSuite with HedgehogSupport {
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

  testAsync("a passed property does not fail") {
    val suite = new SimpleTestSuite with HedgehogSupport {
      property("passes") {
        Property.point(Result.success)
      }
    }
    runOnlyTest(suite).map {
      case MResult.Success(_) => ()
      case other => fail(s"Expected a success but got $other")
    }
  }
}
