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

import org.scalatest.exceptions.GeneratorDrivenPropertyCheckFailedException
                               
class AssertScalaCheckDrivenPropertyChecksMixinSuite extends org.scalatest.funspec.AnyFunSpec with org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks {

  val famousLastWords = for {
    s <- Gen.oneOf("the", "program", "compiles", "therefore", "it", "should", "work")
  } yield s

  val sevenEleven: Gen[String] =
    Gen.sized { (size: Int) =>
      if (size >= 7 && size <= 11)
        Gen.const("OKAY")
      else
        throw new Exception("expected 7 <= size <= 11 but got " + size)
    }

  val fiveFive: Gen[String] =
    Gen.sized { (size: Int) =>
      if (size == 5)
        Gen.const("OKAY")
      else
        throw new Exception("expected size 5 but got " + size)
    }
                                

  it("generator-driven property that takes 1 args, which succeeds") {

    forAll { (a: String) =>
      assert(a.length === ((a).length))
    }
  }

  it("generator-driven property that takes 1 args, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll { (a: String) =>
        assert(a.length < 0)
      }
    }
  }

  it("generator-driven property that takes 1 named args, which succeeds") {

    forAll ("a") { (a: String) =>
      assert(a.length === ((a).length))
    }
  }

  it("generator-driven property that takes 1 named args, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ("a") { (a: String) =>
        assert(a.length < 0)
      }
    }
  }

  it("generator-driven property that takes 1 args and generators, which succeeds") {

    forAll (famousLastWords) { (a: String) =>
      assert(a.length === ((a).length))
    }
  }

  it("generator-driven property that takes 1 args and generators, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll (famousLastWords) { (a: String) =>
        assert(a.length < 0)
      }
    }
  }

  it("generator-driven property that takes 1 named args and generators, which succeeds") {

    forAll ((famousLastWords, "a")) { (a: String) =>
      assert(a.length === ((a).length))
    }
  }

  it("generator-driven property that takes 1 named args and generators, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ((famousLastWords, "a")) { (a: String) =>
        assert(a.length < 0)
      }
    }
  }

  // Same thing, but with config params
  it("generator-driven property that takes 1 args, which succeeds, with config params") {

    forAll (minSize(10), sizeRange(10)) { (a: String) =>
      assert(a.length === ((a).length))
    }
  }

  it("generator-driven property that takes 1 args, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll (minSize(10), sizeRange(10)) { (a: String) =>
        assert(a.length < 0)
      }
    }
  }

  it("generator-driven property that takes 1 named args, which succeeds, with config params") {

    forAll ("a", minSize(10), sizeRange(10)) { (a: String) =>
      assert(a.length === ((a).length))
    }
  }

  it("generator-driven property that takes 1 named args, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ("a", minSize(10), sizeRange(10)) { (a: String) =>
        assert(a.length < 0)
      }
    }
  }

  it("generator-driven property that takes 1 args and generators, which succeeds, with config params") {

    forAll (famousLastWords, minSize(10), sizeRange(10)) { (a: String) =>
      assert(a.length === ((a).length))
    }
  }

  it("generator-driven property that takes 1 args and generators, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll (famousLastWords, minSize(10), sizeRange(10)) { (a: String) =>
        assert(a.length < 0)
      }
    }
  }

  it("generator-driven property that takes 1 named args and generators, which succeeds, with config params") {

    forAll ((famousLastWords, "a"), minSize(10), sizeRange(10)) { (a: String) =>
      assert(a.length === ((a).length))
    }
  }

  it("generator-driven property that takes 1 named args and generators, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ((famousLastWords, "a"), minSize(10), sizeRange(10)) { (a: String) =>
        assert(a.length < 0)
      }
    }
  }

  // Same thing, but set minSuccessful to 5 with param, prop fails after 5
  it("generator-driven property that takes 1 args, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    forAll (minSuccessful(5)) { (a: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 1 args, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (minSuccessful(5)) { (a: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 1 named args, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    forAll ("a", minSuccessful(5)) { (a: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 1 named args, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ("a", minSuccessful(5)) { (a: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 1 args and generators, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    forAll (famousLastWords, minSuccessful(5)) { (a: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 1 args and generators, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (famousLastWords, minSuccessful(5)) { (a: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 1 named args and generators, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    forAll ((famousLastWords, "a"), minSuccessful(5)) { (a: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 1 named args and generators, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ((famousLastWords, "a"), minSuccessful(5)) { (a: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  // Same thing, but set default minSuccessful to 5, prop fails after 5
  it("generator-driven property that takes 1 args, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    forAll { (a: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 1 args, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll { (a: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 1 named args, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    forAll ("a") { (a: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 1 named args, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ("a") { (a: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 1 args and generators, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    forAll (famousLastWords) { (a: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 1 args and generators, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (famousLastWords) { (a: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 1 named args and generators, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    forAll ((famousLastWords, "a")) { (a: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 1 named args and generators, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ((famousLastWords, "a")) { (a: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  // Same thing, but set maxDiscardedFactor to 0.6 with param, prop fails after 5
  it("generator-driven property that takes 1 args, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    forAll (maxDiscardedFactor(0.6)) { (a: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 1 args, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (maxDiscardedFactor(0.6)) { (a: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 1 named args, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    forAll ("a", maxDiscardedFactor(0.6)) { (a: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 1 named args, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ("a", maxDiscardedFactor(0.6)) { (a: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 1 args and generators, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    forAll (famousLastWords, maxDiscardedFactor(0.6)) { (a: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 1 args and generators, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (famousLastWords, maxDiscardedFactor(0.6)) { (a: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 1 named args and generators, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    forAll ((famousLastWords, "a"), maxDiscardedFactor(0.6)) { (a: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 1 named args and generators, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ((famousLastWords, "a"), maxDiscardedFactor(0.6)) { (a: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  // Same thing, but set default maxDiscardedFactor to 0.6, prop fails after 5
  it("generator-driven property that takes 1 args, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    forAll { (a: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 1 args, which fails, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll { (a: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 1 named args, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    forAll ("a") { (a: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 1 named args, which fails, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ("a") { (a: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 1 args and generators, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    forAll (famousLastWords) { (a: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 1 args and generators, which fails, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (famousLastWords) { (a: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 1 named args and generators, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    forAll ((famousLastWords, "a")) { (a: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 1 named args and generators, which fails, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ((famousLastWords, "a")) { (a: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  // set sizeRange with param 
  it("generator-driven property that takes 1 args, with sizeRange specified as param") {

    forAll (sizeRange(5)) { (a: String) =>
      assert(a.length <= 5)
    }
  }

  it("generator-driven property that takes 1 named args, with sizeRange specified as param") {

    forAll ("a", sizeRange(5)) { (a: String) =>
      assert(a.length <= 5)
    }
  }

  // set sizeRange with default (ensure always passed with a size less than sizeRange)
  it("generator-driven property that takes 1 args, with sizeRange specified as default") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 5)

    forAll { (a: String) =>
      assert(a.length <= 5)
    }
  }

  it("generator-driven property that takes 1 named args, with sizeRange specified as default") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 5)

    forAll ("a") { (a: String) =>
      assert(a.length <= 5)
    }
  }

  // set sizeRange == 0 with (param, param)
  it("generator-driven property that takes 1 args and generators, with sizeRange == 0, specified as (param, param)") {

    forAll (fiveFive, minSize(5), sizeRange(0)) { (a: String) =>
        assert(a === ("OKAY"))
    }
  }

  it("generator-driven property that takes 1 named args and generators, with sizeRange == 0, specified as (param, param)") {

    forAll ((fiveFive, "a"), minSize(5), sizeRange(0)) { (a: String) =>
        assert(a === ("OKAY"))
    }
  }

  // set sizeRange == 0 with (param, default) (ensure always passed with that size)
  it("generator-driven property that takes 1 args and generators, with sizeRange == 0, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 0)

    forAll (fiveFive, minSize(5)) { (a: String) =>
        assert(a === ("OKAY"))
    }
  }

  it("generator-driven property that takes 1 named args and generators, with sizeRange == 0, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 0)

    forAll ((fiveFive, "a"), minSize(5)) { (a: String) =>
        assert(a === ("OKAY"))
    }
  }

  // set sizeRange == 0 with (default, param) (ensure always passed with that size)
  it("generator-driven property that takes 1 args and generators, with sizeRange == 0, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5)

    forAll (fiveFive, sizeRange(0)) { (a: String) =>
        assert(a === ("OKAY"))
    }
  }

  it("generator-driven property that takes 1 named args and generators, with sizeRange == 0, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5)

    forAll ((fiveFive, "a"), sizeRange(0)) { (a: String) =>
        assert(a === ("OKAY"))
    }
  }

  // set sizeRange == 0 with (default, default) (ensure always passed with that size)
  it("generator-driven property that takes 1 args and generators, with sizeRange == 0, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5, sizeRange = 0)

    forAll (fiveFive) { (a: String) =>
        assert(a === ("OKAY"))
    }
  }

  it("generator-driven property that takes 1 named args and generators, with sizeRange == 0, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5, sizeRange = 0)

    forAll ((fiveFive, "a")) { (a: String) =>
        assert(a === ("OKAY"))
    }
  }

  // set minSize to 7 and sizeRange to 4 with (param, param) (ensure always passed with that size)
  it("generator-driven property that takes 1 args and generators, with minSize to 7 and sizeRange to 4, specified as (param, param)") {

    forAll (sevenEleven, minSize(7), sizeRange(4)) { (a: String) =>
        assert(a === ("OKAY"))
    }
  }

  it("generator-driven property that takes 1 named args and generators, with minSize to 7 and sizeRange to 4, specified as (param, param)") {

    forAll ((sevenEleven, "a"), minSize(7), sizeRange(4)) { (a: String) =>
        assert(a === ("OKAY"))
    }
  }

  // set minSize to 7 and sizeRange to 4 with (param, default) (ensure always passed with that size)
  it("generator-driven property that takes 1 args and generators, with minSize to 7 and sizeRange to 4, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 4)

    forAll (sevenEleven, minSize(7)) { (a: String) =>
        assert(a === ("OKAY"))
    }
  }

  it("generator-driven property that takes 1 named args and generators, with minSize to 7 and sizeRange to 4, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 4)

    forAll ((sevenEleven, "a"), minSize(7)) { (a: String) =>
        assert(a === ("OKAY"))
    }
  }

  // set minSize to 7 and sizeRange to 4 with (default, param) (ensure always passed with that size)
  it("generator-driven property that takes 1 args and generators, with minSize to 7 and sizeRange to 4, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7)

    forAll (sevenEleven, sizeRange(4)) { (a: String) =>
        assert(a === ("OKAY"))
    }
  }

  it("generator-driven property that takes 1 named args and generators, with minSize to 7 and sizeRange to 4, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7)

    forAll ((sevenEleven, "a"), sizeRange(4)) { (a: String) =>
        assert(a === ("OKAY"))
    }
  }

  // set minSize to 7 and sizeRange to 4 with (default, default) (ensure always passed with that size)
  it("generator-driven property that takes 1 args and generators, with minSize to 7 and sizeRange to 4, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7, sizeRange = 4)

    forAll (sevenEleven) { (a: String) =>
        assert(a === ("OKAY"))
    }
  }

  it("generator-driven property that takes 1 named args and generators, with minSize to 7 and sizeRange to 4, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7, sizeRange = 4)

    forAll ((sevenEleven, "a")) { (a: String) =>
        assert(a === ("OKAY"))
    }
  }
                               

  it("generator-driven property that takes 2 args, which succeeds") {

    forAll { (a: String, b: String) =>
      assert(a.length + b.length === ((a + b).length))
    }
  }

  it("generator-driven property that takes 2 args, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll { (a: String, b: String) =>
        assert(a.length + b.length < 0)
      }
    }
  }

  it("generator-driven property that takes 2 named args, which succeeds") {

    forAll ("a", "b") { (a: String, b: String) =>
      assert(a.length + b.length === ((a + b).length))
    }
  }

  it("generator-driven property that takes 2 named args, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ("a", "b") { (a: String, b: String) =>
        assert(a.length + b.length < 0)
      }
    }
  }

  it("generator-driven property that takes 2 args and generators, which succeeds") {

    forAll (famousLastWords, famousLastWords) { (a: String, b: String) =>
      assert(a.length + b.length === ((a + b).length))
    }
  }

  it("generator-driven property that takes 2 args and generators, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll (famousLastWords, famousLastWords) { (a: String, b: String) =>
        assert(a.length + b.length < 0)
      }
    }
  }

  it("generator-driven property that takes 2 named args and generators, which succeeds") {

    forAll ((famousLastWords, "a"), (famousLastWords, "b")) { (a: String, b: String) =>
      assert(a.length + b.length === ((a + b).length))
    }
  }

  it("generator-driven property that takes 2 named args and generators, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ((famousLastWords, "a"), (famousLastWords, "b")) { (a: String, b: String) =>
        assert(a.length + b.length < 0)
      }
    }
  }

  // Same thing, but with config params
  it("generator-driven property that takes 2 args, which succeeds, with config params") {

    forAll (minSize(10), sizeRange(10)) { (a: String, b: String) =>
      assert(a.length + b.length === ((a + b).length))
    }
  }

  it("generator-driven property that takes 2 args, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll (minSize(10), sizeRange(10)) { (a: String, b: String) =>
        assert(a.length + b.length < 0)
      }
    }
  }

  it("generator-driven property that takes 2 named args, which succeeds, with config params") {

    forAll ("a", "b", minSize(10), sizeRange(10)) { (a: String, b: String) =>
      assert(a.length + b.length === ((a + b).length))
    }
  }

  it("generator-driven property that takes 2 named args, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ("a", "b", minSize(10), sizeRange(10)) { (a: String, b: String) =>
        assert(a.length + b.length < 0)
      }
    }
  }

  it("generator-driven property that takes 2 args and generators, which succeeds, with config params") {

    forAll (famousLastWords, famousLastWords, minSize(10), sizeRange(10)) { (a: String, b: String) =>
      assert(a.length + b.length === ((a + b).length))
    }
  }

  it("generator-driven property that takes 2 args and generators, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll (famousLastWords, famousLastWords, minSize(10), sizeRange(10)) { (a: String, b: String) =>
        assert(a.length + b.length < 0)
      }
    }
  }

  it("generator-driven property that takes 2 named args and generators, which succeeds, with config params") {

    forAll ((famousLastWords, "a"), (famousLastWords, "b"), minSize(10), sizeRange(10)) { (a: String, b: String) =>
      assert(a.length + b.length === ((a + b).length))
    }
  }

  it("generator-driven property that takes 2 named args and generators, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ((famousLastWords, "a"), (famousLastWords, "b"), minSize(10), sizeRange(10)) { (a: String, b: String) =>
        assert(a.length + b.length < 0)
      }
    }
  }

  // Same thing, but set minSuccessful to 5 with param, prop fails after 5
  it("generator-driven property that takes 2 args, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    forAll (minSuccessful(5)) { (a: String, b: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 2 args, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (minSuccessful(5)) { (a: String, b: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 2 named args, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    forAll ("a", "b", minSuccessful(5)) { (a: String, b: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 2 named args, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ("a", "b", minSuccessful(5)) { (a: String, b: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 2 args and generators, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    forAll (famousLastWords, famousLastWords, minSuccessful(5)) { (a: String, b: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 2 args and generators, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (famousLastWords, famousLastWords, minSuccessful(5)) { (a: String, b: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 2 named args and generators, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    forAll ((famousLastWords, "a"), (famousLastWords, "b"), minSuccessful(5)) { (a: String, b: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 2 named args and generators, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ((famousLastWords, "a"), (famousLastWords, "b"), minSuccessful(5)) { (a: String, b: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  // Same thing, but set default minSuccessful to 5, prop fails after 5
  it("generator-driven property that takes 2 args, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    forAll { (a: String, b: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 2 args, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll { (a: String, b: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 2 named args, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    forAll ("a", "b") { (a: String, b: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 2 named args, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ("a", "b") { (a: String, b: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 2 args and generators, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    forAll (famousLastWords, famousLastWords) { (a: String, b: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 2 args and generators, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (famousLastWords, famousLastWords) { (a: String, b: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 2 named args and generators, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    forAll ((famousLastWords, "a"), (famousLastWords, "b")) { (a: String, b: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 2 named args and generators, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ((famousLastWords, "a"), (famousLastWords, "b")) { (a: String, b: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  // Same thing, but set maxDiscardedFactor to 0.6 with param, prop fails after 5
  it("generator-driven property that takes 2 args, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    forAll (maxDiscardedFactor(0.6)) { (a: String, b: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 2 args, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (maxDiscardedFactor(0.6)) { (a: String, b: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 2 named args, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    forAll ("a", "b", maxDiscardedFactor(0.6)) { (a: String, b: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 2 named args, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ("a", "b", maxDiscardedFactor(0.6)) { (a: String, b: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 2 args and generators, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    forAll (famousLastWords, famousLastWords, maxDiscardedFactor(0.6)) { (a: String, b: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 2 args and generators, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (famousLastWords, famousLastWords, maxDiscardedFactor(0.6)) { (a: String, b: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 2 named args and generators, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    forAll ((famousLastWords, "a"), (famousLastWords, "b"), maxDiscardedFactor(0.6)) { (a: String, b: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 2 named args and generators, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ((famousLastWords, "a"), (famousLastWords, "b"), maxDiscardedFactor(0.6)) { (a: String, b: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  // Same thing, but set default maxDiscardedFactor to 0.6, prop fails after 5
  it("generator-driven property that takes 2 args, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    forAll { (a: String, b: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 2 args, which fails, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll { (a: String, b: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 2 named args, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    forAll ("a", "b") { (a: String, b: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 2 named args, which fails, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ("a", "b") { (a: String, b: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 2 args and generators, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    forAll (famousLastWords, famousLastWords) { (a: String, b: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 2 args and generators, which fails, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (famousLastWords, famousLastWords) { (a: String, b: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 2 named args and generators, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    forAll ((famousLastWords, "a"), (famousLastWords, "b")) { (a: String, b: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 2 named args and generators, which fails, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ((famousLastWords, "a"), (famousLastWords, "b")) { (a: String, b: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  // set sizeRange with param 
  it("generator-driven property that takes 2 args, with sizeRange specified as param") {

    forAll (sizeRange(5)) { (a: String, b: String) =>
      assert(a.length <= 5)
      assert(b.length <= 5)
    }
  }

  it("generator-driven property that takes 2 named args, with sizeRange specified as param") {

    forAll ("a", "b", sizeRange(5)) { (a: String, b: String) =>
      assert(a.length <= 5)
      assert(b.length <= 5)
    }
  }

  // set sizeRange with default (ensure always passed with a size less than sizeRange)
  it("generator-driven property that takes 2 args, with sizeRange specified as default") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 5)

    forAll { (a: String, b: String) =>
      assert(a.length <= 5)
      assert(b.length <= 5)
    }
  }

  it("generator-driven property that takes 2 named args, with sizeRange specified as default") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 5)

    forAll ("a", "b") { (a: String, b: String) =>
      assert(a.length <= 5)
      assert(b.length <= 5)
    }
  }

  // set sizeRange == 0 with (param, param)
  it("generator-driven property that takes 2 args and generators, with sizeRange == 0, specified as (param, param)") {

    forAll (fiveFive, fiveFive, minSize(5), sizeRange(0)) { (a: String, b: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
    }
  }

  it("generator-driven property that takes 2 named args and generators, with sizeRange == 0, specified as (param, param)") {

    forAll ((fiveFive, "a"), (fiveFive, "b"), minSize(5), sizeRange(0)) { (a: String, b: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
    }
  }

  // set sizeRange == 0 with (param, default) (ensure always passed with that size)
  it("generator-driven property that takes 2 args and generators, with sizeRange == 0, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 0)

    forAll (fiveFive, fiveFive, minSize(5)) { (a: String, b: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
    }
  }

  it("generator-driven property that takes 2 named args and generators, with sizeRange == 0, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 0)

    forAll ((fiveFive, "a"), (fiveFive, "b"), minSize(5)) { (a: String, b: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
    }
  }

  // set sizeRange == 0 with (default, param) (ensure always passed with that size)
  it("generator-driven property that takes 2 args and generators, with sizeRange == 0, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5)

    forAll (fiveFive, fiveFive, sizeRange(0)) { (a: String, b: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
    }
  }

  it("generator-driven property that takes 2 named args and generators, with sizeRange == 0, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5)

    forAll ((fiveFive, "a"), (fiveFive, "b"), sizeRange(0)) { (a: String, b: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
    }
  }

  // set sizeRange == 0 with (default, default) (ensure always passed with that size)
  it("generator-driven property that takes 2 args and generators, with sizeRange == 0, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5, sizeRange = 0)

    forAll (fiveFive, fiveFive) { (a: String, b: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
    }
  }

  it("generator-driven property that takes 2 named args and generators, with sizeRange == 0, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5, sizeRange = 0)

    forAll ((fiveFive, "a"), (fiveFive, "b")) { (a: String, b: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
    }
  }

  // set minSize to 7 and sizeRange to 4 with (param, param) (ensure always passed with that size)
  it("generator-driven property that takes 2 args and generators, with minSize to 7 and sizeRange to 4, specified as (param, param)") {

    forAll (sevenEleven, sevenEleven, minSize(7), sizeRange(4)) { (a: String, b: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
    }
  }

  it("generator-driven property that takes 2 named args and generators, with minSize to 7 and sizeRange to 4, specified as (param, param)") {

    forAll ((sevenEleven, "a"), (sevenEleven, "b"), minSize(7), sizeRange(4)) { (a: String, b: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
    }
  }

  // set minSize to 7 and sizeRange to 4 with (param, default) (ensure always passed with that size)
  it("generator-driven property that takes 2 args and generators, with minSize to 7 and sizeRange to 4, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 4)

    forAll (sevenEleven, sevenEleven, minSize(7)) { (a: String, b: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
    }
  }

  it("generator-driven property that takes 2 named args and generators, with minSize to 7 and sizeRange to 4, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 4)

    forAll ((sevenEleven, "a"), (sevenEleven, "b"), minSize(7)) { (a: String, b: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
    }
  }

  // set minSize to 7 and sizeRange to 4 with (default, param) (ensure always passed with that size)
  it("generator-driven property that takes 2 args and generators, with minSize to 7 and sizeRange to 4, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7)

    forAll (sevenEleven, sevenEleven, sizeRange(4)) { (a: String, b: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
    }
  }

  it("generator-driven property that takes 2 named args and generators, with minSize to 7 and sizeRange to 4, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7)

    forAll ((sevenEleven, "a"), (sevenEleven, "b"), sizeRange(4)) { (a: String, b: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
    }
  }

  // set minSize to 7 and sizeRange to 4 with (default, default) (ensure always passed with that size)
  it("generator-driven property that takes 2 args and generators, with minSize to 7 and sizeRange to 4, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7, sizeRange = 4)

    forAll (sevenEleven, sevenEleven) { (a: String, b: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
    }
  }

  it("generator-driven property that takes 2 named args and generators, with minSize to 7 and sizeRange to 4, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7, sizeRange = 4)

    forAll ((sevenEleven, "a"), (sevenEleven, "b")) { (a: String, b: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
    }
  }
                               

  it("generator-driven property that takes 3 args, which succeeds") {

    forAll { (a: String, b: String, c: String) =>
      assert(a.length + b.length + c.length === ((a + b + c).length))
    }
  }

  it("generator-driven property that takes 3 args, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll { (a: String, b: String, c: String) =>
        assert(a.length + b.length + c.length < 0)
      }
    }
  }

  it("generator-driven property that takes 3 named args, which succeeds") {

    forAll ("a", "b", "c") { (a: String, b: String, c: String) =>
      assert(a.length + b.length + c.length === ((a + b + c).length))
    }
  }

  it("generator-driven property that takes 3 named args, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ("a", "b", "c") { (a: String, b: String, c: String) =>
        assert(a.length + b.length + c.length < 0)
      }
    }
  }

  it("generator-driven property that takes 3 args and generators, which succeeds") {

    forAll (famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String) =>
      assert(a.length + b.length + c.length === ((a + b + c).length))
    }
  }

  it("generator-driven property that takes 3 args and generators, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll (famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String) =>
        assert(a.length + b.length + c.length < 0)
      }
    }
  }

  it("generator-driven property that takes 3 named args and generators, which succeeds") {

    forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c")) { (a: String, b: String, c: String) =>
      assert(a.length + b.length + c.length === ((a + b + c).length))
    }
  }

  it("generator-driven property that takes 3 named args and generators, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c")) { (a: String, b: String, c: String) =>
        assert(a.length + b.length + c.length < 0)
      }
    }
  }

  // Same thing, but with config params
  it("generator-driven property that takes 3 args, which succeeds, with config params") {

    forAll (minSize(10), sizeRange(10)) { (a: String, b: String, c: String) =>
      assert(a.length + b.length + c.length === ((a + b + c).length))
    }
  }

  it("generator-driven property that takes 3 args, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll (minSize(10), sizeRange(10)) { (a: String, b: String, c: String) =>
        assert(a.length + b.length + c.length < 0)
      }
    }
  }

  it("generator-driven property that takes 3 named args, which succeeds, with config params") {

    forAll ("a", "b", "c", minSize(10), sizeRange(10)) { (a: String, b: String, c: String) =>
      assert(a.length + b.length + c.length === ((a + b + c).length))
    }
  }

  it("generator-driven property that takes 3 named args, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ("a", "b", "c", minSize(10), sizeRange(10)) { (a: String, b: String, c: String) =>
        assert(a.length + b.length + c.length < 0)
      }
    }
  }

  it("generator-driven property that takes 3 args and generators, which succeeds, with config params") {

    forAll (famousLastWords, famousLastWords, famousLastWords, minSize(10), sizeRange(10)) { (a: String, b: String, c: String) =>
      assert(a.length + b.length + c.length === ((a + b + c).length))
    }
  }

  it("generator-driven property that takes 3 args and generators, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll (famousLastWords, famousLastWords, famousLastWords, minSize(10), sizeRange(10)) { (a: String, b: String, c: String) =>
        assert(a.length + b.length + c.length < 0)
      }
    }
  }

  it("generator-driven property that takes 3 named args and generators, which succeeds, with config params") {

    forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), minSize(10), sizeRange(10)) { (a: String, b: String, c: String) =>
      assert(a.length + b.length + c.length === ((a + b + c).length))
    }
  }

  it("generator-driven property that takes 3 named args and generators, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), minSize(10), sizeRange(10)) { (a: String, b: String, c: String) =>
        assert(a.length + b.length + c.length < 0)
      }
    }
  }

  // Same thing, but set minSuccessful to 5 with param, prop fails after 5
  it("generator-driven property that takes 3 args, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    forAll (minSuccessful(5)) { (a: String, b: String, c: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 3 args, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (minSuccessful(5)) { (a: String, b: String, c: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 3 named args, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    forAll ("a", "b", "c", minSuccessful(5)) { (a: String, b: String, c: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 3 named args, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ("a", "b", "c", minSuccessful(5)) { (a: String, b: String, c: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 3 args and generators, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    forAll (famousLastWords, famousLastWords, famousLastWords, minSuccessful(5)) { (a: String, b: String, c: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 3 args and generators, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (famousLastWords, famousLastWords, famousLastWords, minSuccessful(5)) { (a: String, b: String, c: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 3 named args and generators, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), minSuccessful(5)) { (a: String, b: String, c: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 3 named args and generators, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), minSuccessful(5)) { (a: String, b: String, c: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  // Same thing, but set default minSuccessful to 5, prop fails after 5
  it("generator-driven property that takes 3 args, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    forAll { (a: String, b: String, c: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 3 args, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll { (a: String, b: String, c: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 3 named args, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    forAll ("a", "b", "c") { (a: String, b: String, c: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 3 named args, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ("a", "b", "c") { (a: String, b: String, c: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 3 args and generators, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    forAll (famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 3 args and generators, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 3 named args and generators, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c")) { (a: String, b: String, c: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 3 named args and generators, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c")) { (a: String, b: String, c: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  // Same thing, but set maxDiscardedFactor to 0.6 with param, prop fails after 5
  it("generator-driven property that takes 3 args, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    forAll (maxDiscardedFactor(0.6)) { (a: String, b: String, c: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 3 args, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (maxDiscardedFactor(0.6)) { (a: String, b: String, c: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 3 named args, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    forAll ("a", "b", "c", maxDiscardedFactor(0.6)) { (a: String, b: String, c: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 3 named args, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ("a", "b", "c", maxDiscardedFactor(0.6)) { (a: String, b: String, c: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 3 args and generators, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    forAll (famousLastWords, famousLastWords, famousLastWords, maxDiscardedFactor(0.6)) { (a: String, b: String, c: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 3 args and generators, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (famousLastWords, famousLastWords, famousLastWords, maxDiscardedFactor(0.6)) { (a: String, b: String, c: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 3 named args and generators, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), maxDiscardedFactor(0.6)) { (a: String, b: String, c: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 3 named args and generators, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), maxDiscardedFactor(0.6)) { (a: String, b: String, c: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  // Same thing, but set default maxDiscardedFactor to 0.6, prop fails after 5
  it("generator-driven property that takes 3 args, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    forAll { (a: String, b: String, c: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 3 args, which fails, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll { (a: String, b: String, c: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 3 named args, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    forAll ("a", "b", "c") { (a: String, b: String, c: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 3 named args, which fails, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ("a", "b", "c") { (a: String, b: String, c: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 3 args and generators, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    forAll (famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 3 args and generators, which fails, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 3 named args and generators, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c")) { (a: String, b: String, c: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 3 named args and generators, which fails, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c")) { (a: String, b: String, c: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  // set sizeRange with param 
  it("generator-driven property that takes 3 args, with sizeRange specified as param") {

    forAll (sizeRange(5)) { (a: String, b: String, c: String) =>
      assert(a.length <= 5)
      assert(b.length <= 5)
      assert(c.length <= 5)
    }
  }

  it("generator-driven property that takes 3 named args, with sizeRange specified as param") {

    forAll ("a", "b", "c", sizeRange(5)) { (a: String, b: String, c: String) =>
      assert(a.length <= 5)
      assert(b.length <= 5)
      assert(c.length <= 5)
    }
  }

  // set sizeRange with default (ensure always passed with a size less than sizeRange)
  it("generator-driven property that takes 3 args, with sizeRange specified as default") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 5)

    forAll { (a: String, b: String, c: String) =>
      assert(a.length <= 5)
      assert(b.length <= 5)
      assert(c.length <= 5)
    }
  }

  it("generator-driven property that takes 3 named args, with sizeRange specified as default") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 5)

    forAll ("a", "b", "c") { (a: String, b: String, c: String) =>
      assert(a.length <= 5)
      assert(b.length <= 5)
      assert(c.length <= 5)
    }
  }

  // set sizeRange == 0 with (param, param)
  it("generator-driven property that takes 3 args and generators, with sizeRange == 0, specified as (param, param)") {

    forAll (fiveFive, fiveFive, fiveFive, minSize(5), sizeRange(0)) { (a: String, b: String, c: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
    }
  }

  it("generator-driven property that takes 3 named args and generators, with sizeRange == 0, specified as (param, param)") {

    forAll ((fiveFive, "a"), (fiveFive, "b"), (fiveFive, "c"), minSize(5), sizeRange(0)) { (a: String, b: String, c: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
    }
  }

  // set sizeRange == 0 with (param, default) (ensure always passed with that size)
  it("generator-driven property that takes 3 args and generators, with sizeRange == 0, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 0)

    forAll (fiveFive, fiveFive, fiveFive, minSize(5)) { (a: String, b: String, c: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
    }
  }

  it("generator-driven property that takes 3 named args and generators, with sizeRange == 0, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 0)

    forAll ((fiveFive, "a"), (fiveFive, "b"), (fiveFive, "c"), minSize(5)) { (a: String, b: String, c: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
    }
  }

  // set sizeRange == 0 with (default, param) (ensure always passed with that size)
  it("generator-driven property that takes 3 args and generators, with sizeRange == 0, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5)

    forAll (fiveFive, fiveFive, fiveFive, sizeRange(0)) { (a: String, b: String, c: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
    }
  }

  it("generator-driven property that takes 3 named args and generators, with sizeRange == 0, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5)

    forAll ((fiveFive, "a"), (fiveFive, "b"), (fiveFive, "c"), sizeRange(0)) { (a: String, b: String, c: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
    }
  }

  // set sizeRange == 0 with (default, default) (ensure always passed with that size)
  it("generator-driven property that takes 3 args and generators, with sizeRange == 0, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5, sizeRange = 0)

    forAll (fiveFive, fiveFive, fiveFive) { (a: String, b: String, c: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
    }
  }

  it("generator-driven property that takes 3 named args and generators, with sizeRange == 0, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5, sizeRange = 0)

    forAll ((fiveFive, "a"), (fiveFive, "b"), (fiveFive, "c")) { (a: String, b: String, c: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
    }
  }

  // set minSize to 7 and sizeRange to 4 with (param, param) (ensure always passed with that size)
  it("generator-driven property that takes 3 args and generators, with minSize to 7 and sizeRange to 4, specified as (param, param)") {

    forAll (sevenEleven, sevenEleven, sevenEleven, minSize(7), sizeRange(4)) { (a: String, b: String, c: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
    }
  }

  it("generator-driven property that takes 3 named args and generators, with minSize to 7 and sizeRange to 4, specified as (param, param)") {

    forAll ((sevenEleven, "a"), (sevenEleven, "b"), (sevenEleven, "c"), minSize(7), sizeRange(4)) { (a: String, b: String, c: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
    }
  }

  // set minSize to 7 and sizeRange to 4 with (param, default) (ensure always passed with that size)
  it("generator-driven property that takes 3 args and generators, with minSize to 7 and sizeRange to 4, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 4)

    forAll (sevenEleven, sevenEleven, sevenEleven, minSize(7)) { (a: String, b: String, c: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
    }
  }

  it("generator-driven property that takes 3 named args and generators, with minSize to 7 and sizeRange to 4, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 4)

    forAll ((sevenEleven, "a"), (sevenEleven, "b"), (sevenEleven, "c"), minSize(7)) { (a: String, b: String, c: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
    }
  }

  // set minSize to 7 and sizeRange to 4 with (default, param) (ensure always passed with that size)
  it("generator-driven property that takes 3 args and generators, with minSize to 7 and sizeRange to 4, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7)

    forAll (sevenEleven, sevenEleven, sevenEleven, sizeRange(4)) { (a: String, b: String, c: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
    }
  }

  it("generator-driven property that takes 3 named args and generators, with minSize to 7 and sizeRange to 4, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7)

    forAll ((sevenEleven, "a"), (sevenEleven, "b"), (sevenEleven, "c"), sizeRange(4)) { (a: String, b: String, c: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
    }
  }

  // set minSize to 7 and sizeRange to 4 with (default, default) (ensure always passed with that size)
  it("generator-driven property that takes 3 args and generators, with minSize to 7 and sizeRange to 4, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7, sizeRange = 4)

    forAll (sevenEleven, sevenEleven, sevenEleven) { (a: String, b: String, c: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
    }
  }

  it("generator-driven property that takes 3 named args and generators, with minSize to 7 and sizeRange to 4, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7, sizeRange = 4)

    forAll ((sevenEleven, "a"), (sevenEleven, "b"), (sevenEleven, "c")) { (a: String, b: String, c: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
    }
  }
                               

  it("generator-driven property that takes 4 args, which succeeds") {

    forAll { (a: String, b: String, c: String, d: String) =>
      assert(a.length + b.length + c.length + d.length === ((a + b + c + d).length))
    }
  }

  it("generator-driven property that takes 4 args, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll { (a: String, b: String, c: String, d: String) =>
        assert(a.length + b.length + c.length + d.length < 0)
      }
    }
  }

  it("generator-driven property that takes 4 named args, which succeeds") {

    forAll ("a", "b", "c", "d") { (a: String, b: String, c: String, d: String) =>
      assert(a.length + b.length + c.length + d.length === ((a + b + c + d).length))
    }
  }

  it("generator-driven property that takes 4 named args, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ("a", "b", "c", "d") { (a: String, b: String, c: String, d: String) =>
        assert(a.length + b.length + c.length + d.length < 0)
      }
    }
  }

  it("generator-driven property that takes 4 args and generators, which succeeds") {

    forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String) =>
      assert(a.length + b.length + c.length + d.length === ((a + b + c + d).length))
    }
  }

  it("generator-driven property that takes 4 args and generators, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String) =>
        assert(a.length + b.length + c.length + d.length < 0)
      }
    }
  }

  it("generator-driven property that takes 4 named args and generators, which succeeds") {

    forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d")) { (a: String, b: String, c: String, d: String) =>
      assert(a.length + b.length + c.length + d.length === ((a + b + c + d).length))
    }
  }

  it("generator-driven property that takes 4 named args and generators, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d")) { (a: String, b: String, c: String, d: String) =>
        assert(a.length + b.length + c.length + d.length < 0)
      }
    }
  }

  // Same thing, but with config params
  it("generator-driven property that takes 4 args, which succeeds, with config params") {

    forAll (minSize(10), sizeRange(10)) { (a: String, b: String, c: String, d: String) =>
      assert(a.length + b.length + c.length + d.length === ((a + b + c + d).length))
    }
  }

  it("generator-driven property that takes 4 args, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll (minSize(10), sizeRange(10)) { (a: String, b: String, c: String, d: String) =>
        assert(a.length + b.length + c.length + d.length < 0)
      }
    }
  }

  it("generator-driven property that takes 4 named args, which succeeds, with config params") {

    forAll ("a", "b", "c", "d", minSize(10), sizeRange(10)) { (a: String, b: String, c: String, d: String) =>
      assert(a.length + b.length + c.length + d.length === ((a + b + c + d).length))
    }
  }

  it("generator-driven property that takes 4 named args, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ("a", "b", "c", "d", minSize(10), sizeRange(10)) { (a: String, b: String, c: String, d: String) =>
        assert(a.length + b.length + c.length + d.length < 0)
      }
    }
  }

  it("generator-driven property that takes 4 args and generators, which succeeds, with config params") {

    forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, minSize(10), sizeRange(10)) { (a: String, b: String, c: String, d: String) =>
      assert(a.length + b.length + c.length + d.length === ((a + b + c + d).length))
    }
  }

  it("generator-driven property that takes 4 args and generators, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, minSize(10), sizeRange(10)) { (a: String, b: String, c: String, d: String) =>
        assert(a.length + b.length + c.length + d.length < 0)
      }
    }
  }

  it("generator-driven property that takes 4 named args and generators, which succeeds, with config params") {

    forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), minSize(10), sizeRange(10)) { (a: String, b: String, c: String, d: String) =>
      assert(a.length + b.length + c.length + d.length === ((a + b + c + d).length))
    }
  }

  it("generator-driven property that takes 4 named args and generators, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), minSize(10), sizeRange(10)) { (a: String, b: String, c: String, d: String) =>
        assert(a.length + b.length + c.length + d.length < 0)
      }
    }
  }

  // Same thing, but set minSuccessful to 5 with param, prop fails after 5
  it("generator-driven property that takes 4 args, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    forAll (minSuccessful(5)) { (a: String, b: String, c: String, d: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 4 args, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (minSuccessful(5)) { (a: String, b: String, c: String, d: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 4 named args, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    forAll ("a", "b", "c", "d", minSuccessful(5)) { (a: String, b: String, c: String, d: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 4 named args, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ("a", "b", "c", "d", minSuccessful(5)) { (a: String, b: String, c: String, d: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 4 args and generators, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, minSuccessful(5)) { (a: String, b: String, c: String, d: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 4 args and generators, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, minSuccessful(5)) { (a: String, b: String, c: String, d: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 4 named args and generators, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), minSuccessful(5)) { (a: String, b: String, c: String, d: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 4 named args and generators, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), minSuccessful(5)) { (a: String, b: String, c: String, d: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  // Same thing, but set default minSuccessful to 5, prop fails after 5
  it("generator-driven property that takes 4 args, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    forAll { (a: String, b: String, c: String, d: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 4 args, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll { (a: String, b: String, c: String, d: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 4 named args, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    forAll ("a", "b", "c", "d") { (a: String, b: String, c: String, d: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 4 named args, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ("a", "b", "c", "d") { (a: String, b: String, c: String, d: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 4 args and generators, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 4 args and generators, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 4 named args and generators, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d")) { (a: String, b: String, c: String, d: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 4 named args and generators, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d")) { (a: String, b: String, c: String, d: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  // Same thing, but set maxDiscardedFactor to 0.6 with param, prop fails after 5
  it("generator-driven property that takes 4 args, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    forAll (maxDiscardedFactor(0.6)) { (a: String, b: String, c: String, d: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 4 args, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (maxDiscardedFactor(0.6)) { (a: String, b: String, c: String, d: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 4 named args, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    forAll ("a", "b", "c", "d", maxDiscardedFactor(0.6)) { (a: String, b: String, c: String, d: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 4 named args, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ("a", "b", "c", "d", maxDiscardedFactor(0.6)) { (a: String, b: String, c: String, d: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 4 args and generators, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, maxDiscardedFactor(0.6)) { (a: String, b: String, c: String, d: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 4 args and generators, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, maxDiscardedFactor(0.6)) { (a: String, b: String, c: String, d: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 4 named args and generators, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), maxDiscardedFactor(0.6)) { (a: String, b: String, c: String, d: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 4 named args and generators, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), maxDiscardedFactor(0.6)) { (a: String, b: String, c: String, d: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  // Same thing, but set default maxDiscardedFactor to 0.6, prop fails after 5
  it("generator-driven property that takes 4 args, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    forAll { (a: String, b: String, c: String, d: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 4 args, which fails, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll { (a: String, b: String, c: String, d: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 4 named args, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    forAll ("a", "b", "c", "d") { (a: String, b: String, c: String, d: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 4 named args, which fails, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ("a", "b", "c", "d") { (a: String, b: String, c: String, d: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 4 args and generators, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 4 args and generators, which fails, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 4 named args and generators, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d")) { (a: String, b: String, c: String, d: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 4 named args and generators, which fails, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d")) { (a: String, b: String, c: String, d: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  // set sizeRange with param 
  it("generator-driven property that takes 4 args, with sizeRange specified as param") {

    forAll (sizeRange(5)) { (a: String, b: String, c: String, d: String) =>
      assert(a.length <= 5)
      assert(b.length <= 5)
      assert(c.length <= 5)
      assert(d.length <= 5)
    }
  }

  it("generator-driven property that takes 4 named args, with sizeRange specified as param") {

    forAll ("a", "b", "c", "d", sizeRange(5)) { (a: String, b: String, c: String, d: String) =>
      assert(a.length <= 5)
      assert(b.length <= 5)
      assert(c.length <= 5)
      assert(d.length <= 5)
    }
  }

  // set sizeRange with default (ensure always passed with a size less than sizeRange)
  it("generator-driven property that takes 4 args, with sizeRange specified as default") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 5)

    forAll { (a: String, b: String, c: String, d: String) =>
      assert(a.length <= 5)
      assert(b.length <= 5)
      assert(c.length <= 5)
      assert(d.length <= 5)
    }
  }

  it("generator-driven property that takes 4 named args, with sizeRange specified as default") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 5)

    forAll ("a", "b", "c", "d") { (a: String, b: String, c: String, d: String) =>
      assert(a.length <= 5)
      assert(b.length <= 5)
      assert(c.length <= 5)
      assert(d.length <= 5)
    }
  }

  // set sizeRange == 0 with (param, param)
  it("generator-driven property that takes 4 args and generators, with sizeRange == 0, specified as (param, param)") {

    forAll (fiveFive, fiveFive, fiveFive, fiveFive, minSize(5), sizeRange(0)) { (a: String, b: String, c: String, d: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
    }
  }

  it("generator-driven property that takes 4 named args and generators, with sizeRange == 0, specified as (param, param)") {

    forAll ((fiveFive, "a"), (fiveFive, "b"), (fiveFive, "c"), (fiveFive, "d"), minSize(5), sizeRange(0)) { (a: String, b: String, c: String, d: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
    }
  }

  // set sizeRange == 0 with (param, default) (ensure always passed with that size)
  it("generator-driven property that takes 4 args and generators, with sizeRange == 0, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 0)

    forAll (fiveFive, fiveFive, fiveFive, fiveFive, minSize(5)) { (a: String, b: String, c: String, d: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
    }
  }

  it("generator-driven property that takes 4 named args and generators, with sizeRange == 0, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 0)

    forAll ((fiveFive, "a"), (fiveFive, "b"), (fiveFive, "c"), (fiveFive, "d"), minSize(5)) { (a: String, b: String, c: String, d: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
    }
  }

  // set sizeRange == 0 with (default, param) (ensure always passed with that size)
  it("generator-driven property that takes 4 args and generators, with sizeRange == 0, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5)

    forAll (fiveFive, fiveFive, fiveFive, fiveFive, sizeRange(0)) { (a: String, b: String, c: String, d: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
    }
  }

  it("generator-driven property that takes 4 named args and generators, with sizeRange == 0, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5)

    forAll ((fiveFive, "a"), (fiveFive, "b"), (fiveFive, "c"), (fiveFive, "d"), sizeRange(0)) { (a: String, b: String, c: String, d: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
    }
  }

  // set sizeRange == 0 with (default, default) (ensure always passed with that size)
  it("generator-driven property that takes 4 args and generators, with sizeRange == 0, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5, sizeRange = 0)

    forAll (fiveFive, fiveFive, fiveFive, fiveFive) { (a: String, b: String, c: String, d: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
    }
  }

  it("generator-driven property that takes 4 named args and generators, with sizeRange == 0, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5, sizeRange = 0)

    forAll ((fiveFive, "a"), (fiveFive, "b"), (fiveFive, "c"), (fiveFive, "d")) { (a: String, b: String, c: String, d: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
    }
  }

  // set minSize to 7 and sizeRange to 4 with (param, param) (ensure always passed with that size)
  it("generator-driven property that takes 4 args and generators, with minSize to 7 and sizeRange to 4, specified as (param, param)") {

    forAll (sevenEleven, sevenEleven, sevenEleven, sevenEleven, minSize(7), sizeRange(4)) { (a: String, b: String, c: String, d: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
    }
  }

  it("generator-driven property that takes 4 named args and generators, with minSize to 7 and sizeRange to 4, specified as (param, param)") {

    forAll ((sevenEleven, "a"), (sevenEleven, "b"), (sevenEleven, "c"), (sevenEleven, "d"), minSize(7), sizeRange(4)) { (a: String, b: String, c: String, d: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
    }
  }

  // set minSize to 7 and sizeRange to 4 with (param, default) (ensure always passed with that size)
  it("generator-driven property that takes 4 args and generators, with minSize to 7 and sizeRange to 4, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 4)

    forAll (sevenEleven, sevenEleven, sevenEleven, sevenEleven, minSize(7)) { (a: String, b: String, c: String, d: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
    }
  }

  it("generator-driven property that takes 4 named args and generators, with minSize to 7 and sizeRange to 4, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 4)

    forAll ((sevenEleven, "a"), (sevenEleven, "b"), (sevenEleven, "c"), (sevenEleven, "d"), minSize(7)) { (a: String, b: String, c: String, d: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
    }
  }

  // set minSize to 7 and sizeRange to 4 with (default, param) (ensure always passed with that size)
  it("generator-driven property that takes 4 args and generators, with minSize to 7 and sizeRange to 4, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7)

    forAll (sevenEleven, sevenEleven, sevenEleven, sevenEleven, sizeRange(4)) { (a: String, b: String, c: String, d: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
    }
  }

  it("generator-driven property that takes 4 named args and generators, with minSize to 7 and sizeRange to 4, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7)

    forAll ((sevenEleven, "a"), (sevenEleven, "b"), (sevenEleven, "c"), (sevenEleven, "d"), sizeRange(4)) { (a: String, b: String, c: String, d: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
    }
  }

  // set minSize to 7 and sizeRange to 4 with (default, default) (ensure always passed with that size)
  it("generator-driven property that takes 4 args and generators, with minSize to 7 and sizeRange to 4, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7, sizeRange = 4)

    forAll (sevenEleven, sevenEleven, sevenEleven, sevenEleven) { (a: String, b: String, c: String, d: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
    }
  }

  it("generator-driven property that takes 4 named args and generators, with minSize to 7 and sizeRange to 4, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7, sizeRange = 4)

    forAll ((sevenEleven, "a"), (sevenEleven, "b"), (sevenEleven, "c"), (sevenEleven, "d")) { (a: String, b: String, c: String, d: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
    }
  }
                               

  it("generator-driven property that takes 5 args, which succeeds") {

    forAll { (a: String, b: String, c: String, d: String, e: String) =>
      assert(a.length + b.length + c.length + d.length + e.length === ((a + b + c + d + e).length))
    }
  }

  it("generator-driven property that takes 5 args, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll { (a: String, b: String, c: String, d: String, e: String) =>
        assert(a.length + b.length + c.length + d.length + e.length < 0)
      }
    }
  }

  it("generator-driven property that takes 5 named args, which succeeds") {

    forAll ("a", "b", "c", "d", "e") { (a: String, b: String, c: String, d: String, e: String) =>
      assert(a.length + b.length + c.length + d.length + e.length === ((a + b + c + d + e).length))
    }
  }

  it("generator-driven property that takes 5 named args, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ("a", "b", "c", "d", "e") { (a: String, b: String, c: String, d: String, e: String) =>
        assert(a.length + b.length + c.length + d.length + e.length < 0)
      }
    }
  }

  it("generator-driven property that takes 5 args and generators, which succeeds") {

    forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String) =>
      assert(a.length + b.length + c.length + d.length + e.length === ((a + b + c + d + e).length))
    }
  }

  it("generator-driven property that takes 5 args and generators, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String) =>
        assert(a.length + b.length + c.length + d.length + e.length < 0)
      }
    }
  }

  it("generator-driven property that takes 5 named args and generators, which succeeds") {

    forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), (famousLastWords, "e")) { (a: String, b: String, c: String, d: String, e: String) =>
      assert(a.length + b.length + c.length + d.length + e.length === ((a + b + c + d + e).length))
    }
  }

  it("generator-driven property that takes 5 named args and generators, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), (famousLastWords, "e")) { (a: String, b: String, c: String, d: String, e: String) =>
        assert(a.length + b.length + c.length + d.length + e.length < 0)
      }
    }
  }

  // Same thing, but with config params
  it("generator-driven property that takes 5 args, which succeeds, with config params") {

    forAll (minSize(10), sizeRange(10)) { (a: String, b: String, c: String, d: String, e: String) =>
      assert(a.length + b.length + c.length + d.length + e.length === ((a + b + c + d + e).length))
    }
  }

  it("generator-driven property that takes 5 args, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll (minSize(10), sizeRange(10)) { (a: String, b: String, c: String, d: String, e: String) =>
        assert(a.length + b.length + c.length + d.length + e.length < 0)
      }
    }
  }

  it("generator-driven property that takes 5 named args, which succeeds, with config params") {

    forAll ("a", "b", "c", "d", "e", minSize(10), sizeRange(10)) { (a: String, b: String, c: String, d: String, e: String) =>
      assert(a.length + b.length + c.length + d.length + e.length === ((a + b + c + d + e).length))
    }
  }

  it("generator-driven property that takes 5 named args, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ("a", "b", "c", "d", "e", minSize(10), sizeRange(10)) { (a: String, b: String, c: String, d: String, e: String) =>
        assert(a.length + b.length + c.length + d.length + e.length < 0)
      }
    }
  }

  it("generator-driven property that takes 5 args and generators, which succeeds, with config params") {

    forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, minSize(10), sizeRange(10)) { (a: String, b: String, c: String, d: String, e: String) =>
      assert(a.length + b.length + c.length + d.length + e.length === ((a + b + c + d + e).length))
    }
  }

  it("generator-driven property that takes 5 args and generators, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, minSize(10), sizeRange(10)) { (a: String, b: String, c: String, d: String, e: String) =>
        assert(a.length + b.length + c.length + d.length + e.length < 0)
      }
    }
  }

  it("generator-driven property that takes 5 named args and generators, which succeeds, with config params") {

    forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), (famousLastWords, "e"), minSize(10), sizeRange(10)) { (a: String, b: String, c: String, d: String, e: String) =>
      assert(a.length + b.length + c.length + d.length + e.length === ((a + b + c + d + e).length))
    }
  }

  it("generator-driven property that takes 5 named args and generators, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), (famousLastWords, "e"), minSize(10), sizeRange(10)) { (a: String, b: String, c: String, d: String, e: String) =>
        assert(a.length + b.length + c.length + d.length + e.length < 0)
      }
    }
  }

  // Same thing, but set minSuccessful to 5 with param, prop fails after 5
  it("generator-driven property that takes 5 args, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    forAll (minSuccessful(5)) { (a: String, b: String, c: String, d: String, e: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 5 args, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (minSuccessful(5)) { (a: String, b: String, c: String, d: String, e: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 5 named args, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    forAll ("a", "b", "c", "d", "e", minSuccessful(5)) { (a: String, b: String, c: String, d: String, e: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 5 named args, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ("a", "b", "c", "d", "e", minSuccessful(5)) { (a: String, b: String, c: String, d: String, e: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 5 args and generators, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, minSuccessful(5)) { (a: String, b: String, c: String, d: String, e: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 5 args and generators, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, minSuccessful(5)) { (a: String, b: String, c: String, d: String, e: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 5 named args and generators, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), (famousLastWords, "e"), minSuccessful(5)) { (a: String, b: String, c: String, d: String, e: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 5 named args and generators, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), (famousLastWords, "e"), minSuccessful(5)) { (a: String, b: String, c: String, d: String, e: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  // Same thing, but set default minSuccessful to 5, prop fails after 5
  it("generator-driven property that takes 5 args, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    forAll { (a: String, b: String, c: String, d: String, e: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 5 args, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll { (a: String, b: String, c: String, d: String, e: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 5 named args, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    forAll ("a", "b", "c", "d", "e") { (a: String, b: String, c: String, d: String, e: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 5 named args, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ("a", "b", "c", "d", "e") { (a: String, b: String, c: String, d: String, e: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 5 args and generators, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 5 args and generators, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 5 named args and generators, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), (famousLastWords, "e")) { (a: String, b: String, c: String, d: String, e: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 5 named args and generators, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), (famousLastWords, "e")) { (a: String, b: String, c: String, d: String, e: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  // Same thing, but set maxDiscardedFactor to 0.6 with param, prop fails after 5
  it("generator-driven property that takes 5 args, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    forAll (maxDiscardedFactor(0.6)) { (a: String, b: String, c: String, d: String, e: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 5 args, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (maxDiscardedFactor(0.6)) { (a: String, b: String, c: String, d: String, e: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 5 named args, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    forAll ("a", "b", "c", "d", "e", maxDiscardedFactor(0.6)) { (a: String, b: String, c: String, d: String, e: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 5 named args, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ("a", "b", "c", "d", "e", maxDiscardedFactor(0.6)) { (a: String, b: String, c: String, d: String, e: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 5 args and generators, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, maxDiscardedFactor(0.6)) { (a: String, b: String, c: String, d: String, e: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 5 args and generators, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, maxDiscardedFactor(0.6)) { (a: String, b: String, c: String, d: String, e: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 5 named args and generators, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), (famousLastWords, "e"), maxDiscardedFactor(0.6)) { (a: String, b: String, c: String, d: String, e: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 5 named args and generators, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), (famousLastWords, "e"), maxDiscardedFactor(0.6)) { (a: String, b: String, c: String, d: String, e: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  // Same thing, but set default maxDiscardedFactor to 0.6, prop fails after 5
  it("generator-driven property that takes 5 args, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    forAll { (a: String, b: String, c: String, d: String, e: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 5 args, which fails, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll { (a: String, b: String, c: String, d: String, e: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 5 named args, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    forAll ("a", "b", "c", "d", "e") { (a: String, b: String, c: String, d: String, e: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 5 named args, which fails, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ("a", "b", "c", "d", "e") { (a: String, b: String, c: String, d: String, e: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 5 args and generators, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 5 args and generators, which fails, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 5 named args and generators, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), (famousLastWords, "e")) { (a: String, b: String, c: String, d: String, e: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 5 named args and generators, which fails, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), (famousLastWords, "e")) { (a: String, b: String, c: String, d: String, e: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  // set sizeRange with param 
  it("generator-driven property that takes 5 args, with sizeRange specified as param") {

    forAll (sizeRange(5)) { (a: String, b: String, c: String, d: String, e: String) =>
      assert(a.length <= 5)
      assert(b.length <= 5)
      assert(c.length <= 5)
      assert(d.length <= 5)
      assert(e.length <= 5)
    }
  }

  it("generator-driven property that takes 5 named args, with sizeRange specified as param") {

    forAll ("a", "b", "c", "d", "e", sizeRange(5)) { (a: String, b: String, c: String, d: String, e: String) =>
      assert(a.length <= 5)
      assert(b.length <= 5)
      assert(c.length <= 5)
      assert(d.length <= 5)
      assert(e.length <= 5)
    }
  }

  // set sizeRange with default (ensure always passed with a size less than sizeRange)
  it("generator-driven property that takes 5 args, with sizeRange specified as default") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 5)

    forAll { (a: String, b: String, c: String, d: String, e: String) =>
      assert(a.length <= 5)
      assert(b.length <= 5)
      assert(c.length <= 5)
      assert(d.length <= 5)
      assert(e.length <= 5)
    }
  }

  it("generator-driven property that takes 5 named args, with sizeRange specified as default") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 5)

    forAll ("a", "b", "c", "d", "e") { (a: String, b: String, c: String, d: String, e: String) =>
      assert(a.length <= 5)
      assert(b.length <= 5)
      assert(c.length <= 5)
      assert(d.length <= 5)
      assert(e.length <= 5)
    }
  }

  // set sizeRange == 0 with (param, param)
  it("generator-driven property that takes 5 args and generators, with sizeRange == 0, specified as (param, param)") {

    forAll (fiveFive, fiveFive, fiveFive, fiveFive, fiveFive, minSize(5), sizeRange(0)) { (a: String, b: String, c: String, d: String, e: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
    }
  }

  it("generator-driven property that takes 5 named args and generators, with sizeRange == 0, specified as (param, param)") {

    forAll ((fiveFive, "a"), (fiveFive, "b"), (fiveFive, "c"), (fiveFive, "d"), (fiveFive, "e"), minSize(5), sizeRange(0)) { (a: String, b: String, c: String, d: String, e: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
    }
  }

  // set sizeRange == 0 with (param, default) (ensure always passed with that size)
  it("generator-driven property that takes 5 args and generators, with sizeRange == 0, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 0)

    forAll (fiveFive, fiveFive, fiveFive, fiveFive, fiveFive, minSize(5)) { (a: String, b: String, c: String, d: String, e: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
    }
  }

  it("generator-driven property that takes 5 named args and generators, with sizeRange == 0, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 0)

    forAll ((fiveFive, "a"), (fiveFive, "b"), (fiveFive, "c"), (fiveFive, "d"), (fiveFive, "e"), minSize(5)) { (a: String, b: String, c: String, d: String, e: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
    }
  }

  // set sizeRange == 0 with (default, param) (ensure always passed with that size)
  it("generator-driven property that takes 5 args and generators, with sizeRange == 0, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5)

    forAll (fiveFive, fiveFive, fiveFive, fiveFive, fiveFive, sizeRange(0)) { (a: String, b: String, c: String, d: String, e: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
    }
  }

  it("generator-driven property that takes 5 named args and generators, with sizeRange == 0, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5)

    forAll ((fiveFive, "a"), (fiveFive, "b"), (fiveFive, "c"), (fiveFive, "d"), (fiveFive, "e"), sizeRange(0)) { (a: String, b: String, c: String, d: String, e: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
    }
  }

  // set sizeRange == 0 with (default, default) (ensure always passed with that size)
  it("generator-driven property that takes 5 args and generators, with sizeRange == 0, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5, sizeRange = 0)

    forAll (fiveFive, fiveFive, fiveFive, fiveFive, fiveFive) { (a: String, b: String, c: String, d: String, e: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
    }
  }

  it("generator-driven property that takes 5 named args and generators, with sizeRange == 0, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5, sizeRange = 0)

    forAll ((fiveFive, "a"), (fiveFive, "b"), (fiveFive, "c"), (fiveFive, "d"), (fiveFive, "e")) { (a: String, b: String, c: String, d: String, e: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
    }
  }

  // set minSize to 7 and sizeRange to 4 with (param, param) (ensure always passed with that size)
  it("generator-driven property that takes 5 args and generators, with minSize to 7 and sizeRange to 4, specified as (param, param)") {

    forAll (sevenEleven, sevenEleven, sevenEleven, sevenEleven, sevenEleven, minSize(7), sizeRange(4)) { (a: String, b: String, c: String, d: String, e: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
    }
  }

  it("generator-driven property that takes 5 named args and generators, with minSize to 7 and sizeRange to 4, specified as (param, param)") {

    forAll ((sevenEleven, "a"), (sevenEleven, "b"), (sevenEleven, "c"), (sevenEleven, "d"), (sevenEleven, "e"), minSize(7), sizeRange(4)) { (a: String, b: String, c: String, d: String, e: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
    }
  }

  // set minSize to 7 and sizeRange to 4 with (param, default) (ensure always passed with that size)
  it("generator-driven property that takes 5 args and generators, with minSize to 7 and sizeRange to 4, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 4)

    forAll (sevenEleven, sevenEleven, sevenEleven, sevenEleven, sevenEleven, minSize(7)) { (a: String, b: String, c: String, d: String, e: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
    }
  }

  it("generator-driven property that takes 5 named args and generators, with minSize to 7 and sizeRange to 4, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 4)

    forAll ((sevenEleven, "a"), (sevenEleven, "b"), (sevenEleven, "c"), (sevenEleven, "d"), (sevenEleven, "e"), minSize(7)) { (a: String, b: String, c: String, d: String, e: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
    }
  }

  // set minSize to 7 and sizeRange to 4 with (default, param) (ensure always passed with that size)
  it("generator-driven property that takes 5 args and generators, with minSize to 7 and sizeRange to 4, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7)

    forAll (sevenEleven, sevenEleven, sevenEleven, sevenEleven, sevenEleven, sizeRange(4)) { (a: String, b: String, c: String, d: String, e: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
    }
  }

  it("generator-driven property that takes 5 named args and generators, with minSize to 7 and sizeRange to 4, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7)

    forAll ((sevenEleven, "a"), (sevenEleven, "b"), (sevenEleven, "c"), (sevenEleven, "d"), (sevenEleven, "e"), sizeRange(4)) { (a: String, b: String, c: String, d: String, e: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
    }
  }

  // set minSize to 7 and sizeRange to 4 with (default, default) (ensure always passed with that size)
  it("generator-driven property that takes 5 args and generators, with minSize to 7 and sizeRange to 4, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7, sizeRange = 4)

    forAll (sevenEleven, sevenEleven, sevenEleven, sevenEleven, sevenEleven) { (a: String, b: String, c: String, d: String, e: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
    }
  }

  it("generator-driven property that takes 5 named args and generators, with minSize to 7 and sizeRange to 4, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7, sizeRange = 4)

    forAll ((sevenEleven, "a"), (sevenEleven, "b"), (sevenEleven, "c"), (sevenEleven, "d"), (sevenEleven, "e")) { (a: String, b: String, c: String, d: String, e: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
    }
  }
                               

  it("generator-driven property that takes 6 args, which succeeds") {

    forAll { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      assert(a.length + b.length + c.length + d.length + e.length + f.length === ((a + b + c + d + e + f).length))
    }
  }

  it("generator-driven property that takes 6 args, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        assert(a.length + b.length + c.length + d.length + e.length + f.length < 0)
      }
    }
  }

  it("generator-driven property that takes 6 named args, which succeeds") {

    forAll ("a", "b", "c", "d", "e", "f") { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      assert(a.length + b.length + c.length + d.length + e.length + f.length === ((a + b + c + d + e + f).length))
    }
  }

  it("generator-driven property that takes 6 named args, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ("a", "b", "c", "d", "e", "f") { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        assert(a.length + b.length + c.length + d.length + e.length + f.length < 0)
      }
    }
  }

  it("generator-driven property that takes 6 args and generators, which succeeds") {

    forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      assert(a.length + b.length + c.length + d.length + e.length + f.length === ((a + b + c + d + e + f).length))
    }
  }

  it("generator-driven property that takes 6 args and generators, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        assert(a.length + b.length + c.length + d.length + e.length + f.length < 0)
      }
    }
  }

  it("generator-driven property that takes 6 named args and generators, which succeeds") {

    forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), (famousLastWords, "e"), (famousLastWords, "f")) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      assert(a.length + b.length + c.length + d.length + e.length + f.length === ((a + b + c + d + e + f).length))
    }
  }

  it("generator-driven property that takes 6 named args and generators, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), (famousLastWords, "e"), (famousLastWords, "f")) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        assert(a.length + b.length + c.length + d.length + e.length + f.length < 0)
      }
    }
  }

  // Same thing, but with config params
  it("generator-driven property that takes 6 args, which succeeds, with config params") {

    forAll (minSize(10), sizeRange(10)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      assert(a.length + b.length + c.length + d.length + e.length + f.length === ((a + b + c + d + e + f).length))
    }
  }

  it("generator-driven property that takes 6 args, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll (minSize(10), sizeRange(10)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        assert(a.length + b.length + c.length + d.length + e.length + f.length < 0)
      }
    }
  }

  it("generator-driven property that takes 6 named args, which succeeds, with config params") {

    forAll ("a", "b", "c", "d", "e", "f", minSize(10), sizeRange(10)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      assert(a.length + b.length + c.length + d.length + e.length + f.length === ((a + b + c + d + e + f).length))
    }
  }

  it("generator-driven property that takes 6 named args, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ("a", "b", "c", "d", "e", "f", minSize(10), sizeRange(10)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        assert(a.length + b.length + c.length + d.length + e.length + f.length < 0)
      }
    }
  }

  it("generator-driven property that takes 6 args and generators, which succeeds, with config params") {

    forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, minSize(10), sizeRange(10)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      assert(a.length + b.length + c.length + d.length + e.length + f.length === ((a + b + c + d + e + f).length))
    }
  }

  it("generator-driven property that takes 6 args and generators, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, minSize(10), sizeRange(10)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        assert(a.length + b.length + c.length + d.length + e.length + f.length < 0)
      }
    }
  }

  it("generator-driven property that takes 6 named args and generators, which succeeds, with config params") {

    forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), (famousLastWords, "e"), (famousLastWords, "f"), minSize(10), sizeRange(10)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      assert(a.length + b.length + c.length + d.length + e.length + f.length === ((a + b + c + d + e + f).length))
    }
  }

  it("generator-driven property that takes 6 named args and generators, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), (famousLastWords, "e"), (famousLastWords, "f"), minSize(10), sizeRange(10)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        assert(a.length + b.length + c.length + d.length + e.length + f.length < 0)
      }
    }
  }

  // Same thing, but set minSuccessful to 5 with param, prop fails after 5
  it("generator-driven property that takes 6 args, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    forAll (minSuccessful(5)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 6 args, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (minSuccessful(5)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 6 named args, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    forAll ("a", "b", "c", "d", "e", "f", minSuccessful(5)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 6 named args, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ("a", "b", "c", "d", "e", "f", minSuccessful(5)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 6 args and generators, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, minSuccessful(5)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 6 args and generators, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, minSuccessful(5)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 6 named args and generators, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), (famousLastWords, "e"), (famousLastWords, "f"), minSuccessful(5)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 6 named args and generators, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), (famousLastWords, "e"), (famousLastWords, "f"), minSuccessful(5)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  // Same thing, but set default minSuccessful to 5, prop fails after 5
  it("generator-driven property that takes 6 args, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    forAll { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 6 args, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 6 named args, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    forAll ("a", "b", "c", "d", "e", "f") { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 6 named args, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ("a", "b", "c", "d", "e", "f") { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 6 args and generators, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 6 args and generators, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  it("generator-driven property that takes 6 named args and generators, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), (famousLastWords, "e"), (famousLastWords, "f")) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      i += 1
      assert(i != 6)
    }
  }

  it("generator-driven property that takes 6 named args and generators, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), (famousLastWords, "e"), (famousLastWords, "f")) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        i += 1
        assert(i != 5)
      }
    }
  }

  // Same thing, but set maxDiscardedFactor to 0.6 with param, prop fails after 5
  it("generator-driven property that takes 6 args, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    forAll (maxDiscardedFactor(0.6)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 6 args, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (maxDiscardedFactor(0.6)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 6 named args, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    forAll ("a", "b", "c", "d", "e", "f", maxDiscardedFactor(0.6)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 6 named args, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ("a", "b", "c", "d", "e", "f", maxDiscardedFactor(0.6)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 6 args and generators, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, maxDiscardedFactor(0.6)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 6 args and generators, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, maxDiscardedFactor(0.6)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 6 named args and generators, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), (famousLastWords, "e"), (famousLastWords, "f"), maxDiscardedFactor(0.6)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 6 named args and generators, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), (famousLastWords, "e"), (famousLastWords, "f"), maxDiscardedFactor(0.6)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  // Same thing, but set default maxDiscardedFactor to 0.6, prop fails after 5
  it("generator-driven property that takes 6 args, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    forAll { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 6 args, which fails, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 6 named args, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    forAll ("a", "b", "c", "d", "e", "f") { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 6 named args, which fails, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ("a", "b", "c", "d", "e", "f") { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 6 args and generators, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 6 args and generators, which fails, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  it("generator-driven property that takes 6 named args and generators, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), (famousLastWords, "e"), (famousLastWords, "f")) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      i += 1
      whenever (i > 5) { assert(1 + 1 === (2)) }
    }
  }

  it("generator-driven property that takes 6 named args and generators, which fails, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      forAll ((famousLastWords, "a"), (famousLastWords, "b"), (famousLastWords, "c"), (famousLastWords, "d"), (famousLastWords, "e"), (famousLastWords, "f")) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        i += 1
        whenever (i > 7) { assert(1 + 1 === (2)) }
      }
    }
  }

  // set sizeRange with param 
  it("generator-driven property that takes 6 args, with sizeRange specified as param") {

    forAll (sizeRange(5)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      assert(a.length <= 5)
      assert(b.length <= 5)
      assert(c.length <= 5)
      assert(d.length <= 5)
      assert(e.length <= 5)
      assert(f.length <= 5)
    }
  }

  it("generator-driven property that takes 6 named args, with sizeRange specified as param") {

    forAll ("a", "b", "c", "d", "e", "f", sizeRange(5)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      assert(a.length <= 5)
      assert(b.length <= 5)
      assert(c.length <= 5)
      assert(d.length <= 5)
      assert(e.length <= 5)
      assert(f.length <= 5)
    }
  }

  // set sizeRange with default (ensure always passed with a size less than sizeRange)
  it("generator-driven property that takes 6 args, with sizeRange specified as default") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 5)

    forAll { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      assert(a.length <= 5)
      assert(b.length <= 5)
      assert(c.length <= 5)
      assert(d.length <= 5)
      assert(e.length <= 5)
      assert(f.length <= 5)
    }
  }

  it("generator-driven property that takes 6 named args, with sizeRange specified as default") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 5)

    forAll ("a", "b", "c", "d", "e", "f") { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      assert(a.length <= 5)
      assert(b.length <= 5)
      assert(c.length <= 5)
      assert(d.length <= 5)
      assert(e.length <= 5)
      assert(f.length <= 5)
    }
  }

  // set sizeRange == 0 with (param, param)
  it("generator-driven property that takes 6 args and generators, with sizeRange == 0, specified as (param, param)") {

    forAll (fiveFive, fiveFive, fiveFive, fiveFive, fiveFive, fiveFive, minSize(5), sizeRange(0)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
        assert(f === ("OKAY"))
    }
  }

  it("generator-driven property that takes 6 named args and generators, with sizeRange == 0, specified as (param, param)") {

    forAll ((fiveFive, "a"), (fiveFive, "b"), (fiveFive, "c"), (fiveFive, "d"), (fiveFive, "e"), (fiveFive, "f"), minSize(5), sizeRange(0)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
        assert(f === ("OKAY"))
    }
  }

  // set sizeRange == 0 with (param, default) (ensure always passed with that size)
  it("generator-driven property that takes 6 args and generators, with sizeRange == 0, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 0)

    forAll (fiveFive, fiveFive, fiveFive, fiveFive, fiveFive, fiveFive, minSize(5)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
        assert(f === ("OKAY"))
    }
  }

  it("generator-driven property that takes 6 named args and generators, with sizeRange == 0, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 0)

    forAll ((fiveFive, "a"), (fiveFive, "b"), (fiveFive, "c"), (fiveFive, "d"), (fiveFive, "e"), (fiveFive, "f"), minSize(5)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
        assert(f === ("OKAY"))
    }
  }

  // set sizeRange == 0 with (default, param) (ensure always passed with that size)
  it("generator-driven property that takes 6 args and generators, with sizeRange == 0, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5)

    forAll (fiveFive, fiveFive, fiveFive, fiveFive, fiveFive, fiveFive, sizeRange(0)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
        assert(f === ("OKAY"))
    }
  }

  it("generator-driven property that takes 6 named args and generators, with sizeRange == 0, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5)

    forAll ((fiveFive, "a"), (fiveFive, "b"), (fiveFive, "c"), (fiveFive, "d"), (fiveFive, "e"), (fiveFive, "f"), sizeRange(0)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
        assert(f === ("OKAY"))
    }
  }

  // set sizeRange == 0 with (default, default) (ensure always passed with that size)
  it("generator-driven property that takes 6 args and generators, with sizeRange == 0, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5, sizeRange = 0)

    forAll (fiveFive, fiveFive, fiveFive, fiveFive, fiveFive, fiveFive) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
        assert(f === ("OKAY"))
    }
  }

  it("generator-driven property that takes 6 named args and generators, with sizeRange == 0, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5, sizeRange = 0)

    forAll ((fiveFive, "a"), (fiveFive, "b"), (fiveFive, "c"), (fiveFive, "d"), (fiveFive, "e"), (fiveFive, "f")) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
        assert(f === ("OKAY"))
    }
  }

  // set minSize to 7 and sizeRange to 4 with (param, param) (ensure always passed with that size)
  it("generator-driven property that takes 6 args and generators, with minSize to 7 and sizeRange to 4, specified as (param, param)") {

    forAll (sevenEleven, sevenEleven, sevenEleven, sevenEleven, sevenEleven, sevenEleven, minSize(7), sizeRange(4)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
        assert(f === ("OKAY"))
    }
  }

  it("generator-driven property that takes 6 named args and generators, with minSize to 7 and sizeRange to 4, specified as (param, param)") {

    forAll ((sevenEleven, "a"), (sevenEleven, "b"), (sevenEleven, "c"), (sevenEleven, "d"), (sevenEleven, "e"), (sevenEleven, "f"), minSize(7), sizeRange(4)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
        assert(f === ("OKAY"))
    }
  }

  // set minSize to 7 and sizeRange to 4 with (param, default) (ensure always passed with that size)
  it("generator-driven property that takes 6 args and generators, with minSize to 7 and sizeRange to 4, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 4)

    forAll (sevenEleven, sevenEleven, sevenEleven, sevenEleven, sevenEleven, sevenEleven, minSize(7)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
        assert(f === ("OKAY"))
    }
  }

  it("generator-driven property that takes 6 named args and generators, with minSize to 7 and sizeRange to 4, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 4)

    forAll ((sevenEleven, "a"), (sevenEleven, "b"), (sevenEleven, "c"), (sevenEleven, "d"), (sevenEleven, "e"), (sevenEleven, "f"), minSize(7)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
        assert(f === ("OKAY"))
    }
  }

  // set minSize to 7 and sizeRange to 4 with (default, param) (ensure always passed with that size)
  it("generator-driven property that takes 6 args and generators, with minSize to 7 and sizeRange to 4, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7)

    forAll (sevenEleven, sevenEleven, sevenEleven, sevenEleven, sevenEleven, sevenEleven, sizeRange(4)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
        assert(f === ("OKAY"))
    }
  }

  it("generator-driven property that takes 6 named args and generators, with minSize to 7 and sizeRange to 4, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7)

    forAll ((sevenEleven, "a"), (sevenEleven, "b"), (sevenEleven, "c"), (sevenEleven, "d"), (sevenEleven, "e"), (sevenEleven, "f"), sizeRange(4)) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
        assert(f === ("OKAY"))
    }
  }

  // set minSize to 7 and sizeRange to 4 with (default, default) (ensure always passed with that size)
  it("generator-driven property that takes 6 args and generators, with minSize to 7 and sizeRange to 4, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7, sizeRange = 4)

    forAll (sevenEleven, sevenEleven, sevenEleven, sevenEleven, sevenEleven, sevenEleven) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
        assert(f === ("OKAY"))
    }
  }

  it("generator-driven property that takes 6 named args and generators, with minSize to 7 and sizeRange to 4, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7, sizeRange = 4)

    forAll ((sevenEleven, "a"), (sevenEleven, "b"), (sevenEleven, "c"), (sevenEleven, "d"), (sevenEleven, "e"), (sevenEleven, "f")) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        assert(a === ("OKAY"))
        assert(b === ("OKAY"))
        assert(c === ("OKAY"))
        assert(d === ("OKAY"))
        assert(e === ("OKAY"))
        assert(f === ("OKAY"))
    }
  }
                               }
