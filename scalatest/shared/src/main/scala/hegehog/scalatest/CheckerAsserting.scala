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
import hedgehog.core.{PropertyConfig, PropertyT, Result, Seed, Status}
import hedgehog.runner.{SeedSource, Test}
import org.scalactic.{FailureMessages => _, Resources => _, UnquotedString => _, _}
import org.scalatest.{Assertion, Succeeded}
import org.scalatest.exceptions.{GeneratorDrivenPropertyCheckFailedException, StackDepthException}

/**
 * Supertrait for <code>CheckerAsserting</code> typeclasses, which are used to implement and
 * determine the result type of
 * [[org.scalatest.prop.GeneratorDrivenPropertyChecks GeneratorDrivenPropertyChecks]]'s
 * <code>apply</code> and <code>forAll</code> method.
 *
 * <p> Currently, an
 * [[org.scalatest.prop.GeneratorDrivenPropertyChecks GeneratorDrivenPropertyChecks]] expression
 * will have result type <code>Assertion</code>, if the function passed has result type
 * <code>Assertion</code>, else it will have result type <code>Unit</code>. </p>
 */
trait CheckerAsserting[T] {

  /**
   * The result type of the <code>check</code> method.
   */
  type CheckResult

  private val seedSource = SeedSource.fromEnvOrTime()
  private val seed: Seed = Seed.fromLong(seedSource.seed)

  def succeed(result: T): (Boolean, Option[Throwable]) = (true, None)

  private[scalatest] def indicateSuccess: CheckResult

  private[scalatest] def indicateFailure(
      messageFun: StackDepthException => String,
      undecoratedMessage: => String,
      scalaCheckArgs: List[Any],
      scalaCheckLabels: List[String],
      optionalCause: Option[Throwable],
      pos: source.Position
  ): CheckResult

  /**
   * Converts the Hedgehog property based test into a result scalatest can understand. If the
   * property check succeeds, return a <code>CheckResult</code> indicating success, else return a
   * <code>CheckResult</code> indicating failure.
   *
   * @param test
   *   The `test` to be checked.
   * @param config
   *   The `config` to be used to check the property.
   * @param pos
   *   the <code>Position</code> of the caller site
   * @return
   *   the <code>Result</code> of the property check.
   */
  def check(test: PropertyT[Result], config: PropertyConfig, pos: source.Position): CheckResult = {
    val report = Property.check(config, test, seed)

    val rendered = Test.renderReport(
      this.getClass.getName,
      Test("todo", test),
      report,
      ansiCodesSupported = true
    )
    if (report.status != Status.ok) {
      // fail the test using scalatest
      indicateFailure(_ => rendered, rendered, Nil, Nil, None, pos)
    } else {
      println(rendered)
      indicateSuccess
    }
  }
}

/**
 * Class holding lowest priority <code>CheckerAsserting</code> implicit, which enables
 * [[org.scalatest.prop.GeneratorDrivenPropertyChecks GeneratorDrivenPropertyChecks]] expressions
 * that have result type <code>Unit</code>.
 */
abstract class UnitCheckerAsserting {

  /**
   * Provides support of [[org.scalatest.enablers.CheckerAsserting CheckerAsserting]] for Unit. Do
   * nothing when the check succeeds, but throw
   * [[org.scalatest.exceptions.GeneratorDrivenPropertyCheckFailedException GeneratorDrivenPropertyCheckFailedException]]
   * when check fails.
   */
  implicit def assertingNatureOfT[T]: CheckerAsserting[T] { type CheckResult = Unit } =
    new CheckerAsserting[T] {
      type CheckResult = Unit

      private[scalatest] def indicateSuccess: Unit = ()

      private[scalatest] def indicateFailure(
          messageFun: StackDepthException => String,
          undecoratedMessage: => String,
          scalaCheckArgs: List[Any],
          scalaCheckLabels: List[String],
          optionalCause: Option[Throwable],
          pos: source.Position
      ): Unit = {
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
 * Companion object to <code>CheckerAsserting</code> that provides two implicit providers, a higher
 * priority one for passed functions that have result type <code>Assertion</code>, which also yields
 * result type <code>Assertion</code>, and one for any other type, which yields result type
 * <code>Unit</code>.
 */
object CheckerAsserting {

  /**
   * Provides support of [[org.scalatest.enablers.CheckerAsserting CheckerAsserting]] for Assertion.
   * Returns [[org.scalatest.Succeeded Succeeded]] when the check succeeds, but throw
   * [[org.scalatest.exceptions.GeneratorDrivenPropertyCheckFailedException GeneratorDrivenPropertyCheckFailedException]]
   * when check fails.
   */
  implicit def assertingNatureOfAssertion
      : CheckerAsserting[Assertion] { type CheckResult = Assertion } = {
    new CheckerAsserting[Assertion] {
      type CheckResult = Assertion

      private[scalatest] def indicateSuccess: Assertion = Succeeded

      private[scalatest] def indicateFailure(
          messageFun: StackDepthException => String,
          undecoratedMessage: => String,
          scalaCheckArgs: List[Any],
          scalaCheckLabels: List[String],
          optionalCause: Option[Throwable],
          pos: source.Position
      ): Assertion =
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
