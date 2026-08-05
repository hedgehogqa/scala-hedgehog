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
import hedgehog.core._
import hedgehog.runner.{SeedSource, Test}
import org.scalactic.source.Position
import org.scalatest.Assertion
import org.scalatest.Assertions.succeed
import org.scalatest.exceptions.GeneratorDrivenPropertyCheckFailedException

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
trait CheckerAsserting {

  private val seedSource = SeedSource.fromEnvOrTime()
  private val seed: Seed = Seed.fromLong(seedSource.seed)

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
  def check(test: PropertyT[Result], config: PropertyConfig, pos: Position): Assertion = {
    val report = Property.check(config, test, seed)

    val rendered = Test.renderReport(
      this.getClass.getName,
      Test("todo", test),
      report,
      ansiCodesSupported = true
    )
    if (report.status != Status.ok) {
      // fail the test using scalatest
      throw new GeneratorDrivenPropertyCheckFailedException(
        _ => rendered,
        None,
        pos,
        None,
        rendered,
        Nil,
        None,
        Nil
      )
    } else {
      println(rendered)
      succeed
    }
  }
}

object CheckerAsserting extends CheckerAsserting
