/*
 * Copyright 2001-2013 Artima, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hegehog.scalatest

import hedgehog.Property
import hedgehog.core.{PropertyConfig, Seed, Status}
import hedgehog.runner.{SeedSource, Test}
import org.scalactic.{FailureMessages => _, Resources => _, UnquotedString => _, _}
import org.scalatest.{Assertion, Succeeded}
import org.scalatest.exceptions.{GeneratorDrivenPropertyCheckFailedException, StackDepthException}

/**
 * Supertrait for <code>CheckerAsserting</code> typeclasses, which are used to implement and determine the result
 * type of [[org.scalatest.prop.GeneratorDrivenPropertyChecks GeneratorDrivenPropertyChecks]]'s <code>apply</code> and <code>forAll</code> method.
 *
 * <p>
 * Currently, an [[org.scalatest.prop.GeneratorDrivenPropertyChecks GeneratorDrivenPropertyChecks]] expression will have result type <code>Assertion</code>, if the function passed has result type <code>Assertion</code>,
 * else it will have result type <code>Unit</code>.
 * </p>
 */
trait CheckerAsserting[T] {
  /**
   * The result type of the <code>check</code> method.
   */
  type Result

  def succeed(result: T): (Boolean, Option[Throwable])

  /**
   * Perform the property check using the given <code>Prop</code> and <code>Test.Parameters</code>.
   *
   * @param test       The `test` to be checked.
   * @param config     The `config` to be used to check the property.
   * @param prettifier the <code>Prettifier</code> to be used to prettify error message
   * @param pos        the <code>Position</code> of the caller site
   * @param argNames   the list of argument names
   * @return the <code>Result</code> of the property check.
   */
  def check(test: Test, config: PropertyConfig, prettifier: Prettifier, pos: source.Position, argNames: Option[List[String]] = None): Result
}

/**
 * Class holding lowest priority <code>CheckerAsserting</code> implicit, which enables [[org.scalatest.prop.GeneratorDrivenPropertyChecks GeneratorDrivenPropertyChecks]] expressions that have result type <code>Unit</code>.
 */
abstract class UnitCheckerAsserting {

  private val seedSource = SeedSource.fromEnvOrTime()
  private val seed: Seed = Seed.fromLong(seedSource.seed)

  /**
   * Abstract subclass of <code>CheckerAsserting</code> that provides the bulk of the implementations of <code>CheckerAsserting</code>
   * <code>check</code> method.
   */
  abstract class CheckerAssertingImpl[T] extends CheckerAsserting[T] {

    import CheckerAsserting._

    /** If the check succeeds, call <code>indicateSuccess</code>, else call <code>indicateFailure</code>. */
    def check(test: Test, config: PropertyConfig, prettifier: Prettifier, pos: source.Position, argNames: Option[List[String]] = None): Result = {
      val result = Property.check(test.withConfig(config), test.result, seed)

      if (result.status != Status.ok) {
        val report = Test.renderReport(this.getClass.getName, test, result, ansiCodesSupported = true)
        // fail the test using scalatest
        indicateFailure(_ => report, report, Nil, Nil, None, pos)
      } else indicateSuccess(FailureMessages.propertyCheckSucceeded())
    }

    private[scalacheck] def indicateSuccess(message: => String): Result

    private[scalacheck] def indicateFailure(messageFun: StackDepthException => String, undecoratedMessage: => String, scalaCheckArgs: List[Any], scalaCheckLabels: List[String], optionalCause: Option[Throwable], pos: source.Position): Result
  }

  /**
   * Provides support of [[org.scalatest.enablers.CheckerAsserting CheckerAsserting]] for Unit.  Do nothing when the check succeeds,
   * but throw [[org.scalatest.exceptions.GeneratorDrivenPropertyCheckFailedException GeneratorDrivenPropertyCheckFailedException]]
   * when check fails.
   */
  implicit def assertingNatureOfT[T]: CheckerAsserting[T] {type Result = Unit} =
    new CheckerAssertingImpl[T] {
      type Result = Unit

      def succeed(result: T) = (true, None)

      private[scalacheck] def indicateSuccess(message: => String): Unit = ()

      private[scalacheck] def indicateFailure(messageFun: StackDepthException => String, undecoratedMessage: => String, scalaCheckArgs: List[Any], scalaCheckLabels: List[String], optionalCause: Option[Throwable], pos: source.Position): Unit = {
        throw new GeneratorDrivenPropertyCheckFailedException(
          messageFun,
          optionalCause,
          pos,
          None,
          undecoratedMessage,
          scalaCheckArgs,
          None,
          scalaCheckLabels
        )
      }
    }
}

/**
 * Companion object to <code>CheckerAsserting</code> that provides two implicit providers, a higher priority one for passed functions that have result
 * type <code>Assertion</code>, which also yields result type <code>Assertion</code>, and one for any other type, which yields result type <code>Unit</code>.
 */
object CheckerAsserting extends UnitCheckerAsserting {

  /**
   * Provides support of [[org.scalatest.enablers.CheckerAsserting CheckerAsserting]] for Assertion.  Returns [[org.scalatest.Succeeded Succeeded]] when the check succeeds,
   * but throw [[org.scalatest.exceptions.GeneratorDrivenPropertyCheckFailedException GeneratorDrivenPropertyCheckFailedException]]
   * when check fails.
   */
  implicit def assertingNatureOfAssertion: CheckerAsserting[Assertion] {type Result = Assertion} = {
    new CheckerAssertingImpl[Assertion] {
      type Result = Assertion

      def succeed(result: Assertion) = (true, None)

      private[scalacheck] def indicateSuccess(message: => String): Assertion = Succeeded

      private[scalacheck] def indicateFailure(messageFun: StackDepthException => String, undecoratedMessage: => String, scalaCheckArgs: List[Any], scalaCheckLabels: List[String], optionalCause: Option[Throwable], pos: source.Position): Assertion =
        throw new GeneratorDrivenPropertyCheckFailedException(
          messageFun,
          optionalCause,
          pos,
          None,
          undecoratedMessage,
          scalaCheckArgs,
          None,
          scalaCheckLabels
        )
    }
  }
}

