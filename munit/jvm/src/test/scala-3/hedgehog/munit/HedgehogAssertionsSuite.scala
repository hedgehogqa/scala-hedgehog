package hedgehog
package munit

import hedgehog.core.Log
import _root_.munit.Assertions
import _root_.munit.FunSuite

class HedgehogAssertionsSuite extends FunSuite {

  val instance = FunFixture[HedgehogAssertions](
    setup = { test =>
      new HedgehogAssertions with Assertions {}
    },
    teardown = { suite => () }
  )

  val resultSuccess =
    FunFixture[Result](setup = test => Result.success, teardown = suite => ())

  val resultFailure =
    FunFixture[Result](setup = test => Result.failure, teardown = suite => ())

  val results = FunFixture.map2(resultSuccess, resultFailure)

  val instanceAndResults =
    FunFixture.map3(instance, resultSuccess, resultFailure)

  val exampleOne = FunFixture[Int](setup = test => 1, teardown = suite => ())

  val exampleTwo = FunFixture[Int](setup = test => 2, teardown = suite => ())

  val comparisonFunction = FunFixture[(Int, Int) => Boolean](
    setup = test => (x: Int, y: Int) => x < y,
    teardown = suite => ()
  )

  val instanceExamplesAndComparisonFunction = FunFixture.map2(
    FunFixture.map3(instance, exampleOne, exampleTwo),
    comparisonFunction
  )

  val logName =
    FunFixture[String](setup = test => "logName", teardown = suite => ())

  val instanceLogNameExamplesAndComparisonFunction =
    FunFixture.map2(instanceExamplesAndComparisonFunction, logName)

  val cond = FunFixture[Boolean](setup = test => true, teardown = suite => ())

  val failCond =
    FunFixture[Boolean](setup = test => false, teardown = suite => ())

  val clue = FunFixture[String](setup = test => "clue", teardown = suite => ())

  val instanceConditionAndClue = FunFixture.map3(instance, cond, clue)

  val instanceConditionClueAndSuccess =
    FunFixture.map2(instanceConditionAndClue, resultSuccess)

  val failException = FunFixture[Exception](
    setup = test => new Exception("fail!"),
    teardown = suite => ()
  )

  val instanceAndSuccess = FunFixture.map2(instance, resultSuccess)

  val instanceAndFailException = FunFixture.map2(instance, failException)

  val log = FunFixture[Log](
    setup = test => Log.String2Log("woot"),
    teardown = suite => ()
  )

  val instanceAndLog = FunFixture.map2(instance, log)

  val instanceConditionAndSuccess =
    FunFixture.map3(instance, cond, resultSuccess)

  val instanceConditionAndFailure =
    FunFixture.map3(instance, failCond, resultFailure)

  val instanceAndExample1AndClueAndSuccess =
    FunFixture.map2(FunFixture.map3(instance, exampleOne, clue), resultSuccess)

  val failResult = FunFixture[Result](
    setup = test =>
      Result.failure
        .log("=== Failed ===")
        .log("--- lhs ---")
        .log("1")
        .log("--- rhs ---")
        .log("2"),
    teardown = suite => ()
  )

  val instanceExamplesAndClueAndFailureExamples =
    FunFixture.map3(
      FunFixture.map3(instance, exampleOne, exampleTwo),
      clue,
      failResult
    )

  val instanceExamplesAndSuccess =
    FunFixture.map3(instance, exampleOne, resultSuccess)

  val instanceExamplesAndFailure = FunFixture.map2(
    FunFixture.map3(instance, exampleOne, exampleTwo),
    failResult
  )

  val doubleExample =
    FunFixture[Double](setup = testOptions => 1.00, teardown = _ => ())

  val doubleExample2 =
    FunFixture[Double](setup = testOptions => 2.00, teardown = _ => ())

  val doubleExample3 =
    FunFixture[Double](setup = testOptions => 3.00, teardown = _ => ())

