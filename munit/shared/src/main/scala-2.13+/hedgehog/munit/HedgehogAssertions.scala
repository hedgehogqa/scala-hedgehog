package hedgehog
package munit

import hedgehog.{core => hc}
import _root_.munit.Assertions

import java.{lang => jl}

/** Mirrors munit.Assertions assertions, allowing munit users to use familiar
  * assertions while returning proper hedgehog.Result objects in property tests.
  *
  * Signatures don't line up exactly -- munit.Assertions is not F-bound
  * polymorphic, so there is no way to align the signatures of the same
  * argumentns with different return types in scala. We can, however, simply
  * omit the implicit location arguments, because hedgehog.Result doesn't
  * require munit.Location. Except in cases where Location is passed explicitly
  * (which should be rare in user code), this should result in fairly easy
  * adoption by munit and munit-scalacheck users.
  */
trait HedgehogAssertions { self: Assertions =>

  /** Turns off hedgehog munit-like assertions, so users can use both property-
    * and non-property- based-tests in their test suites. By using the passed
    * assertions parameter, all the standard munit assertions that do not return
    * unit are available, despite name ambiguities.
    *
    * ==Usage==
    * {{{
    *   test("1 + 1 is 2"){
    *     withMunitAssertions{ assertions =>
    *       assertEquals(1 + 1, 2)
    *     }
    *   }
    * }}}
    *
    * @param body
    *   a test body, taking an assertions parameter
    * @return
    */
  def withMunitAssertions(body: => Assertions => Any): Any = body(
    this.asInstanceOf[Assertions]
  )

  /** @see
    *   hedgehog.core.Result.Failure
    */
  type Failure = Result.Failure

  /** @see
    *   hedgehog.core.Result.Success
    */
  type Success = Result.Success.type

  /** Alias for Result.Success
    *
    * @see
    *   hedgehog.core.Result.Success
    */
  lazy val Success: Success = Result.Success

  /** Alias for Result.Failure
    *
    * @see
    *   See hedgehog.core.Result.Failure
    */
  def Failure(log: List[hc.Log]) = Result.Failure(log)

  /** Alias for Result.success
    *
    * @see
    *   hedgehog.core.Result.success
    */
  def success = Result.success

  /** Alias for Result.failure
    *
    * @see
    *   hedgehog.core.Result.failure
    */
  def failure = Result.failure

  /** Alias for Result.error
    *
    * @see
    *   hedgehog.core.Result.error
    */
  def error(e: Exception) = Result.error(e)

  /** Alias for Result.all
    *
    * @see
    *   hedgehog.core.Result.all
    */
  def all(l: List[Result]) = Result.all(l)

  /** Alias for Result.any
    *
    * @see
    *   hedgehog.core.Result.any
    */
  def any(l: List[Result]) = Result.any(l)

  /** Alias for Result.diff
    *
    * @see
    *   hedgehog.core.Result.diff
    */
  def diff[A, B](a: A, b: B)(f: (A, B) => Boolean) = Result.diff(a, b)(f)

  /** Alias for Result.diffNamed
    *
    * @see
    *   hedgehog.core.Result.diffNamed
    */
  def diffNamed[A, B](logName: String, a: A, b: B)(f: (A, B) => Boolean) =
    Result.diffNamed(logName, a, b)(f)

  /** Fails the test with a failure Result when `cond` is `false`.
    *
    * Analagous to munit.Assertions.assert.
    *
    * Only the condition is used. Clues are ignored.
    *
    * @param cond
    * @param clue
    *   ignored -- usage triggers the deprecation warning
    * @return
    *   Success iff cond is true. Failure otherwise.
    */
  @deprecated(
    "Clues are unnecessary with hedgehog. Use HedgehogAssertions.diff, which will automatically output clues",
    ""
  )
  def assert(cond: => Boolean, clue: => Any): Result =
    Result.assert(
      cond
    )

  /** Fails the test with a failure Result when `cond` is `false`.
    *
    * @param cond
    * @return
    *   Success iff cond is true. Failure otherwise.
    */
  def assert(cond: => Boolean): Result = Result.assert(cond)

  /** Fails the test if `obtained` and `expected` are non-equal using `==`.
    *
    * Analagous to munit.Assertions.assert.
    *
    * Only the obtained and expected values are used.
    *
    * @param obtained
    *   The actual value
    * @param expected
    * @param clue
    *   Ignored -- Triggers deprecation warning
    * @param ev
    *   Evidence that A and B are of the same type for the comparison to be
    *   valid.
    * @return
    *   Success iff obtained == expected. Failure otherwise.
    */
  @deprecated(
    "Clues are unnecessary with hedgehog. Use HedgehogAssertions.assertEquals, which will automatically output clues",
    ""
  )
  def assertEquals[A, B](obtained: A, expected: B, clue: => Any)(implicit
      ev: B <:< A
  ): Result = assertEquals(obtained, expected)

  /** Fails the test if `obtained` and `expected` are non-equal using `==`.
    *
    * @param obtained
    *   The actual value
    * @param expected
    *   The expected value
    * @return
    *   Success iff obtained == expected. Failure otherwise.
    */
  def assertEquals[A, B](obtained: A, expected: B)(implicit
      ev: B <:< A
  ): Result =
    diff(obtained, expected)(_ == _)

