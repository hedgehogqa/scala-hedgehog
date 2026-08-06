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
package hegehog.scalatest

import hedgehog.core.{PropertyConfig, PropertyT}
import hedgehog.{Gen, Property, Result, forTupled}
import org.scalactic.source.Position
import org.scalatest.Assertion
import org.scalatest.exceptions.DiscardedEvaluationException
import org.scalatest.prop.Whenever

/**
 * Trait containing methods that faciliate property checks against generated data.
 *
 * This trait contains `forAll` methods that provide various ways to check properties using
 * generated data. Use of this trait requires that Hedgehog be on the class path when you compile
 * and run your tests.
 *
 * For an example of trait [[HedgehogDrivenPropertyChecks]] in action, imagine you want to test this
 * `Fraction` class:
 *
 * {{{
 * class Fraction(n: Int, d: Int) {
 *  require(d != 0)
 *  require(d != Integer.MIN_VALUE)
 *  require(n != Integer.MIN_VALUE)
 *
 *  val numer = if (d < 0) -1 * n else n
 *  val denom = d.abs
 *
 *  override def toString = s"$numer / $denom"
 * }
 * }}}
 *
 * To test the behavior of `Fraction`, you could mix in or import the members of
 * `ScalaCheckDrivenPropertyChecks` (and `Matchers`) and check a property using a `forAll` method,
 * like this:
 *
 * {{{
 * val allIntegers = Gen.int(hedgehog.Range.linear(Integer.MIN_VALUE, Integer.MAX_VALUE))
 * forAll(allIntegers, allIntegers) { (n, d) =>
 *   whenever(d != 0 && d != Integer.MIN_VALUE && n != Integer.MIN_VALUE) {
 *     val f = new Fraction(n, d)
 *     if (n < 0 && d < 0 || n > 0 && d > 0) f.numer should be > 0
 *     else if (n != 0) f.numer should be < 0
 *     else f.numer should equal(0)
 *
 *     f.denom should be > 0
 *    }
 *  }
 * }}}
 *
 * Trait `ScalaCheckDrivenPropertyChecks` provides overloaded `forAll` methods that allow you to
 * check properties using the data provided by a Hedgehog generator. The `forAll` method takes three
 * parameter lists, the last being implicit. The first parameter list contains a varying degree of
 * generators, or properties that need to be evaluated. The second parameter list contains the
 * property function to be checked, which takes as many parameters as there are generators or
 * properties in the first parameter list. The third parameter list contains an implicit
 * [[PropertyConfig]] object that provides configuration parameters for the property check, and an
 * implicit `Position` object that provides information about the source code position of the
 * `forAll` invocation.
 *
 * The `forAll` methods use the supplied generators to generate example arguments and pass them to
 * the property function, and generate a
 * [[org.scalatest.exceptions.GeneratorDrivenPropertyCheckFailedException]] if the function
 * completes abruptly for any exception that would normally cause a test to fail in ScalaTest other
 * than [[org.scalatest.exceptions.DiscardedEvaluationException]]. A `DiscardedEvaluationException`,
 * which is thrown by the `whenever` method (defined in trait [[org.scalatest.prop.Whenever]], which
 * this trait extends) to indicate a condition required by the property function is not met by a row
 * of passed data, will simply cause `forAll` to discard that row of data.
 *
 * <a name="supplyingGenerators"></a><h2>Supplying generators</h2>
 *
 * Hedgehog provides a nice library of compositors that makes it easy to create your own custom
 * generators. To supply generators to a property check, place them in parentheses after `forAll`,
 * before the property check function.
 *
 * For example, to create a generator of even integers between (and including) -2000 and 2000, you
 * could write this:
 * {{{
 * import hedgehog.Gen
 *
 * val eventInts = for (n <- Gen.int(hedgehog.Range.linear(-1000, 1000))) yield 2 * n
 * }}}
 *
 * Given this generator, you could use it on a property check like this:
 * {{{forAll (eventInts) { n => n % 2 should equal (0) }}}}
 *
 * Custom generators are necessary when you want to pass data types not supported by Hedgehog's
 * provided generators, but are also useful when some of the values in the full range for the passed
 * types are not valid. For such values you could use a `whenever` clause or use the filtering
 * mechanisms from Hedgehog itself. In the `Fraction` class shown above, neither the passed
 * numerator or denominator can be `Integer.MIN_VALUE`, and the passed denominator cannot be zero.
 * This shows up in the `whenever` clause like this:
 * {{{whenever (d != 0 && d != Integer.MIN_VALUE && n != Integer.MIN_VALUE) { ... }}}}
 *
 * You could in addition define generators for the numerator and denominator that only produce valid
 * values, like this:
 *
 * {{{
 * val validNumers = Gen.int(hedgehog.Range.linear(Integer.MIN_VALUE + 1, Integer.MAX_VALUE))
 * val validDenoms = validNumers.filter(_ != 0)
 * }}}
 *
 * You could then use them in the property check like this:
 *
 * {{{
 * forAll(validNumers, validDenoms) { (n, d) =>
 *   val f = new Fraction(n, d)
 *
 *   if (n < 0 && d < 0 || n > 0 && d > 0) f.numer should be > 0
 *   else if (n != 0) f.numer should be < 0
 *   else f.numer should equal(0)
 *
 *   f.denom should be > 0
 * }
 * }}}
 *
 * <a name="propCheckConfig"></a><h2>Property check configuration</h2>
 *
 * The property checks performed by the `forAll` methods of this trait can be flexibly configured
 * via implicitly passing a [[PropertyConfig]] object to the `forAll` method.
 *
 * @define forAllProperties
 *   Performs a property check by applying the specified property test function to arguments
 *   supplied by the passed properties.
 * @define forAllGenerators
 *   Performs a property check by applying the specified property test function to arguments
 *   supplied by the passed generators.
 * @define test
 *   The property test function to apply to the generated arguments.
 */
