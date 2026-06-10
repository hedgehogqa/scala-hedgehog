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

import org.scalacheck.Prop.{BooleanOperators => _, Exception => _}
import org.scalatest.exceptions.GeneratorDrivenPropertyCheckFailedException

class AssertCheckersImportSuite extends org.scalatest.funspec.AnyFunSpec  {

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
                                

  it("ScalaCheck property that takes 1 args, which succeeds") {

    check { (a: String) =>
      a.length == ((a).length)
    }
  }

  it("ScalaCheck property that takes 1 args, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      check { (a: String) =>
        a.length < 0
      }
    }
  }

  it("ScalaCheck property that takes 1 args and generators, which succeeds") {

    val prop = forAll (famousLastWords) { (a: String) =>
      a.length == ((a).length)
    }
    check(prop)
  }

  it("ScalaCheck property that takes 1 args and generators, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      val prop = forAll (famousLastWords) { (a: String) =>
        a.length < 0
      }
      check(prop)
    }
  }

  // Same thing, but with config params
  it("ScalaCheck property that takes 1 args, which succeeds, with config params") {

    check(
      (a: String) => a.length == ((a).length),
      minSize(10),
      sizeRange(10)
    )
  }

  it("ScalaCheck property that takes 1 args, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      check(
        (a: String) => a.length < 0,
        minSize(10),
        sizeRange(10)
      )
    }
  }

  it("ScalaCheck property that takes 1 args and generators, which succeeds, with config params") {

    val prop = forAll (famousLastWords) { (a: String) =>
      a.length == ((a).length)
    }
    check(prop, minSize(10), sizeRange(10))
  }

  it("ScalaCheck property that takes 1 args and generators, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      val prop = forAll (famousLastWords) { (a: String) =>
        a.length < 0
      }
      check(prop, minSize(10), sizeRange(10))
    }
  }

  // Same thing, but set minSuccessful to 5 with param, prop fails after 5
  it("ScalaCheck property that takes 1 args, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    check(
      (a: String) => {
        val res = i != 5
        i += 1
        res
      },
      minSuccessful(5)
    )
  }

  it("ScalaCheck property that takes 1 args, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      check(
        (a: String) => {
          val res = i != 4
          i += 1
        res
        },
        minSuccessful(5)
      ) 
    }
  }

  it("ScalaCheck property that takes 1 args and generators, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    val prop = forAll (famousLastWords) { (a: String) =>
      val res = i != 5
      i += 1
      res
    }
    check(prop, minSuccessful(5))
  }

  it("ScalaCheck property that takes 1 args and generators, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      val prop = forAll (famousLastWords) { (a: String) =>
        val res = i != 4
        i += 1
        res
      }
      check(prop, minSuccessful(5))
    }
  }

  // Same thing, but set default minSuccessful to 5, prop fails after 5
  it("ScalaCheck property that takes 1 args, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    check { (a: String) =>
      val res = i != 5
      i += 1
      res
    }
  }

  it("ScalaCheck property that takes 1 args, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      check { (a: String) =>
        val res = i != 4
        i += 1
        res
      }
    }
  }

  it("ScalaCheck property that takes 1 args and generators, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    val prop = forAll (famousLastWords) { (a: String) =>
      val res = i != 5
      i += 1
      res
    }
    check(prop)
  }

  it("ScalaCheck property that takes 1 args and generators, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      val prop = forAll (famousLastWords) { (a: String) =>
        val res = i != 4
        i += 1
        res
      }
      check(prop)
    }
  }

  // Same thing, but set maxDiscardedFactor to 0.6 with param, prop fails after 5
  it("ScalaCheck property that takes 1 args, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    check(
      (a: String) => {
        i += 1
        (i > 5) ==> { 1 + 1 == (2) }
      },
      maxDiscardedFactor(0.6)
    )
  }

  it("ScalaCheck property that takes 1 args, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      check(
        (a: String) => {
          i += 1
          (i > 7) ==> { 1 + 1 == (2) }
        },
        maxDiscardedFactor(0.6)
      ) 
    }
  }

  it("ScalaCheck property that takes 1 args and generators, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    val prop = forAll (famousLastWords) { (a: String) =>
      i += 1
      (i > 5) ==> { 1 + 1 == (2) }
    }
    check(prop, maxDiscardedFactor(0.6))
  }

  it("ScalaCheck property that takes 1 args and generators, which fails, with maxDiscardedFactor param set to 1.2") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      val prop = forAll (famousLastWords) { (a: String) =>
        i += 1
        (i > 7) ==> { 1 + 1 == (2) }
      }
      check(prop, maxDiscardedFactor(1.2))
    }
  }

  // Same thing, but set default maxDiscarded to 5, prop fails after 5
  it("ScalaCheck property that takes 1 args, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    check { (a: String) =>
      i += 1
      (i > 5) ==> { 1 + 1 == (2) }
    }
  }

  it("ScalaCheck property that takes 1 args, which fails, with default maxDiscardedFactor set to 1.2") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 1.2, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      check { (a: String) =>
        i += 1
        (i > 7) ==> { 1 + 1 == (2) }
      }
    }
  }

  it("ScalaCheck property that takes 1 args and generators, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    val prop = forAll (famousLastWords) { (a: String) =>
      i += 1
      (i > 5) ==> { 1 + 1 == (2) }
    }
    check(prop)
  }

  it("ScalaCheck property that takes 1 args and generators, which fails, with default maxDiscardedFactor set to 1.2") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 1.2, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      val prop = forAll (famousLastWords) { (a: String) =>
        i += 1
        (i > 7) ==> { 1 + 1 == (2) }
      }
      check(prop)
    }
  }

  // set sizeRange with param (ensure always passed with a size less than sizeRange)
  it("ScalaCheck property that takes 1 args, with sizeRange specified as param") {

    check(
      (a: String) => {
      a.length <= 5
      },
      sizeRange(5)
    ) 
  }

  // set sizeRange with default (ensure always passed with a size less than sizeRange)
  it("ScalaCheck property that takes 1 args, with sizeRange specified as default") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 5)

    check { (a: String) =>
      a.length <= 5
    }
  }

  // set sizeRange == 0 with (param, param) (ensure always passed with that size)
  it("ScalaCheck property that takes 1 args and generators, with sizeRange == 0, specified as (param, param)") {

    val prop = forAll (fiveFive) { (a: String) =>
        a == ("OKAY")
    }
    check(prop, minSize(5), sizeRange(0))
  }

  // set sizeRange == 0 with (param, default) (ensure always passed with that size)
  it("ScalaCheck property that takes 1 args and generators, with sizeRange == 0, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 0)

    val prop = forAll (fiveFive) { (a: String) =>
        a == ("OKAY")
    }
    check(prop, minSize(5))
  }

  // set sizeRange == 0 with (default, param) (ensure always passed with that size)
  it("ScalaCheck property that takes 1 args and generators, with sizeRange == 0, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5)

    val prop = forAll (fiveFive) { (a: String) =>
        a == ("OKAY")
    }
    check(prop, sizeRange(0))
  }

  // set sizeRange == 0 with (default, default) (ensure always passed with that size)
  it("ScalaCheck property that takes 1 args and generators, with sizeRange == 0, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5, sizeRange = 0)

    val prop = forAll (fiveFive) { (a: String) =>
        a == ("OKAY")
    }
    check(prop)
  }

  // set minSize to 7 and sizeRange to 4 with (param, param) (ensure always passed with that size)
  it("ScalaCheck property that takes 1 args and generators, with minSize to 7 and sizeRange to 4, specified as (param, param)") {

    val prop = forAll (sevenEleven) { (a: String) =>
        a == ("OKAY")
    }
    check(prop, minSize(7), sizeRange(4))
  }

  // set minSize to 7 and sizeRange to 4 with (param, default) (ensure always passed with that size)
  it("ScalaCheck property that takes 1 args and generators, with minSize to 7 and sizeRange to 4, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 4)

    val prop = forAll (sevenEleven) { (a: String) =>
        a == ("OKAY")
    }
    check(prop, minSize(7))
  }

  // set minSize to 7 and sizeRange to 4 with (default, param) (ensure always passed with that size)
  it("ScalaCheck property that takes 1 args and generators, with minSize to 7 and sizeRange to 4, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7)

    val prop = forAll (sevenEleven) { (a: String) =>
        a == ("OKAY")
    }
    check(prop, sizeRange(4))
  }

  // set minSize to 7 and sizeRange to 4 with (default, default) (ensure always passed with that size)
  it("ScalaCheck property that takes 1 args and generators, with minSize to 7 and sizeRange to 4, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7, sizeRange = 4)

    val prop = forAll (sevenEleven) { (a: String) =>
        a == ("OKAY")
    }
    check(prop)
  }
                              

  it("ScalaCheck property that takes 2 args, which succeeds") {

    check { (a: String, b: String) =>
      a.length + b.length == ((a + b).length)
    }
  }

  it("ScalaCheck property that takes 2 args, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      check { (a: String, b: String) =>
        a.length + b.length < 0
      }
    }
  }

  it("ScalaCheck property that takes 2 args and generators, which succeeds") {

    val prop = forAll (famousLastWords, famousLastWords) { (a: String, b: String) =>
      a.length + b.length == ((a + b).length)
    }
    check(prop)
  }

  it("ScalaCheck property that takes 2 args and generators, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      val prop = forAll (famousLastWords, famousLastWords) { (a: String, b: String) =>
        a.length + b.length < 0
      }
      check(prop)
    }
  }

  // Same thing, but with config params
  it("ScalaCheck property that takes 2 args, which succeeds, with config params") {

    check(
      (a: String, b: String) => a.length + b.length == ((a + b).length),
      minSize(10),
      sizeRange(10)
    )
  }

  it("ScalaCheck property that takes 2 args, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      check(
        (a: String, b: String) => a.length + b.length < 0,
        minSize(10),
        sizeRange(10)
      )
    }
  }

  it("ScalaCheck property that takes 2 args and generators, which succeeds, with config params") {

    val prop = forAll (famousLastWords, famousLastWords) { (a: String, b: String) =>
      a.length + b.length == ((a + b).length)
    }
    check(prop, minSize(10), sizeRange(10))
  }

  it("ScalaCheck property that takes 2 args and generators, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      val prop = forAll (famousLastWords, famousLastWords) { (a: String, b: String) =>
        a.length + b.length < 0
      }
      check(prop, minSize(10), sizeRange(10))
    }
  }

  // Same thing, but set minSuccessful to 5 with param, prop fails after 5
  it("ScalaCheck property that takes 2 args, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    check(
      (a: String, b: String) => {
        val res = i != 5
        i += 1
        res
      },
      minSuccessful(5)
    )
  }

  it("ScalaCheck property that takes 2 args, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      check(
        (a: String, b: String) => {
          val res = i != 4
          i += 1
        res
        },
        minSuccessful(5)
      ) 
    }
  }

  it("ScalaCheck property that takes 2 args and generators, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    val prop = forAll (famousLastWords, famousLastWords) { (a: String, b: String) =>
      val res = i != 5
      i += 1
      res
    }
    check(prop, minSuccessful(5))
  }

  it("ScalaCheck property that takes 2 args and generators, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      val prop = forAll (famousLastWords, famousLastWords) { (a: String, b: String) =>
        val res = i != 4
        i += 1
        res
      }
      check(prop, minSuccessful(5))
    }
  }

  // Same thing, but set default minSuccessful to 5, prop fails after 5
  it("ScalaCheck property that takes 2 args, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    check { (a: String, b: String) =>
      val res = i != 5
      i += 1
      res
    }
  }

  it("ScalaCheck property that takes 2 args, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      check { (a: String, b: String) =>
        val res = i != 4
        i += 1
        res
      }
    }
  }

  it("ScalaCheck property that takes 2 args and generators, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    val prop = forAll (famousLastWords, famousLastWords) { (a: String, b: String) =>
      val res = i != 5
      i += 1
      res
    }
    check(prop)
  }

  it("ScalaCheck property that takes 2 args and generators, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      val prop = forAll (famousLastWords, famousLastWords) { (a: String, b: String) =>
        val res = i != 4
        i += 1
        res
      }
      check(prop)
    }
  }

  // Same thing, but set maxDiscardedFactor to 0.6 with param, prop fails after 5
  it("ScalaCheck property that takes 2 args, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    check(
      (a: String, b: String) => {
        i += 1
        (i > 5) ==> { 1 + 1 == (2) }
      },
      maxDiscardedFactor(0.6)
    )
  }

  it("ScalaCheck property that takes 2 args, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      check(
        (a: String, b: String) => {
          i += 1
          (i > 7) ==> { 1 + 1 == (2) }
        },
        maxDiscardedFactor(0.6)
      ) 
    }
  }

  it("ScalaCheck property that takes 2 args and generators, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    val prop = forAll (famousLastWords, famousLastWords) { (a: String, b: String) =>
      i += 1
      (i > 5) ==> { 1 + 1 == (2) }
    }
    check(prop, maxDiscardedFactor(0.6))
  }

  it("ScalaCheck property that takes 2 args and generators, which fails, with maxDiscardedFactor param set to 1.2") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      val prop = forAll (famousLastWords, famousLastWords) { (a: String, b: String) =>
        i += 1
        (i > 7) ==> { 1 + 1 == (2) }
      }
      check(prop, maxDiscardedFactor(1.2))
    }
  }

  // Same thing, but set default maxDiscarded to 5, prop fails after 5
  it("ScalaCheck property that takes 2 args, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    check { (a: String, b: String) =>
      i += 1
      (i > 5) ==> { 1 + 1 == (2) }
    }
  }

  it("ScalaCheck property that takes 2 args, which fails, with default maxDiscardedFactor set to 1.2") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 1.2, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      check { (a: String, b: String) =>
        i += 1
        (i > 7) ==> { 1 + 1 == (2) }
      }
    }
  }

  it("ScalaCheck property that takes 2 args and generators, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    val prop = forAll (famousLastWords, famousLastWords) { (a: String, b: String) =>
      i += 1
      (i > 5) ==> { 1 + 1 == (2) }
    }
    check(prop)
  }

  it("ScalaCheck property that takes 2 args and generators, which fails, with default maxDiscardedFactor set to 1.2") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 1.2, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      val prop = forAll (famousLastWords, famousLastWords) { (a: String, b: String) =>
        i += 1
        (i > 7) ==> { 1 + 1 == (2) }
      }
      check(prop)
    }
  }

  // set sizeRange with param (ensure always passed with a size less than sizeRange)
  it("ScalaCheck property that takes 2 args, with sizeRange specified as param") {

    check(
      (a: String, b: String) => {
      a.length <= 5
      b.length <= 5
      },
      sizeRange(5)
    ) 
  }

  // set sizeRange with default (ensure always passed with a size less than sizeRange)
  it("ScalaCheck property that takes 2 args, with sizeRange specified as default") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 5)

    check { (a: String, b: String) =>
      a.length <= 5
      b.length <= 5
    }
  }

  // set sizeRange == 0 with (param, param) (ensure always passed with that size)
  it("ScalaCheck property that takes 2 args and generators, with sizeRange == 0, specified as (param, param)") {

    val prop = forAll (fiveFive, fiveFive) { (a: String, b: String) =>
        a == ("OKAY")
        b == ("OKAY")
    }
    check(prop, minSize(5), sizeRange(0))
  }

  // set sizeRange == 0 with (param, default) (ensure always passed with that size)
  it("ScalaCheck property that takes 2 args and generators, with sizeRange == 0, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 0)

    val prop = forAll (fiveFive, fiveFive) { (a: String, b: String) =>
        a == ("OKAY")
        b == ("OKAY")
    }
    check(prop, minSize(5))
  }

  // set sizeRange == 0 with (default, param) (ensure always passed with that size)
  it("ScalaCheck property that takes 2 args and generators, with sizeRange == 0, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5)

    val prop = forAll (fiveFive, fiveFive) { (a: String, b: String) =>
        a == ("OKAY")
        b == ("OKAY")
    }
    check(prop, sizeRange(0))
  }

  // set sizeRange == 0 with (default, default) (ensure always passed with that size)
  it("ScalaCheck property that takes 2 args and generators, with sizeRange == 0, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5, sizeRange = 0)

    val prop = forAll (fiveFive, fiveFive) { (a: String, b: String) =>
        a == ("OKAY")
        b == ("OKAY")
    }
    check(prop)
  }

  // set minSize to 7 and sizeRange to 4 with (param, param) (ensure always passed with that size)
  it("ScalaCheck property that takes 2 args and generators, with minSize to 7 and sizeRange to 4, specified as (param, param)") {

    val prop = forAll (sevenEleven, sevenEleven) { (a: String, b: String) =>
        a == ("OKAY")
        b == ("OKAY")
    }
    check(prop, minSize(7), sizeRange(4))
  }

  // set minSize to 7 and sizeRange to 4 with (param, default) (ensure always passed with that size)
  it("ScalaCheck property that takes 2 args and generators, with minSize to 7 and sizeRange to 4, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 4)

    val prop = forAll (sevenEleven, sevenEleven) { (a: String, b: String) =>
        a == ("OKAY")
        b == ("OKAY")
    }
    check(prop, minSize(7))
  }

  // set minSize to 7 and sizeRange to 4 with (default, param) (ensure always passed with that size)
  it("ScalaCheck property that takes 2 args and generators, with minSize to 7 and sizeRange to 4, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7)

    val prop = forAll (sevenEleven, sevenEleven) { (a: String, b: String) =>
        a == ("OKAY")
        b == ("OKAY")
    }
    check(prop, sizeRange(4))
  }

  // set minSize to 7 and sizeRange to 4 with (default, default) (ensure always passed with that size)
  it("ScalaCheck property that takes 2 args and generators, with minSize to 7 and sizeRange to 4, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7, sizeRange = 4)

    val prop = forAll (sevenEleven, sevenEleven) { (a: String, b: String) =>
        a == ("OKAY")
        b == ("OKAY")
    }
    check(prop)
  }
                              

  it("ScalaCheck property that takes 3 args, which succeeds") {

    check { (a: String, b: String, c: String) =>
      a.length + b.length + c.length == ((a + b + c).length)
    }
  }

  it("ScalaCheck property that takes 3 args, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      check { (a: String, b: String, c: String) =>
        a.length + b.length + c.length < 0
      }
    }
  }

  it("ScalaCheck property that takes 3 args and generators, which succeeds") {

    val prop = forAll (famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String) =>
      a.length + b.length + c.length == ((a + b + c).length)
    }
    check(prop)
  }

  it("ScalaCheck property that takes 3 args and generators, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      val prop = forAll (famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String) =>
        a.length + b.length + c.length < 0
      }
      check(prop)
    }
  }

  // Same thing, but with config params
  it("ScalaCheck property that takes 3 args, which succeeds, with config params") {

    check(
      (a: String, b: String, c: String) => a.length + b.length + c.length == ((a + b + c).length),
      minSize(10),
      sizeRange(10)
    )
  }

  it("ScalaCheck property that takes 3 args, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      check(
        (a: String, b: String, c: String) => a.length + b.length + c.length < 0,
        minSize(10),
        sizeRange(10)
      )
    }
  }

  it("ScalaCheck property that takes 3 args and generators, which succeeds, with config params") {

    val prop = forAll (famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String) =>
      a.length + b.length + c.length == ((a + b + c).length)
    }
    check(prop, minSize(10), sizeRange(10))
  }

  it("ScalaCheck property that takes 3 args and generators, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      val prop = forAll (famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String) =>
        a.length + b.length + c.length < 0
      }
      check(prop, minSize(10), sizeRange(10))
    }
  }

  // Same thing, but set minSuccessful to 5 with param, prop fails after 5
  it("ScalaCheck property that takes 3 args, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    check(
      (a: String, b: String, c: String) => {
        val res = i != 5
        i += 1
        res
      },
      minSuccessful(5)
    )
  }

  it("ScalaCheck property that takes 3 args, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      check(
        (a: String, b: String, c: String) => {
          val res = i != 4
          i += 1
        res
        },
        minSuccessful(5)
      ) 
    }
  }

  it("ScalaCheck property that takes 3 args and generators, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    val prop = forAll (famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String) =>
      val res = i != 5
      i += 1
      res
    }
    check(prop, minSuccessful(5))
  }

  it("ScalaCheck property that takes 3 args and generators, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      val prop = forAll (famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String) =>
        val res = i != 4
        i += 1
        res
      }
      check(prop, minSuccessful(5))
    }
  }

  // Same thing, but set default minSuccessful to 5, prop fails after 5
  it("ScalaCheck property that takes 3 args, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    check { (a: String, b: String, c: String) =>
      val res = i != 5
      i += 1
      res
    }
  }

  it("ScalaCheck property that takes 3 args, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      check { (a: String, b: String, c: String) =>
        val res = i != 4
        i += 1
        res
      }
    }
  }

  it("ScalaCheck property that takes 3 args and generators, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    val prop = forAll (famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String) =>
      val res = i != 5
      i += 1
      res
    }
    check(prop)
  }

  it("ScalaCheck property that takes 3 args and generators, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      val prop = forAll (famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String) =>
        val res = i != 4
        i += 1
        res
      }
      check(prop)
    }
  }

  // Same thing, but set maxDiscardedFactor to 0.6 with param, prop fails after 5
  it("ScalaCheck property that takes 3 args, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    check(
      (a: String, b: String, c: String) => {
        i += 1
        (i > 5) ==> { 1 + 1 == (2) }
      },
      maxDiscardedFactor(0.6)
    )
  }

  it("ScalaCheck property that takes 3 args, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      check(
        (a: String, b: String, c: String) => {
          i += 1
          (i > 7) ==> { 1 + 1 == (2) }
        },
        maxDiscardedFactor(0.6)
      ) 
    }
  }

  it("ScalaCheck property that takes 3 args and generators, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    val prop = forAll (famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String) =>
      i += 1
      (i > 5) ==> { 1 + 1 == (2) }
    }
    check(prop, maxDiscardedFactor(0.6))
  }

  it("ScalaCheck property that takes 3 args and generators, which fails, with maxDiscardedFactor param set to 1.2") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      val prop = forAll (famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String) =>
        i += 1
        (i > 7) ==> { 1 + 1 == (2) }
      }
      check(prop, maxDiscardedFactor(1.2))
    }
  }

  // Same thing, but set default maxDiscarded to 5, prop fails after 5
  it("ScalaCheck property that takes 3 args, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    check { (a: String, b: String, c: String) =>
      i += 1
      (i > 5) ==> { 1 + 1 == (2) }
    }
  }

  it("ScalaCheck property that takes 3 args, which fails, with default maxDiscardedFactor set to 1.2") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 1.2, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      check { (a: String, b: String, c: String) =>
        i += 1
        (i > 7) ==> { 1 + 1 == (2) }
      }
    }
  }

  it("ScalaCheck property that takes 3 args and generators, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    val prop = forAll (famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String) =>
      i += 1
      (i > 5) ==> { 1 + 1 == (2) }
    }
    check(prop)
  }

  it("ScalaCheck property that takes 3 args and generators, which fails, with default maxDiscardedFactor set to 1.2") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 1.2, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      val prop = forAll (famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String) =>
        i += 1
        (i > 7) ==> { 1 + 1 == (2) }
      }
      check(prop)
    }
  }

  // set sizeRange with param (ensure always passed with a size less than sizeRange)
  it("ScalaCheck property that takes 3 args, with sizeRange specified as param") {

    check(
      (a: String, b: String, c: String) => {
      a.length <= 5
      b.length <= 5
      c.length <= 5
      },
      sizeRange(5)
    ) 
  }

  // set sizeRange with default (ensure always passed with a size less than sizeRange)
  it("ScalaCheck property that takes 3 args, with sizeRange specified as default") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 5)

    check { (a: String, b: String, c: String) =>
      a.length <= 5
      b.length <= 5
      c.length <= 5
    }
  }

  // set sizeRange == 0 with (param, param) (ensure always passed with that size)
  it("ScalaCheck property that takes 3 args and generators, with sizeRange == 0, specified as (param, param)") {

    val prop = forAll (fiveFive, fiveFive, fiveFive) { (a: String, b: String, c: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
    }
    check(prop, minSize(5), sizeRange(0))
  }

  // set sizeRange == 0 with (param, default) (ensure always passed with that size)
  it("ScalaCheck property that takes 3 args and generators, with sizeRange == 0, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 0)

    val prop = forAll (fiveFive, fiveFive, fiveFive) { (a: String, b: String, c: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
    }
    check(prop, minSize(5))
  }

  // set sizeRange == 0 with (default, param) (ensure always passed with that size)
  it("ScalaCheck property that takes 3 args and generators, with sizeRange == 0, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5)

    val prop = forAll (fiveFive, fiveFive, fiveFive) { (a: String, b: String, c: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
    }
    check(prop, sizeRange(0))
  }

  // set sizeRange == 0 with (default, default) (ensure always passed with that size)
  it("ScalaCheck property that takes 3 args and generators, with sizeRange == 0, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5, sizeRange = 0)

    val prop = forAll (fiveFive, fiveFive, fiveFive) { (a: String, b: String, c: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
    }
    check(prop)
  }

  // set minSize to 7 and sizeRange to 4 with (param, param) (ensure always passed with that size)
  it("ScalaCheck property that takes 3 args and generators, with minSize to 7 and sizeRange to 4, specified as (param, param)") {

    val prop = forAll (sevenEleven, sevenEleven, sevenEleven) { (a: String, b: String, c: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
    }
    check(prop, minSize(7), sizeRange(4))
  }

  // set minSize to 7 and sizeRange to 4 with (param, default) (ensure always passed with that size)
  it("ScalaCheck property that takes 3 args and generators, with minSize to 7 and sizeRange to 4, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 4)

    val prop = forAll (sevenEleven, sevenEleven, sevenEleven) { (a: String, b: String, c: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
    }
    check(prop, minSize(7))
  }

  // set minSize to 7 and sizeRange to 4 with (default, param) (ensure always passed with that size)
  it("ScalaCheck property that takes 3 args and generators, with minSize to 7 and sizeRange to 4, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7)

    val prop = forAll (sevenEleven, sevenEleven, sevenEleven) { (a: String, b: String, c: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
    }
    check(prop, sizeRange(4))
  }

  // set minSize to 7 and sizeRange to 4 with (default, default) (ensure always passed with that size)
  it("ScalaCheck property that takes 3 args and generators, with minSize to 7 and sizeRange to 4, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7, sizeRange = 4)

    val prop = forAll (sevenEleven, sevenEleven, sevenEleven) { (a: String, b: String, c: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
    }
    check(prop)
  }
                              

  it("ScalaCheck property that takes 4 args, which succeeds") {

    check { (a: String, b: String, c: String, d: String) =>
      a.length + b.length + c.length + d.length == ((a + b + c + d).length)
    }
  }

  it("ScalaCheck property that takes 4 args, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      check { (a: String, b: String, c: String, d: String) =>
        a.length + b.length + c.length + d.length < 0
      }
    }
  }

  it("ScalaCheck property that takes 4 args and generators, which succeeds") {

    val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String) =>
      a.length + b.length + c.length + d.length == ((a + b + c + d).length)
    }
    check(prop)
  }

  it("ScalaCheck property that takes 4 args and generators, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String) =>
        a.length + b.length + c.length + d.length < 0
      }
      check(prop)
    }
  }

  // Same thing, but with config params
  it("ScalaCheck property that takes 4 args, which succeeds, with config params") {

    check(
      (a: String, b: String, c: String, d: String) => a.length + b.length + c.length + d.length == ((a + b + c + d).length),
      minSize(10),
      sizeRange(10)
    )
  }

  it("ScalaCheck property that takes 4 args, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      check(
        (a: String, b: String, c: String, d: String) => a.length + b.length + c.length + d.length < 0,
        minSize(10),
        sizeRange(10)
      )
    }
  }

  it("ScalaCheck property that takes 4 args and generators, which succeeds, with config params") {

    val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String) =>
      a.length + b.length + c.length + d.length == ((a + b + c + d).length)
    }
    check(prop, minSize(10), sizeRange(10))
  }

  it("ScalaCheck property that takes 4 args and generators, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String) =>
        a.length + b.length + c.length + d.length < 0
      }
      check(prop, minSize(10), sizeRange(10))
    }
  }

  // Same thing, but set minSuccessful to 5 with param, prop fails after 5
  it("ScalaCheck property that takes 4 args, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    check(
      (a: String, b: String, c: String, d: String) => {
        val res = i != 5
        i += 1
        res
      },
      minSuccessful(5)
    )
  }

  it("ScalaCheck property that takes 4 args, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      check(
        (a: String, b: String, c: String, d: String) => {
          val res = i != 4
          i += 1
        res
        },
        minSuccessful(5)
      ) 
    }
  }

  it("ScalaCheck property that takes 4 args and generators, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String) =>
      val res = i != 5
      i += 1
      res
    }
    check(prop, minSuccessful(5))
  }

  it("ScalaCheck property that takes 4 args and generators, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String) =>
        val res = i != 4
        i += 1
        res
      }
      check(prop, minSuccessful(5))
    }
  }

  // Same thing, but set default minSuccessful to 5, prop fails after 5
  it("ScalaCheck property that takes 4 args, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    check { (a: String, b: String, c: String, d: String) =>
      val res = i != 5
      i += 1
      res
    }
  }

  it("ScalaCheck property that takes 4 args, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      check { (a: String, b: String, c: String, d: String) =>
        val res = i != 4
        i += 1
        res
      }
    }
  }

  it("ScalaCheck property that takes 4 args and generators, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String) =>
      val res = i != 5
      i += 1
      res
    }
    check(prop)
  }

  it("ScalaCheck property that takes 4 args and generators, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String) =>
        val res = i != 4
        i += 1
        res
      }
      check(prop)
    }
  }

  // Same thing, but set maxDiscardedFactor to 0.6 with param, prop fails after 5
  it("ScalaCheck property that takes 4 args, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    check(
      (a: String, b: String, c: String, d: String) => {
        i += 1
        (i > 5) ==> { 1 + 1 == (2) }
      },
      maxDiscardedFactor(0.6)
    )
  }

  it("ScalaCheck property that takes 4 args, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      check(
        (a: String, b: String, c: String, d: String) => {
          i += 1
          (i > 7) ==> { 1 + 1 == (2) }
        },
        maxDiscardedFactor(0.6)
      ) 
    }
  }

  it("ScalaCheck property that takes 4 args and generators, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String) =>
      i += 1
      (i > 5) ==> { 1 + 1 == (2) }
    }
    check(prop, maxDiscardedFactor(0.6))
  }

  it("ScalaCheck property that takes 4 args and generators, which fails, with maxDiscardedFactor param set to 1.2") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String) =>
        i += 1
        (i > 7) ==> { 1 + 1 == (2) }
      }
      check(prop, maxDiscardedFactor(1.2))
    }
  }

  // Same thing, but set default maxDiscarded to 5, prop fails after 5
  it("ScalaCheck property that takes 4 args, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    check { (a: String, b: String, c: String, d: String) =>
      i += 1
      (i > 5) ==> { 1 + 1 == (2) }
    }
  }

  it("ScalaCheck property that takes 4 args, which fails, with default maxDiscardedFactor set to 1.2") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 1.2, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      check { (a: String, b: String, c: String, d: String) =>
        i += 1
        (i > 7) ==> { 1 + 1 == (2) }
      }
    }
  }

  it("ScalaCheck property that takes 4 args and generators, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String) =>
      i += 1
      (i > 5) ==> { 1 + 1 == (2) }
    }
    check(prop)
  }

  it("ScalaCheck property that takes 4 args and generators, which fails, with default maxDiscardedFactor set to 1.2") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 1.2, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String) =>
        i += 1
        (i > 7) ==> { 1 + 1 == (2) }
      }
      check(prop)
    }
  }

  // set sizeRange with param (ensure always passed with a size less than sizeRange)
  it("ScalaCheck property that takes 4 args, with sizeRange specified as param") {

    check(
      (a: String, b: String, c: String, d: String) => {
      a.length <= 5
      b.length <= 5
      c.length <= 5
      d.length <= 5
      },
      sizeRange(5)
    ) 
  }

  // set sizeRange with default (ensure always passed with a size less than sizeRange)
  it("ScalaCheck property that takes 4 args, with sizeRange specified as default") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 5)

    check { (a: String, b: String, c: String, d: String) =>
      a.length <= 5
      b.length <= 5
      c.length <= 5
      d.length <= 5
    }
  }

  // set sizeRange == 0 with (param, param) (ensure always passed with that size)
  it("ScalaCheck property that takes 4 args and generators, with sizeRange == 0, specified as (param, param)") {

    val prop = forAll (fiveFive, fiveFive, fiveFive, fiveFive) { (a: String, b: String, c: String, d: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
        d == ("OKAY")
    }
    check(prop, minSize(5), sizeRange(0))
  }

  // set sizeRange == 0 with (param, default) (ensure always passed with that size)
  it("ScalaCheck property that takes 4 args and generators, with sizeRange == 0, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 0)

    val prop = forAll (fiveFive, fiveFive, fiveFive, fiveFive) { (a: String, b: String, c: String, d: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
        d == ("OKAY")
    }
    check(prop, minSize(5))
  }

  // set sizeRange == 0 with (default, param) (ensure always passed with that size)
  it("ScalaCheck property that takes 4 args and generators, with sizeRange == 0, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5)

    val prop = forAll (fiveFive, fiveFive, fiveFive, fiveFive) { (a: String, b: String, c: String, d: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
        d == ("OKAY")
    }
    check(prop, sizeRange(0))
  }

  // set sizeRange == 0 with (default, default) (ensure always passed with that size)
  it("ScalaCheck property that takes 4 args and generators, with sizeRange == 0, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5, sizeRange = 0)

    val prop = forAll (fiveFive, fiveFive, fiveFive, fiveFive) { (a: String, b: String, c: String, d: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
        d == ("OKAY")
    }
    check(prop)
  }

  // set minSize to 7 and sizeRange to 4 with (param, param) (ensure always passed with that size)
  it("ScalaCheck property that takes 4 args and generators, with minSize to 7 and sizeRange to 4, specified as (param, param)") {

    val prop = forAll (sevenEleven, sevenEleven, sevenEleven, sevenEleven) { (a: String, b: String, c: String, d: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
        d == ("OKAY")
    }
    check(prop, minSize(7), sizeRange(4))
  }

  // set minSize to 7 and sizeRange to 4 with (param, default) (ensure always passed with that size)
  it("ScalaCheck property that takes 4 args and generators, with minSize to 7 and sizeRange to 4, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 4)

    val prop = forAll (sevenEleven, sevenEleven, sevenEleven, sevenEleven) { (a: String, b: String, c: String, d: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
        d == ("OKAY")
    }
    check(prop, minSize(7))
  }

  // set minSize to 7 and sizeRange to 4 with (default, param) (ensure always passed with that size)
  it("ScalaCheck property that takes 4 args and generators, with minSize to 7 and sizeRange to 4, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7)

    val prop = forAll (sevenEleven, sevenEleven, sevenEleven, sevenEleven) { (a: String, b: String, c: String, d: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
        d == ("OKAY")
    }
    check(prop, sizeRange(4))
  }

  // set minSize to 7 and sizeRange to 4 with (default, default) (ensure always passed with that size)
  it("ScalaCheck property that takes 4 args and generators, with minSize to 7 and sizeRange to 4, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7, sizeRange = 4)

    val prop = forAll (sevenEleven, sevenEleven, sevenEleven, sevenEleven) { (a: String, b: String, c: String, d: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
        d == ("OKAY")
    }
    check(prop)
  }
                              

  it("ScalaCheck property that takes 5 args, which succeeds") {

    check { (a: String, b: String, c: String, d: String, e: String) =>
      a.length + b.length + c.length + d.length + e.length == ((a + b + c + d + e).length)
    }
  }

  it("ScalaCheck property that takes 5 args, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      check { (a: String, b: String, c: String, d: String, e: String) =>
        a.length + b.length + c.length + d.length + e.length < 0
      }
    }
  }

  it("ScalaCheck property that takes 5 args and generators, which succeeds") {

    val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String) =>
      a.length + b.length + c.length + d.length + e.length == ((a + b + c + d + e).length)
    }
    check(prop)
  }

  it("ScalaCheck property that takes 5 args and generators, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String) =>
        a.length + b.length + c.length + d.length + e.length < 0
      }
      check(prop)
    }
  }

  // Same thing, but with config params
  it("ScalaCheck property that takes 5 args, which succeeds, with config params") {

    check(
      (a: String, b: String, c: String, d: String, e: String) => a.length + b.length + c.length + d.length + e.length == ((a + b + c + d + e).length),
      minSize(10),
      sizeRange(10)
    )
  }

  it("ScalaCheck property that takes 5 args, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      check(
        (a: String, b: String, c: String, d: String, e: String) => a.length + b.length + c.length + d.length + e.length < 0,
        minSize(10),
        sizeRange(10)
      )
    }
  }

  it("ScalaCheck property that takes 5 args and generators, which succeeds, with config params") {

    val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String) =>
      a.length + b.length + c.length + d.length + e.length == ((a + b + c + d + e).length)
    }
    check(prop, minSize(10), sizeRange(10))
  }

  it("ScalaCheck property that takes 5 args and generators, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String) =>
        a.length + b.length + c.length + d.length + e.length < 0
      }
      check(prop, minSize(10), sizeRange(10))
    }
  }

  // Same thing, but set minSuccessful to 5 with param, prop fails after 5
  it("ScalaCheck property that takes 5 args, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    check(
      (a: String, b: String, c: String, d: String, e: String) => {
        val res = i != 5
        i += 1
        res
      },
      minSuccessful(5)
    )
  }

  it("ScalaCheck property that takes 5 args, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      check(
        (a: String, b: String, c: String, d: String, e: String) => {
          val res = i != 4
          i += 1
        res
        },
        minSuccessful(5)
      ) 
    }
  }

  it("ScalaCheck property that takes 5 args and generators, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String) =>
      val res = i != 5
      i += 1
      res
    }
    check(prop, minSuccessful(5))
  }

  it("ScalaCheck property that takes 5 args and generators, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String) =>
        val res = i != 4
        i += 1
        res
      }
      check(prop, minSuccessful(5))
    }
  }

  // Same thing, but set default minSuccessful to 5, prop fails after 5
  it("ScalaCheck property that takes 5 args, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    check { (a: String, b: String, c: String, d: String, e: String) =>
      val res = i != 5
      i += 1
      res
    }
  }

  it("ScalaCheck property that takes 5 args, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      check { (a: String, b: String, c: String, d: String, e: String) =>
        val res = i != 4
        i += 1
        res
      }
    }
  }

  it("ScalaCheck property that takes 5 args and generators, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String) =>
      val res = i != 5
      i += 1
      res
    }
    check(prop)
  }

  it("ScalaCheck property that takes 5 args and generators, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String) =>
        val res = i != 4
        i += 1
        res
      }
      check(prop)
    }
  }

  // Same thing, but set maxDiscardedFactor to 0.6 with param, prop fails after 5
  it("ScalaCheck property that takes 5 args, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    check(
      (a: String, b: String, c: String, d: String, e: String) => {
        i += 1
        (i > 5) ==> { 1 + 1 == (2) }
      },
      maxDiscardedFactor(0.6)
    )
  }

  it("ScalaCheck property that takes 5 args, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      check(
        (a: String, b: String, c: String, d: String, e: String) => {
          i += 1
          (i > 7) ==> { 1 + 1 == (2) }
        },
        maxDiscardedFactor(0.6)
      ) 
    }
  }

  it("ScalaCheck property that takes 5 args and generators, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String) =>
      i += 1
      (i > 5) ==> { 1 + 1 == (2) }
    }
    check(prop, maxDiscardedFactor(0.6))
  }

  it("ScalaCheck property that takes 5 args and generators, which fails, with maxDiscardedFactor param set to 1.2") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String) =>
        i += 1
        (i > 7) ==> { 1 + 1 == (2) }
      }
      check(prop, maxDiscardedFactor(1.2))
    }
  }

  // Same thing, but set default maxDiscarded to 5, prop fails after 5
  it("ScalaCheck property that takes 5 args, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    check { (a: String, b: String, c: String, d: String, e: String) =>
      i += 1
      (i > 5) ==> { 1 + 1 == (2) }
    }
  }

  it("ScalaCheck property that takes 5 args, which fails, with default maxDiscardedFactor set to 1.2") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 1.2, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      check { (a: String, b: String, c: String, d: String, e: String) =>
        i += 1
        (i > 7) ==> { 1 + 1 == (2) }
      }
    }
  }

  it("ScalaCheck property that takes 5 args and generators, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String) =>
      i += 1
      (i > 5) ==> { 1 + 1 == (2) }
    }
    check(prop)
  }

  it("ScalaCheck property that takes 5 args and generators, which fails, with default maxDiscardedFactor set to 1.2") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 1.2, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String) =>
        i += 1
        (i > 7) ==> { 1 + 1 == (2) }
      }
      check(prop)
    }
  }

  // set sizeRange with param (ensure always passed with a size less than sizeRange)
  it("ScalaCheck property that takes 5 args, with sizeRange specified as param") {

    check(
      (a: String, b: String, c: String, d: String, e: String) => {
      a.length <= 5
      b.length <= 5
      c.length <= 5
      d.length <= 5
      e.length <= 5
      },
      sizeRange(5)
    ) 
  }

  // set sizeRange with default (ensure always passed with a size less than sizeRange)
  it("ScalaCheck property that takes 5 args, with sizeRange specified as default") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 5)

    check { (a: String, b: String, c: String, d: String, e: String) =>
      a.length <= 5
      b.length <= 5
      c.length <= 5
      d.length <= 5
      e.length <= 5
    }
  }

  // set sizeRange == 0 with (param, param) (ensure always passed with that size)
  it("ScalaCheck property that takes 5 args and generators, with sizeRange == 0, specified as (param, param)") {

    val prop = forAll (fiveFive, fiveFive, fiveFive, fiveFive, fiveFive) { (a: String, b: String, c: String, d: String, e: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
        d == ("OKAY")
        e == ("OKAY")
    }
    check(prop, minSize(5), sizeRange(0))
  }

  // set sizeRange == 0 with (param, default) (ensure always passed with that size)
  it("ScalaCheck property that takes 5 args and generators, with sizeRange == 0, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 0)

    val prop = forAll (fiveFive, fiveFive, fiveFive, fiveFive, fiveFive) { (a: String, b: String, c: String, d: String, e: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
        d == ("OKAY")
        e == ("OKAY")
    }
    check(prop, minSize(5))
  }

  // set sizeRange == 0 with (default, param) (ensure always passed with that size)
  it("ScalaCheck property that takes 5 args and generators, with sizeRange == 0, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5)

    val prop = forAll (fiveFive, fiveFive, fiveFive, fiveFive, fiveFive) { (a: String, b: String, c: String, d: String, e: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
        d == ("OKAY")
        e == ("OKAY")
    }
    check(prop, sizeRange(0))
  }

  // set sizeRange == 0 with (default, default) (ensure always passed with that size)
  it("ScalaCheck property that takes 5 args and generators, with sizeRange == 0, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5, sizeRange = 0)

    val prop = forAll (fiveFive, fiveFive, fiveFive, fiveFive, fiveFive) { (a: String, b: String, c: String, d: String, e: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
        d == ("OKAY")
        e == ("OKAY")
    }
    check(prop)
  }

  // set minSize to 7 and sizeRange to 4 with (param, param) (ensure always passed with that size)
  it("ScalaCheck property that takes 5 args and generators, with minSize to 7 and sizeRange to 4, specified as (param, param)") {

    val prop = forAll (sevenEleven, sevenEleven, sevenEleven, sevenEleven, sevenEleven) { (a: String, b: String, c: String, d: String, e: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
        d == ("OKAY")
        e == ("OKAY")
    }
    check(prop, minSize(7), sizeRange(4))
  }

  // set minSize to 7 and sizeRange to 4 with (param, default) (ensure always passed with that size)
  it("ScalaCheck property that takes 5 args and generators, with minSize to 7 and sizeRange to 4, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 4)

    val prop = forAll (sevenEleven, sevenEleven, sevenEleven, sevenEleven, sevenEleven) { (a: String, b: String, c: String, d: String, e: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
        d == ("OKAY")
        e == ("OKAY")
    }
    check(prop, minSize(7))
  }

  // set minSize to 7 and sizeRange to 4 with (default, param) (ensure always passed with that size)
  it("ScalaCheck property that takes 5 args and generators, with minSize to 7 and sizeRange to 4, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7)

    val prop = forAll (sevenEleven, sevenEleven, sevenEleven, sevenEleven, sevenEleven) { (a: String, b: String, c: String, d: String, e: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
        d == ("OKAY")
        e == ("OKAY")
    }
    check(prop, sizeRange(4))
  }

  // set minSize to 7 and sizeRange to 4 with (default, default) (ensure always passed with that size)
  it("ScalaCheck property that takes 5 args and generators, with minSize to 7 and sizeRange to 4, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7, sizeRange = 4)

    val prop = forAll (sevenEleven, sevenEleven, sevenEleven, sevenEleven, sevenEleven) { (a: String, b: String, c: String, d: String, e: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
        d == ("OKAY")
        e == ("OKAY")
    }
    check(prop)
  }
                              

  it("ScalaCheck property that takes 6 args, which succeeds") {

    check { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      a.length + b.length + c.length + d.length + e.length + f.length == ((a + b + c + d + e + f).length)
    }
  }

  it("ScalaCheck property that takes 6 args, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      check { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        a.length + b.length + c.length + d.length + e.length + f.length < 0
      }
    }
  }

  it("ScalaCheck property that takes 6 args and generators, which succeeds") {

    val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      a.length + b.length + c.length + d.length + e.length + f.length == ((a + b + c + d + e + f).length)
    }
    check(prop)
  }

  it("ScalaCheck property that takes 6 args and generators, which fails") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        a.length + b.length + c.length + d.length + e.length + f.length < 0
      }
      check(prop)
    }
  }

  // Same thing, but with config params
  it("ScalaCheck property that takes 6 args, which succeeds, with config params") {

    check(
      (a: String, b: String, c: String, d: String, e: String, f: String) => a.length + b.length + c.length + d.length + e.length + f.length == ((a + b + c + d + e + f).length),
      minSize(10),
      sizeRange(10)
    )
  }

  it("ScalaCheck property that takes 6 args, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      check(
        (a: String, b: String, c: String, d: String, e: String, f: String) => a.length + b.length + c.length + d.length + e.length + f.length < 0,
        minSize(10),
        sizeRange(10)
      )
    }
  }

  it("ScalaCheck property that takes 6 args and generators, which succeeds, with config params") {

    val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      a.length + b.length + c.length + d.length + e.length + f.length == ((a + b + c + d + e + f).length)
    }
    check(prop, minSize(10), sizeRange(10))
  }

  it("ScalaCheck property that takes 6 args and generators, which fails, with config params") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        a.length + b.length + c.length + d.length + e.length + f.length < 0
      }
      check(prop, minSize(10), sizeRange(10))
    }
  }

  // Same thing, but set minSuccessful to 5 with param, prop fails after 5
  it("ScalaCheck property that takes 6 args, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    check(
      (a: String, b: String, c: String, d: String, e: String, f: String) => {
        val res = i != 5
        i += 1
        res
      },
      minSuccessful(5)
    )
  }

  it("ScalaCheck property that takes 6 args, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      check(
        (a: String, b: String, c: String, d: String, e: String, f: String) => {
          val res = i != 4
          i += 1
        res
        },
        minSuccessful(5)
      ) 
    }
  }

  it("ScalaCheck property that takes 6 args and generators, which succeeds, with minSuccessful param set to 5") {

    var i = 0
    val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      val res = i != 5
      i += 1
      res
    }
    check(prop, minSuccessful(5))
  }

  it("ScalaCheck property that takes 6 args and generators, which fails, with minSuccessful param set to 5") {

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        val res = i != 4
        i += 1
        res
      }
      check(prop, minSuccessful(5))
    }
  }

  // Same thing, but set default minSuccessful to 5, prop fails after 5
  it("ScalaCheck property that takes 6 args, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    check { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      val res = i != 5
      i += 1
      res
    }
  }

  it("ScalaCheck property that takes 6 args, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      check { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        val res = i != 4
        i += 1
        res
      }
    }
  }

  it("ScalaCheck property that takes 6 args and generators, which succeeds, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    var i = 0
    val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      val res = i != 5
      i += 1
      res
    }
    check(prop)
  }

  it("ScalaCheck property that takes 6 args and generators, which fails, with default minSuccessful param set to 5") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        val res = i != 4
        i += 1
        res
      }
      check(prop)
    }
  }

  // Same thing, but set maxDiscardedFactor to 0.6 with param, prop fails after 5
  it("ScalaCheck property that takes 6 args, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    check(
      (a: String, b: String, c: String, d: String, e: String, f: String) => {
        i += 1
        (i > 5) ==> { 1 + 1 == (2) }
      },
      maxDiscardedFactor(0.6)
    )
  }

  it("ScalaCheck property that takes 6 args, which fails, with maxDiscardedFactor param set to 0.6") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      check(
        (a: String, b: String, c: String, d: String, e: String, f: String) => {
          i += 1
          (i > 7) ==> { 1 + 1 == (2) }
        },
        maxDiscardedFactor(0.6)
      ) 
    }
  }

  it("ScalaCheck property that takes 6 args and generators, which succeeds, with maxDiscardedFactor param set to 0.6") {

    var i = 0
    val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      i += 1
      (i > 5) ==> { 1 + 1 == (2) }
    }
    check(prop, maxDiscardedFactor(0.6))
  }

  it("ScalaCheck property that takes 6 args and generators, which fails, with maxDiscardedFactor param set to 1.2") {

    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        i += 1
        (i > 7) ==> { 1 + 1 == (2) }
      }
      check(prop, maxDiscardedFactor(1.2))
    }
  }

  // Same thing, but set default maxDiscarded to 5, prop fails after 5
  it("ScalaCheck property that takes 6 args, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    check { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      i += 1
      (i > 5) ==> { 1 + 1 == (2) }
    }
  }

  it("ScalaCheck property that takes 6 args, which fails, with default maxDiscardedFactor set to 1.2") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 1.2, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      check { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        i += 1
        (i > 7) ==> { 1 + 1 == (2) }
      }
    }
  }

  it("ScalaCheck property that takes 6 args and generators, which succeeds, with default maxDiscardedFactor set to 0.6") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 0.6)

    var i = 0
    val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      i += 1
      (i > 5) ==> { 1 + 1 == (2) }
    }
    check(prop)
  }

  it("ScalaCheck property that takes 6 args and generators, which fails, with default maxDiscardedFactor set to 1.2") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(maxDiscardedFactor = 1.2, minSuccessful = 5)

    intercept[GeneratorDrivenPropertyCheckFailedException] {
      var i = 0
      val prop = forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        i += 1
        (i > 7) ==> { 1 + 1 == (2) }
      }
      check(prop)
    }
  }

  // set sizeRange with param (ensure always passed with a size less than sizeRange)
  it("ScalaCheck property that takes 6 args, with sizeRange specified as param") {

    check(
      (a: String, b: String, c: String, d: String, e: String, f: String) => {
      a.length <= 5
      b.length <= 5
      c.length <= 5
      d.length <= 5
      e.length <= 5
      f.length <= 5
      },
      sizeRange(5)
    ) 
  }

  // set sizeRange with default (ensure always passed with a size less than sizeRange)
  it("ScalaCheck property that takes 6 args, with sizeRange specified as default") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 5)

    check { (a: String, b: String, c: String, d: String, e: String, f: String) =>
      a.length <= 5
      b.length <= 5
      c.length <= 5
      d.length <= 5
      e.length <= 5
      f.length <= 5
    }
  }

  // set sizeRange == 0 with (param, param) (ensure always passed with that size)
  it("ScalaCheck property that takes 6 args and generators, with sizeRange == 0, specified as (param, param)") {

    val prop = forAll (fiveFive, fiveFive, fiveFive, fiveFive, fiveFive, fiveFive) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
        d == ("OKAY")
        e == ("OKAY")
        f == ("OKAY")
    }
    check(prop, minSize(5), sizeRange(0))
  }

  // set sizeRange == 0 with (param, default) (ensure always passed with that size)
  it("ScalaCheck property that takes 6 args and generators, with sizeRange == 0, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 0)

    val prop = forAll (fiveFive, fiveFive, fiveFive, fiveFive, fiveFive, fiveFive) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
        d == ("OKAY")
        e == ("OKAY")
        f == ("OKAY")
    }
    check(prop, minSize(5))
  }

  // set sizeRange == 0 with (default, param) (ensure always passed with that size)
  it("ScalaCheck property that takes 6 args and generators, with sizeRange == 0, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5)

    val prop = forAll (fiveFive, fiveFive, fiveFive, fiveFive, fiveFive, fiveFive) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
        d == ("OKAY")
        e == ("OKAY")
        f == ("OKAY")
    }
    check(prop, sizeRange(0))
  }

  // set sizeRange == 0 with (default, default) (ensure always passed with that size)
  it("ScalaCheck property that takes 6 args and generators, with sizeRange == 0, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 5, sizeRange = 0)

    val prop = forAll (fiveFive, fiveFive, fiveFive, fiveFive, fiveFive, fiveFive) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
        d == ("OKAY")
        e == ("OKAY")
        f == ("OKAY")
    }
    check(prop)
  }

  // set minSize to 7 and sizeRange to 4 with (param, param) (ensure always passed with that size)
  it("ScalaCheck property that takes 6 args and generators, with minSize to 7 and sizeRange to 4, specified as (param, param)") {

    val prop = forAll (sevenEleven, sevenEleven, sevenEleven, sevenEleven, sevenEleven, sevenEleven) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
        d == ("OKAY")
        e == ("OKAY")
        f == ("OKAY")
    }
    check(prop, minSize(7), sizeRange(4))
  }

  // set minSize to 7 and sizeRange to 4 with (param, default) (ensure always passed with that size)
  it("ScalaCheck property that takes 6 args and generators, with minSize to 7 and sizeRange to 4, specified as (param, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(sizeRange = 4)

    val prop = forAll (sevenEleven, sevenEleven, sevenEleven, sevenEleven, sevenEleven, sevenEleven) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
        d == ("OKAY")
        e == ("OKAY")
        f == ("OKAY")
    }
    check(prop, minSize(7))
  }

  // set minSize to 7 and sizeRange to 4 with (default, param) (ensure always passed with that size)
  it("ScalaCheck property that takes 6 args and generators, with minSize to 7 and sizeRange to 4, specified as (default, param)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7)

    val prop = forAll (sevenEleven, sevenEleven, sevenEleven, sevenEleven, sevenEleven, sevenEleven) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
        d == ("OKAY")
        e == ("OKAY")
        f == ("OKAY")
    }
    check(prop, sizeRange(4))
  }

  // set minSize to 7 and sizeRange to 4 with (default, default) (ensure always passed with that size)
  it("ScalaCheck property that takes 6 args and generators, with minSize to 7 and sizeRange to 4, specified as (default, default)") {

    // Hides the member
    implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSize = 7, sizeRange = 4)

    val prop = forAll (sevenEleven, sevenEleven, sevenEleven, sevenEleven, sevenEleven, sevenEleven) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
        a == ("OKAY")
        b == ("OKAY")
        c == ("OKAY")
        d == ("OKAY")
        e == ("OKAY")
        f == ("OKAY")
    }
    check(prop)
  }
                              }