  /** Double specialized version of `HedgehogAssertions.assertEquals`.
    *
    * Asserts two double values are equal +- some error value.
    *
    * Analagous to munit.Assertions.assertEqualsDouble.
    *
    * Only the obtained, expected and delta parameters are used.
    *
    * @param obtained
    *   The actual value.
    * @param expected
    *   The expected value.
    * @param delta
    *   The error allowed for double == comparison.
    * @param clue
    *   Ignored -- usage triggers a deprecation warning
    * @return
    *   Success iff obtained approximately equals expected +- delta. Failure
    *   otherwise.
    */
  @deprecated(
    "Clues are unnecessary with hedgehog. Use HedgehogAssertions.diffDouble, which will automatically output clues",
    ""
  )
  def assertEqualsDouble(
      obtained: Double,
      expected: Double,
      delta: Double,
      clue: => Any
  ): Result = diffDouble(obtained, expected, delta)

  /** Asserts two doubles are equal +- some erorr value.
    *
    * @param obtained
    *   The actual value.
    * @param expected
    *   The expected value.
    * @param delta
    *   The error allowed for double == comparison. Default is 0.00.
    * @return
    *   Success iff obtained approximately equals expected +- delta. Failure
    *   otherwis
    */
  def diffDouble(
      obtained: Double,
      expected: Double,
      delta: Double = 0.00
  ): Result =
    diff(obtained, expected) { (a, b) =>
      jl.Double.compare(expected, obtained) == 0 || Math.abs(
        expected - obtained
      ) <= delta
    }

  /** Float specialized version of assertEquals.
    *
    * Asserts two floats are equal within +- some error value.
    *
    * Analagous to munit.Assertions.assertEqualsFloat.
    *
    * @param obtained
    *   The actual value
    * @param expected
    *   The expected value
    * @param delta
    *   The error allowed for float == comparison.
    * @param clue
    *   Ignored -- usage triggers deprecation warning
    * @return
    *   Success iff obtained approximately equals expected +- delta. Failure
    *   otherwise.
    */
  @deprecated(
    "Clues are unnecessary with hedgehog. Use HedgehogAssertions.diffFloat, which will automatically output clues",
    ""
  )
  def assertEqualsFloat(
      obtained: Float,
      expected: Float,
      delta: Float,
      clue: => Any
  ): Result = diffFloat(obtained, expected, delta)

  /** Float specialized version of HedgehogAssertions.assertEquals.
    *
    * Asserts two floats are equal within +- some error value.
    *
    * @param obtained
    *   The actual value
    * @param expected
    *   The expected value
    * @param delta
    *   The error allowed for float == comparison. Default is 0.0f
    * @return
    *   Success iff obtained approximately equals expected +- delta. Failure
    *   otherwise.
    */
  def diffFloat(obtained: Float, expected: Float, delta: Float = 0.0f): Result =
    Result.diff(obtained, expected) { (a, b) =>
      jl.Float.compare(a, b) == 0 || Math.abs(expected - obtained) <= delta
    }

  /** Asserts two strings are equal without outputting a diff.
    *
    * Analagous to munit.Assertions.assertNoDiff.
    *
    * @param obtained
    *   The actual string
    * @param expected
    *   The expected string
    * @param clue
    *   Ignored -- usage triggers a deprecation warning
    * @return
    *   Success iff actual is obtained. Failure otherwise.
    */
  @deprecated(
    "Clues are unnecessary with hedgehog. Use HedgehogAssertions.diff, which will automatically output clues",
    ""
  )
  def assertNoDiff(obtained: String, expected: String, clue: => Any): Result =
    assertNoDiff(obtained, expected)

  /** Asserts two strings are equal.
    *
    * @param obtained
    *   The actual string
    * @param expected
    *   The expected value
    * @return
    *   Success iff actual is obtained. Failure otherwise.
    */
  def assertNoDiff(obtained: String, expected: String): Result = assert(
    obtained == expected
  )

  /** Asserts obtained is not equal to expected using ==.
    *
    * Analagous to munit.Assertions.notEquals.
    *
    * @param obtained
    *   The actual value
    * @param expected
    *   The expected value
    * @param clue
    *   Ignored -- Usage triggers a deprecation warning
    * @param ev
    *   Evidence that obtained and expected are of the same type.
    * @return
    *   Success iff obtained != expected. Failure otherwise.
    */
  @deprecated(
    "Clues are unnecessary with hedgehog. Use HedgehogAssertions.assertNotEquals, which will automatically output clues",
    ""
  )
  def assertNotEquals[A, B](obtained: A, expected: B, clue: => Any)(implicit
      ev: A =:= B
  ): Result = assertNotEquals(obtained, expected)

  /** Asserts two values are nonequal.
    *
    * @param obtained
    *   The actual value
    * @param expected
    *   The expected value
    * @param ev
    *   Ensures that obtained and expected are of the same type.
    * @return
    *   Success iff obtained != actua.
    */
  def assertNotEquals[A, B](obtained: A, expected: B)(implicit
      ev: A =:= B
  ): Result = diff(obtained, expected)(_ != _)

  /** Fails a test.
    *
    * Analagous to munit.Assertions.fail.
    *
    * @param message
    *   The message to include in the failure.
    * @param cause
    *   An optional underlying exception to use as the cause of the failure.
    * @return
    *   Failure, always.
    */
  def fail(message: String, cause: Throwable): Result =
    failure.log(hc.Error(new Exception(message, cause)))

  /** Fails a test with the given message
    *
    * @param message
    * @return
    *   Failure, always.
    */
  def fail(message: String): Result =
    failure.log(hc.Error(new Exception(message)))

}
