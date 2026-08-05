/*
 * Copyright 2001-2026 Artima, Inc.
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
package hedgehog.scalatest

import hedgehog.Gen
import hedgehog.core.{DiscardCount, PropertyConfig}
import hegehog.scalatest.HedgehogDrivenPropertyChecks
import org.scalatest.exceptions.GeneratorDrivenPropertyCheckFailedException
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class HedgehogDrivenPropertyChecksSuite
    extends AnyFunSpec
    with Matchers
    with HedgehogDrivenPropertyChecks {

  implicit private val propertyConfig: PropertyConfig = PropertyConfig.default

  private val famousLastWords =
    Gen.element1("the", "program", "compiles", "therefore", "it", "should", "work")

  it("generator-driven property that takes 1 args, which succeeds") {
    forAll(famousLastWords) { (a: String) =>
      assert(a.length === a.length)
    }
  }

  it("generator-driven property that takes 1 args, which succeeds using matchers") {
    forAll(famousLastWords) { (a: String) =>
      a.length shouldEqual a.length
      ()
    }
  }

  it("generator-driven property that takes 1 args, which fails") {
    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll(famousLastWords) { (a: String) =>
        assert(a.length < 0)
      }
    }
  }

  it("generator-driven property that takes 1 named args, which succeeds") {
    forAll(famousLastWords.forAll.label("a")) { (a: String) =>
      assert(a.length === a.length)
    }
  }

  it("generator-driven property that takes 1 named args, which fails") {
    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll(famousLastWords.forAll.label("a")) { (a: String) =>
        assert(a.length < 0)
      }
    }
  }

  // Same thing, but set minSuccessful to 5 with param, prop fails after 5
  it(
    "generator-driven property that takes 1 args, which succeeds, with minSuccessful param set to 5"
  ) {
    implicit val propertyConfig: PropertyConfig = PropertyConfig.default.copy(testLimit = 5)
    var i = 0
    forAll(famousLastWords) { (_: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it(
    "generator-driven property that takes 1 args, which fails, with minSuccessful param set to 5"
  ) {
    implicit val propertyConfig: PropertyConfig = PropertyConfig.default.copy(testLimit = 5)
    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll(famousLastWords) { (_: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it(
    "generator-driven property that takes 1 args and generators, which fails, with maxDiscardedFactor param set to 0.6"
  ) {
    implicit val propertyConfig: PropertyConfig =
      PropertyConfig.default.copy(discardLimit = DiscardCount(1))

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll(famousLastWords.filter(_ => false)) { (_: String) =>
        succeed
      }
    }
  }

  it("generator-driven property that takes 2 args and generators, which succeeds") {
    forAll(famousLastWords, famousLastWords) { (a: String, b: String) =>
      assert(a.length + b.length === (a ++ b).length)
    }
  }

  it("generator-driven property that takes 2 args and generators, which fails") {
    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll(famousLastWords, famousLastWords) { (a: String, b: String) =>
        assert(a.length + b.length < 0)
      }
    }
  }

  it("generator-driven property that takes 2 named args and generators, which succeeds") {
    forAll(famousLastWords.forAll.label("a"), famousLastWords.forAll.label("b")) {
      (a: String, b: String) =>
        assert(a.length + b.length === (a ++ b).length)
    }
  }

  it(
    "generator-driven property that takes 2 args, which fails, with default minSuccessful param set to 5"
  ) {

    implicit val propertyConfig: PropertyConfig = PropertyConfig.default.copy(testLimit = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll(famousLastWords, famousLastWords) { (_: String, _: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it(
    "generator-driven property that takes 2 args and generators, which fails, with maxDiscardedFactor param set to 0.6"
  ) {
    implicit val propertyConfig: PropertyConfig =
      PropertyConfig.default.copy(testLimit = 5, discardLimit = DiscardCount(1))

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll(famousLastWords.filter(_ => false), famousLastWords) { (_: String, _: String) =>
        i += 1
        succeed
      }
    }
  }

  it("generator-driven property that takes 3 args and generators, which succeeds") {
    forAll(famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String) =>
      assert(a.length + b.length + c.length === (a ++ b ++ c).length)
    }
  }

  it("generator-driven property that takes 3 args and generators, which fails") {
    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll(famousLastWords, famousLastWords, famousLastWords) {
        (a: String, b: String, c: String) =>
          assert(a.length + b.length + c.length < 0)
      }
    }
  }

  it(
    "generator-driven property that takes 3 args and generators, which succeeds, with default minSuccessful param set to 5"
  ) {
    implicit val propertyConfig: PropertyConfig = PropertyConfig.default.copy(testLimit = 5)

    var i = 0
    forAll(famousLastWords, famousLastWords, famousLastWords) { (_: String, _: String, _: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it(
    "generator-driven property that takes 3 args and generators, which fails, with default minSuccessful param set to 5"
  ) {
    implicit val propertyConfig: PropertyConfig = PropertyConfig.default.copy(testLimit = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll(famousLastWords, famousLastWords, famousLastWords) {
        (_: String, _: String, _: String) =>
          i += 1
          assert(i != 5)
      }
    }
  }

  it(
    "generator-driven property that takes 3 args and generators, which fails, with default maxDiscardedFactor set to 0.6"
  ) {
    implicit val propertyConfig: PropertyConfig =
      PropertyConfig.default.copy(discardLimit = DiscardCount(1))

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll(famousLastWords.filter(_ => false), famousLastWords, famousLastWords) {
        (_: String, _: String, _: String) =>
          i += 1
          succeed
      }
    }
  }

  it("generator-driven property that takes 4 args and generators, which succeeds") {

    forAll(famousLastWords, famousLastWords, famousLastWords, famousLastWords) {
      (a: String, b: String, c: String, d: String) =>
        assert(a.length + b.length + c.length + d.length === (a ++ b ++ c ++ d).length)
    }
  }

  it("generator-driven property that takes 4 args and generators, which fails") {
    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll(famousLastWords, famousLastWords, famousLastWords, famousLastWords) {
        (a: String, b: String, c: String, d: String) =>
          assert(a.length + b.length + c.length + d.length < 0)
      }
    }
  }

  it(
    "generator-driven property that takes 4 args and generators, which succeeds, with default minSuccessful param set to 5"
  ) {
    implicit val propertyConfig: PropertyConfig = PropertyConfig.default.copy(testLimit = 5)

    var i = 0
    forAll(famousLastWords, famousLastWords, famousLastWords, famousLastWords) {
      (_: String, _: String, _: String, _: String) =>
        i += 1
        assert(i != 6)
    }
  }

  it(
    "generator-driven property that takes 4 args and generators, which fails, with default minSuccessful param set to 5"
  ) {
    implicit val propertyConfig: PropertyConfig = PropertyConfig.default.copy(testLimit = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll(famousLastWords, famousLastWords, famousLastWords, famousLastWords) {
        (_: String, _: String, _: String, _: String) =>
          i += 1
          assert(i != 5)
      }
    }
  }

  it(
    "generator-driven property that takes 4 args and generators, which fails, with default maxDiscardedFactor set to 0.6"
  ) {
    implicit val propertyConfig: PropertyConfig =
      PropertyConfig.default.copy(discardLimit = DiscardCount(1))

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll(
        famousLastWords.filter(_ => false),
        famousLastWords,
        famousLastWords,
        famousLastWords
      ) { (_: String, _: String, _: String, _: String) =>
        i += 1
        succeed
      }
    }
  }

  it("generator-driven property that takes 5 args and generators, which succeeds") {
    forAll(famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) {
      (a: String, b: String, c: String, d: String, e: String) =>
        assert(
          a.length + b.length + c.length + d.length + e.length === (a ++ b ++ c ++ d ++ e).length
        )
    }
  }

  it("generator-driven property that takes 5 args and generators, which fails") {
    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll(famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) {
        (a: String, b: String, c: String, d: String, e: String) =>
          assert(a.length + b.length + c.length + d.length + e.length < 0)
      }
    }
  }

  it(
    "generator-driven property that takes 5 args and generators, which succeeds, with default minSuccessful param set to 5"
  ) {
    implicit val propertyConfig: PropertyConfig = PropertyConfig.default.copy(testLimit = 5)

    var i = 0
    forAll(famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) {
      (_: String, _: String, _: String, _: String, _: String) =>
        i += 1
        assert(i != 6)
    }
  }

  it(
    "generator-driven property that takes 5 args and generators, which fails, with default minSuccessful param set to 5"
  ) {
    implicit val propertyConfig: PropertyConfig = PropertyConfig.default.copy(testLimit = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll(famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) {
        (_: String, _: String, _: String, _: String, _: String) =>
          i += 1
          assert(i != 5)
      }
    }
  }

  it(
    "generator-driven property that takes 5 args and generators, which fails, with default maxDiscardedFactor set to 0.6"
  ) {
    implicit val propertyConfig: PropertyConfig =
      PropertyConfig.default.copy(discardLimit = DiscardCount(1))

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll(
        famousLastWords.filter(_ => false),
        famousLastWords,
        famousLastWords,
        famousLastWords,
        famousLastWords
      ) { (_: String, _: String, _: String, _: String, _: String) =>
        i += 1
        succeed
      }
    }
  }

  it("generator-driven property that takes 6 args and generators, which succeeds") {
    forAll(
      famousLastWords,
      famousLastWords,
      famousLastWords,
      famousLastWords,
      famousLastWords,
      famousLastWords
    ) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      assert(
        a.length + b.length + c.length + d.length + e.length + f.length === (a ++ b ++ c ++ d ++ e ++ f).length
      )
    }
  }

  it("generator-driven property that takes 6 args and generators, which fails") {
    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll(
        famousLastWords,
        famousLastWords,
        famousLastWords,
        famousLastWords,
        famousLastWords,
        famousLastWords
      ) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        assert(a.length + b.length + c.length + d.length + e.length + f.length < 0)
      }
    }
  }

  it(
    "generator-driven property that takes 6 args and generators, which succeeds, with default minSuccessful param set to 5"
  ) {
    implicit val propertyConfig: PropertyConfig = PropertyConfig.default.copy(testLimit = 5)

    var i = 0
    forAll(
      famousLastWords,
      famousLastWords,
      famousLastWords,
      famousLastWords,
      famousLastWords,
      famousLastWords
    ) { (_: String, _: String, _: String, _: String, _: String, _: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it(
    "generator-driven property that takes 6 args and generators, which fails, with default minSuccessful param set to 5"
  ) {
    implicit val propertyConfig: PropertyConfig = PropertyConfig.default.copy(testLimit = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll(
        famousLastWords,
        famousLastWords,
        famousLastWords,
        famousLastWords,
        famousLastWords,
        famousLastWords
      ) { (_: String, _: String, _: String, _: String, _: String, _: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it(
    "generator-driven property that takes 6 args and generators, which fails, with default maxDiscardedFactor set to 0.6"
  ) {
    implicit val propertyConfig: PropertyConfig =
      PropertyConfig.default.copy(discardLimit = DiscardCount(1))

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll(
        famousLastWords.filter(_ => false),
        famousLastWords,
        famousLastWords,
        famousLastWords,
        famousLastWords,
        famousLastWords
      ) { (_: String, _: String, _: String, _: String, _: String, _: String) =>
        i += 1
        succeed
      }
    }
  }
}