trait HedgehogDrivenPropertyChecks extends Whenever with HedgehogSupport {

  /** Convert a Scalatest test which works by throwing exceptions to a Hedgehog Property */
  private def scalaTestToHedgehog[A](propertyA: PropertyT[A], test: A => Any): Property =
    propertyA.flatMap { a =>
      try {
        test(a)
        Property.point(Result.success)
      } catch {
        // todo: catch even more exceptions, maybe some exceptions should not be caught, see Scalatest implementation
        case _: DiscardedEvaluationException => Property.discard[Result]
        case e: Exception => Property.error(e)
      }
    }

  /**
   * $forAllProperties
   *
   * @param test
   *   $test
   */
  def forAll[A](propertyA: PropertyT[A])(test: A => Any)(implicit
      config: PropertyConfig,
      pos: Position
  ): Assertion = check(scalaTestToHedgehog(propertyA, test))

  /**
   * $forAllGenerators
   *
   * @param test
   *   $test
   */
  def forAll[A](genA: Gen[A])(test: A => Any)(implicit
      config: PropertyConfig,
      pos: Position
  ): Assertion = forAll(genA.forAll)(test)(config, pos)

  /**
   * $forAllProperties
   *
   * @param test
   *   $test
   */
  def forAll[A, B](propertyA: PropertyT[A], propertyB: PropertyT[B])(
      test: (A, B) => Any
  )(implicit
      config: PropertyConfig,
      pos: Position
  ): Assertion = forAll(forTupled(propertyA, propertyB))(test.tupled(_))

  /**
   * $forAllGenerators
   *
   * @param test
   *   $test
   */
  def forAll[A, B](genA: Gen[A], genB: Gen[B])(test: (A, B) => Any)(implicit
      config: PropertyConfig,
      pos: Position
  ): Assertion = forAll(forTupled(genA, genB).forAll)(test.tupled(_))(config, pos)