  val instanceDoubleExampleClueAndSuccess = FunFixture.map2(
    FunFixture.map3(instance, doubleExample, clue),
    resultSuccess
  )

  val deltaDouble =
    FunFixture[Double](setup = testOptions => 1.00, teardown = _ => ())

  val instanceDoubleExamplesDeltaClueAndSuccess = FunFixture.map2(
    FunFixture.map3(
      FunFixture.map3(instance, doubleExample, doubleExample2),
      deltaDouble,
      clue
    ),
    resultSuccess
  )

  val resultDeltaFailure = FunFixture[Result](
    setup = testOptions => {
      Result.failure
        .log("=== Failed ===")
        .log("--- lhs ---")
        .log("1.0")
        .log("--- rhs ---")
        .log("3.0")
    },
    teardown = _ => ()
  )

  val instanceDoubleExamplesDeltaClueAndFailure = FunFixture.map2(
    FunFixture.map3(
      FunFixture.map3(instance, doubleExample, doubleExample3),
      deltaDouble,
      clue
    ),
    resultDeltaFailure
  )

  val instanceDoubleExamplesDeltaAndSuccess = FunFixture.map3(
    FunFixture.map3(instance, doubleExample, doubleExample2),
    deltaDouble,
    resultSuccess
  )

  val instanceDoubleExamplesDeltaAndFailure = FunFixture.map3(
    FunFixture.map3(instance, doubleExample, doubleExample3),
    deltaDouble,
    resultDeltaFailure
  )

  val floatExample = FunFixture[Float](setup = _ => 1.0f, teardown = _ => ())
  val floatExample2 = FunFixture[Float](setup = _ => 2.0f, teardown = _ => ())
  val floatDelta = FunFixture[Float](setup = _ => 1.0f, teardown = _ => ())
  val instanceFloatExamplesDeltaClueAndSuccess = FunFixture.map2(
    FunFixture.map3(
      FunFixture.map3(instance, floatExample, floatExample2),
      floatDelta,
      clue
    ),
    resultSuccess
  )

  val floatExample3 = FunFixture[Float](setup = _ => 3.0f, teardown = _ => ())

  val instanceFloatExamplesDeltaClueAndFailure = FunFixture.map2(
    FunFixture.map3(
      FunFixture.map3(instance, floatExample, floatExample3),
      floatDelta,
      clue
    ),
    resultDeltaFailure
  )

  val instanceFloatExamplesDeltaAndSuccess = FunFixture.map3(
    FunFixture.map3(instance, floatExample, floatExample2),
    floatDelta,
    resultSuccess
  )

  val instanceFloatExamplesDeltaAndFailure = FunFixture.map3(
    FunFixture.map3(instance, floatExample, floatExample3),
    floatDelta,
    resultDeltaFailure
  )

  val exampleStr =
    FunFixture[String](setup = _ => "example", teardown = _ => ())

  val instanceExampleClueAndSuccess =
    FunFixture.map2(FunFixture.map3(instance, exampleStr, clue), resultSuccess)

  val exampleStr2 =
    FunFixture[String](setup = _ => "example2", teardown = _ => ())

  val instanceExamplesClueAndFailure = FunFixture.map3(
    FunFixture.map3(instance, exampleStr, exampleStr2),
    clue,
    resultFailure
  )

  val instanceExampleAndSuccess =
    FunFixture.map3(instance, exampleStr, resultSuccess)

  val instanceExampleStringsAndFailure = FunFixture.map2(
    FunFixture.map3(instance, exampleStr, exampleStr2),
    resultFailure
  )

  val instanceNonequalExamplesClueAndSuccess = FunFixture.map3(
    FunFixture.map3(instance, exampleStr, exampleStr2),
    clue,
    resultSuccess
  )

