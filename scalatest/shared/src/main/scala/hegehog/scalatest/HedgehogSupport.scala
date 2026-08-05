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
 * A trait that provides a method to check Hedgehog properties and assert the results using
 * ScalaTest. It converts the Hedgehog property-based test into a result that ScalaTest can
 * understand.
 */
trait HedgehogSupport {

  private val seedSource = SeedSource.fromEnvOrTime()
  private val seed = Seed.fromLong(seedSource.seed)

  /**
   * Converts the Hedgehog property based test into a result scalatest can understand.
   *
   * @param property
   *   The property to be checked.
   * @param config
   *   The config to be used to check the property.
   * @param pos
   *   the <code>Position</code> of the caller site
   */
  def check(property: Property)(implicit config: PropertyConfig, pos: Position): Assertion =
    check(Test("", property))

  /**
   * Converts the Hedgehog property based test into a result scalatest can understand.
   *
   * @param test
   *   The test to be checked.
   * @param config
   *   The config to be used to check the property.
   * @param pos
   *   the <code>Position</code> of the caller site
   */
  def check(test: Test)(implicit config: PropertyConfig, pos: Position): Assertion = {
    val report = Property.check(test.withConfig(config), test.result, seed)

    // Render the report, Scalatest takes care of the test name, so we strip it from the rendered report
    val rendered = Test
      .renderReport(
        "",
        test,
        report,
        ansiCodesSupported = false
      )
      .stripPrefix("+")
      .stripPrefix("-")
      .stripPrefix(s" .${test.name}: ")
    val completeMessage = s"${seedSource.renderLog}\n$rendered"
    if (report.status != Status.ok) {
      // fail the test using scalatest
      throw new GeneratorDrivenPropertyCheckFailedException(
        _ => completeMessage,
        None,
        pos,
        None,
        completeMessage,
        Nil,
        None,
        Nil
      )
    } else {
      println(completeMessage)
      succeed
    }
  }
}

object HedgehogSupport extends HedgehogSupport