  /**
   * $forAllProperties
   *
   * @param test
   *   $test
   */
  def forAll[A, B, C](
      propertyA: PropertyT[A],
      propertyB: PropertyT[B],
      propertyC: PropertyT[C]
  )(
      test: (A, B, C) => Any
  )(implicit
      config: PropertyConfig,
      pos: Position
  ): Assertion =
    forAll(forTupled(propertyA, propertyB, propertyC))(test.tupled(_))

  /**
   * $forAllGenerators
   *
   * @param test
   *   $test
   */
  def forAll[A, B, C](genA: Gen[A], genB: Gen[B], genC: Gen[C])(
      test: (A, B, C) => Any
  )(implicit
      config: PropertyConfig,
      pos: Position
  ): Assertion = forAll(forTupled(genA, genB, genC))(test.tupled(_))

  /**
   * $forAllProperties
   *
   * @param test
   *   $test
   */
  def forAll[A, B, C, D](
      propertyA: PropertyT[A],
      propertyB: PropertyT[B],
      propertyC: PropertyT[C],
      propertyD: PropertyT[D]
  )(test: (A, B, C, D) => Any)(implicit
      config: PropertyConfig,
      pos: Position
  ): Assertion = forAll(forTupled(propertyA, propertyB, propertyC, propertyD))(test.tupled(_))

  /**
   * $forAllGenerators
   *
   * @param test
   *   $test
   */
  def forAll[A, B, C, D](
      genA: Gen[A],
      genB: Gen[B],
      genC: Gen[C],
      genD: Gen[D]
  )(test: (A, B, C, D) => Any)(implicit
      config: PropertyConfig,
      pos: Position
  ): Assertion = forAll(forTupled(genA, genB, genC, genD))(test.tupled(_))

  /**
   * $forAllProperties
   *
   * @param test
   *   $test
   */
  def forAll[A, B, C, D, E](
      propertyA: PropertyT[A],
      propertyB: PropertyT[B],
      propertyC: PropertyT[C],
      propertyD: PropertyT[D],
      propertyE: PropertyT[E]
  )(test: (A, B, C, D, E) => Any)(implicit
      config: PropertyConfig = PropertyConfig.default,
      pos: Position
  ): Assertion =
    forAll(forTupled(propertyA, propertyB, propertyC, propertyD, propertyE))(test.tupled(_))

  /**
   * $forAllGenerators
   *
   * @param test
   *   $test
   */
  def forAll[A, B, C, D, E](
      genA: Gen[A],
      genB: Gen[B],
      genC: Gen[C],
      genD: Gen[D],
      genE: Gen[E]
  )(test: (A, B, C, D, E) => Any)(implicit
      config: PropertyConfig,
      pos: Position
  ): Assertion =
    forAll(forTupled(genA, genB, genC, genD, genE))(test.tupled(_))

  /**
   * $forAllProperties
   *
   * @param test
   *   $test
   */
  def forAll[A, B, C, D, E, F](
      propertyA: PropertyT[A],
      propertyB: PropertyT[B],
      propertyC: PropertyT[C],
      propertyD: PropertyT[D],
      propertyE: PropertyT[E],
      propertyF: PropertyT[F]
  )(test: (A, B, C, D, E, F) => Any)(implicit
      config: PropertyConfig,
      pos: Position
  ): Assertion =
    forAll(forTupled(propertyA, propertyB, propertyC, propertyD, propertyE, propertyF))(
      test.tupled(_)
    )

  /**
   * $forAllGenerators
   *
   * @param test
   *   $test
   */
  def forAll[A, B, C, D, E, F](
      genA: Gen[A],
      genB: Gen[B],
      genC: Gen[C],
      genD: Gen[D],
      genE: Gen[E],
      genF: Gen[F]
  )(test: (A, B, C, D, E, F) => Any)(implicit
      config: PropertyConfig,
      pos: Position
  ): Assertion =
    forAll(forTupled(genA, genB, genC, genD, genE, genF))(
      test.tupled(_)
    )
}

object HedgehogDrivenPropertyChecks extends HedgehogDrivenPropertyChecks