  val resultStringFailure = FunFixture[Result](
    setup = _ =>
      Result.failure
        .log("=== Failed ===")
        .log("--- lhs ---")
        .log("example")
        .log("--- rhs ---")
        .log("example"),
    teardown = _ => ()
  )

  val instanceExampleClueAndFailure = FunFixture.map2(
    FunFixture.map3(instance, exampleStr, clue),
    resultStringFailure
  )

  val instanceNonequalExamplesAndSuccess = FunFixture.map2(
    FunFixture.map3(instance, exampleStr, exampleStr2),
    resultSuccess
  )

  val resultStringEqualFailure = FunFixture[Result](
    setup = _ =>
      Result.failure
        .log("=== Failed ===")
        .log("--- lhs ---")
        .log("example")
        .log("--- rhs ---")
        .log("example"),
    teardown = _ => ()
  )

  val instanceExampleStringAndFailure =
    FunFixture.map3(instance, exampleStr, resultStringEqualFailure)

  val failureThrowable = FunFixture[Throwable](
    setup = _ => new Exception("Expected"),
    teardown = _ => ()
  )

  val resultFailCauseFailure = FunFixture[Result](
    setup =
      _ => Result.error(new Exception("example", new Exception("Expected"))),
    teardown = _ => ()
  )

  val instanceMessageCauseAndFailure = FunFixture.map2(
    FunFixture.map3(instance, exampleStr, failureThrowable),
    resultFailCauseFailure
  )

  val resultFail = FunFixture[Result](
    setup = _ => Result.error(new Exception("example")),
    teardown = _ => ()
  )

  val instanceMessageAndFailure =
    FunFixture.map3(instance, exampleStr, resultFail)

  instance.test("success should be Result.success") { hedgehogAssertions =>
    assertEquals(hedgehogAssertions.success, Result.success)
  }

  instance.test("failure should be Result.failure") { hedgehogAssertions =>
    assertEquals(hedgehogAssertions.failure, Result.failure)
  }

  instanceAndFailException.test(
    "error(exception) should be Result.error(exception)"
  ) { case (hedgehogAssertions, exception) =>
    assertEquals(hedgehogAssertions.error(exception), Result.error(exception))
  }

  instanceAndSuccess.test("all(results) should be Result.all(results)") {
    case (hedgehogAssertions, successResult) =>
      val results = List(successResult, successResult)
      assertEquals(hedgehogAssertions.all(results), Result.all(results))
  }

  instanceAndResults.test("any(results) should be Result.any(reults)") {
    case (hedgehogAssertions, successResult, failureResult) =>
      val results = List(successResult, failureResult)
      assertEquals(hedgehogAssertions.any(results), Result.any(results))
  }

  instanceExamplesAndComparisonFunction.test(
    "diff(a, b)(comparison) should be Result.diff(a, b)(comparison)"
  ) { case ((hedgehogAssertions, a, b), comparison) =>
    assertEquals(
      hedgehogAssertions.diff(a, b)(comparison),
      Result.diff(a, b)(comparison)
    )
  }

  instanceLogNameExamplesAndComparisonFunction.test(
    "diffNamed(logName, a, b)(comparison) should be Result.diffNamed(logName, a, b)(comparison)"
  ) { case ((((hedgehogAssertions, a, b), comparison), logName)) =>
    assertEquals(
      hedgehogAssertions.diffNamed(logName, a, b)(comparison),
      Result.diffNamed(logName, a, b)(comparison)
    )
  }

  instanceConditionClueAndSuccess.test(
    "assert(true, clue) should be Result.Success"
  ) { case ((hedgehogAssertions, condition, clue), resultSuccess) =>
    assertEquals(
      hedgehogAssertions.assert(condition, clue),
      resultSuccess
    )
  }

  instanceConditionAndSuccess.test(
    "assert(true) should be Result.Success"
  ) { case (hedgehogAssertions, condition, success) =>
    assertEquals(hedgehogAssertions.assert(condition), success)
  }

  instanceConditionAndFailure.test(
    "assert(false) should be Result.Failure"
  ) { case (hedgehogAssertions, failCond, failResult) =>
    assertEquals(hedgehogAssertions.assert(failCond), failResult)
  }

  instanceAndExample1AndClueAndSuccess.test(
    "assertEquals(example, example, clue) should be Result.success"
  ) { case ((hedgehogAssertions, example, clue), success) =>
    assertEquals(
      hedgehogAssertions.assertEquals(example, example, clue),
      success
    )
  }

  instanceExamplesAndClueAndFailureExamples.test(
    "assertEquals(example1, example2, clue) should be Result.failure with a difference log"
  ) { case ((hedgehogAssertions, example1, example2), clue, resultFailure) =>
    assertEquals(
      hedgehogAssertions.assertEquals(example1, example2, clue),
      resultFailure
    )
  }

  instanceExamplesAndSuccess.test(
    "assertEquals(example, example, clue), should be Success"
  ) { case (hedgehogAssertions, example, resultSuccess) =>
    assertEquals(
      hedgehogAssertions.assertEquals(example, example),
      resultSuccess
    )

  }

  instanceExamplesAndFailure.test(
    "assertEquals(example1, example2) should be a Result.Failure with a log"
  ) { case ((hedgehogAssertions, example1, example2), resultFailure) =>
    assertEquals(
      hedgehogAssertions.assertEquals(example1, example2),
      resultFailure
    )
  }

  instanceDoubleExampleClueAndSuccess.test(
    "assertEqualsDouble(example, example, 0.00, clue), should be Success"
  ) { case ((hedgehogAssertions, example, clue), resultSuccess) =>
    assertEquals(
      hedgehogAssertions.assertEquals(example, example, 0.00),
      resultSuccess
    )
  }

  instanceDoubleExamplesDeltaClueAndSuccess.test(
    "assertEqualsDouble(example1, example2, delta, clue) should be Success"
  ) {
    case (
          ((hedgehogAssertions, example1, example2), delta, clue),
          resultSuccess
        ) =>
      assertEquals(
        hedgehogAssertions.assertEqualsDouble(example1, example2, delta, clue),
        resultSuccess
      )
  }

  instanceDoubleExamplesDeltaClueAndFailure.test(
    "assertEqualsDouble(example1, example3, delta, clue) should be a failure with a diff log"
  ) {
    case (
          ((hedgehogAssertions, example1, example2), delta, clue),
          resultFailure
        ) =>
      assertEquals(
        hedgehogAssertions.assertEqualsDouble(example1, example2, delta, clue),
        resultFailure
      )
  }

  instanceDoubleExamplesDeltaAndSuccess.test(
    "diffDouble(example1, example2, delta) should be Success"
  ) { case ((hedgehogAssertions, example1, example2), delta, resultSuccess) =>
    assertEquals(
      hedgehogAssertions.diffDouble(example1, example2, delta),
      resultSuccess
    )
  }

  instanceDoubleExamplesDeltaAndFailure.test(
    "diffDouble(example1, example3, delta) should be Result.Failure with a diff log"
  ) { case ((hedgehogAssertions, example1, example3), delta, resultFailure) =>
    assertEquals(
      hedgehogAssertions.diffDouble(example1, example3, delta),
      resultFailure
    )
  }

  instanceFloatExamplesDeltaClueAndSuccess.test(
    "assertEqualsFloat(example1, example2, delta, clue) should be Success"
  ) {
    case (
          ((hedgehogAssertions, example1, example2), delta, clue),
          resultSuccess
        ) =>
      assertEquals(
        hedgehogAssertions.assertEqualsFloat(example1, example2, delta, clue),
        resultSuccess
      )
  }

  instanceFloatExamplesDeltaClueAndFailure.test(
    "assertEqualsFloat(example1, example3, delta, clue) should be Result.Failure with a diff log"
  ) {
    case (
          ((hedgehogAssertions, example1, example3), delta, clue),
          resultFailure
        ) =>
      assertEquals(
        hedgehogAssertions.assertEqualsFloat(example1, example3, delta, clue),
        resultFailure
      )
  }

  instanceFloatExamplesDeltaAndSuccess.test(
    "diffFloat(example1, example2, delta) should be Success"
  ) { case ((hedgehogAssertions, example1, example2), delta, resultSuccess) =>
    assertEquals(
      hedgehogAssertions.diffFloat(example1, example2, delta),
      resultSuccess
    )
  }

  instanceFloatExamplesDeltaAndFailure.test(
    "diffFloat(example1, example3, delta) should be Result.Failure with a diff log"
  ) { case ((hedgehogAssertions, example1, example3), delta, resultFailure) =>
    assertEquals(
      hedgehogAssertions.diffFloat(example1, example3, delta),
      resultFailure
    )
  }

  instanceExampleClueAndSuccess.test(
    "assertNoDiff(str, str, clue) should be Result.success"
  ) { case ((hedgehogAssertions, str, clue), resultSuccess) =>
    assertEquals(
      hedgehogAssertions.assertNoDiff(str, str, clue),
      resultSuccess
    )
  }

  instanceExamplesClueAndFailure.test(
    "assertNoDiff(str, str2, clue) should be Result.failure"
  ) { case ((hedgehogAssertions, str, str2), clue, resultFailure) =>
    assertEquals(
      hedgehogAssertions.assertNoDiff(str, str2, clue),
      resultFailure
    )
  }

  instanceExampleAndSuccess.test("assertNoDiff(str, str) should be Success") {
    case (hedgehogAssertions, str, resultSuccess) =>
      assertEquals(hedgehogAssertions.assertNoDiff(str, str), resultSuccess)
  }

  instanceExampleStringsAndFailure.test(
    "assertNoDiff(str, str2) should be Result.failure"
  ) { case ((hedgehogAssertions, str, str2), resultFailure) =>
    assertEquals(hedgehogAssertions.assertNoDiff(str, str2), resultFailure)
  }

  instanceNonequalExamplesClueAndSuccess.test(
    "assertNotEquals(str, str2, clue) should be Success"
  ) { case ((hedgehogAssertions, str, str2), clue, resultSuccess) =>
    assertEquals(
      hedgehogAssertions.assertNotEquals(str, str2, clue),
      resultSuccess
    )
  }

  instanceExampleClueAndFailure.test(
    "assertNotEquals(str, str, clue) should be Result.failure with a diff log"
  ) { case ((hedgehogAssertions, str, clue), resultFailure) =>
    assertEquals(
      hedgehogAssertions.assertNotEquals(str, str, clue),
      resultFailure
    )
  }

  instanceNonequalExamplesAndSuccess.test(
    "assertEquals(str, str2) should be Success"
  ) { case ((hedgehogAssertions, str, str2), resultSucces) =>
    assertEquals(hedgehogAssertions.assertNotEquals(str, str2), resultSucces)
  }

  instanceExampleStringAndFailure.test(
    "assertEquals(str, str) should be resultFailure with a diff log"
  ) { case (hedgehogAssertions, str, resultFailure) =>
    assertEquals(hedgehogAssertions.assertNotEquals(str, str), resultFailure)
  }

  instanceMessageCauseAndFailure.test(
    "fail(message, cause) should be a Result.failure with a message and cause log"
  ) { case ((hedgehogAssertions, message, cause), resultFailure) =>
    assertEquals(
      hedgehogAssertions.fail(message, cause).toString(),
      resultFailure.toString()
    )
  }

  instanceMessageAndFailure.test(
    "fail(message) should be a Result.failure with a message"
  ) { case (hedgehogAssertions, message, resultFailure) =>
    assertEquals(
      hedgehogAssertions.fail(message).toString(),
      resultFailure.toString()
    )
  }
}
