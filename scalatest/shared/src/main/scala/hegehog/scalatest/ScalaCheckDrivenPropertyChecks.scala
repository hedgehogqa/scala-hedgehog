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
import hedgehog.{Gen, Result}
import org.scalactic.source.Position

/**
 * Trait containing methods that faciliate property checks against generated data.
 *
 * <p> This trait contains <code>forAll</code> methods that provide various ways to check properties
 * using generated data. Use of this trait requires that ScalaCheck be on the class path when you
 * compile and run your tests. It also contains a <code>wherever</code> method that can be used to
 * indicate a property need only hold whenever some condition is true. </p>
 *
 * <p> For an example of trait <code>ScalaCheckDrivenPropertyChecks</code> in action, imagine you
 * want to test this <code>Fraction</code> class: </p>
 *
 * <pre class="stHighlight"> class Fraction(n: Int, d: Int) {
 *
 * require(d != 0) require(d != Integer.MIN_VALUE) require(n != Integer.MIN_VALUE)
 *
 * val numer = if (d < 0) -1 * n else n val denom = d.abs
 *
 * override def toString = numer + " / " + denom } </pre>
 *
 * <p> To test the behavior of <code>Fraction</code>, you could mix in or import the members of
 * <code>ScalaCheckDrivenPropertyChecks</code> (and <code>Matchers</code>) and check a property
 * using a <code>forAll</code> method, like this: </p>
 *
 * <pre class="stHighlight"> forAll { (n: Int, d: Int) =>
 *
 * whenever (d != 0 && d != Integer.MIN_VALUE && n != Integer.MIN_VALUE) {
 *
 * val f = new Fraction(n, d)
 *
 * if (n < 0 && d < 0 || n > 0 && d > 0) f.numer should be > 0 else if (n != 0) f.numer should be <
 * 0 else f.numer should be === 0
 *
 * f.denom should be > 0 } } </pre>
 *
 * <p> Trait <code>ScalaCheckDrivenPropertyChecks</code> provides overloaded <code>forAll</code>
 * methods that allow you to check properties using the data provided by a ScalaCheck generator. The
 * simplest form of <code>forAll</code> method takes two parameter lists, the second of which is
 * implicit. The first parameter list is a "property" function with one to six parameters. An
 * implicit <code>Arbitrary</code> generator and <code>Shrink</code> object needs to be supplied for
 * The <code>forAll</code> method will pass each row of data to each parameter type. ScalaCheck
 * provides many implicit <code>Arbitrary</code> generators for common types such as
 * <code>Int</code>, <code>String</code>, <code>List[Float]</code>, <em>etc.</em>, in its
 * <code>org.scalacheck.Arbitrary</code> companion object. So long as you use types for which
 * ScalaCheck already provides implicit <code>Arbitrary</code> generators, you needn't worry about
 * them. Same for <code>Shrink</code> objects, which are provided by ScalaCheck's
 * <code>org.scalacheck.Shrink</code> companion object. Most often you can simply pass a property
 * function to <code>forAll</code>, and the compiler will grab the implicit values provided by
 * ScalaCheck. </p>
 *
 * <p> The <code>forAll</code> methods use the supplied <code>Arbitrary</code> generators to
 * generate example arguments and pass them to the property function, and generate a
 * <code>ScalaCheckDrivenPropertyCheckFailedException</code> if the function completes abruptly for
 * any exception that would <a href="../Suite.html#errorHandling">normally cause</a> a test to fail
 * in ScalaTest other than <code>DiscardedEvaluationException</code>. An
 * <code>DiscardedEvaluationException</code>, which is thrown by the <code>whenever</code> method
 * (defined in trait <code>Whenever</code>, which this trait extends) to indicate a condition
 * required by the property function is not met by a row of passed data, will simply cause
 * <code>forAll</code> to discard that row of data. </p>
 *
 * <a name="supplyingArgumentNames"></a><h2>Supplying argument names</h2>
 *
 * <p> You can optionally specify string names for the arguments passed to a property function,
 * which will be used in any error message when describing the argument values that caused the
 * failure. To supply the names, place them in a comma separated list in parentheses after
 * <code>forAll</code> before the property function (a curried form of <code>forAll</code>). Here's
 * an example: </p>
 *
 * <pre class="stHighlight"> forAll ("a", "b") { (a: String, b: String) => a.length + b.length
 * should equal ((a + b).length + 1) // Should fail } </pre>
 *
 * <p> When this fails, you'll see an error message that includes this: </p>
 *
 * <pre> Occurred when passed generated values ( a = "", b = "" ) </pre>
 *
 * <p> When you don't supply argument names, the error message will say <code>arg0</code>,
 * <code>arg1</code>, <em>etc.</em>. For example, this property check: </p>
 *
 * <pre class="stHighlight"> forAll { (a: String, b: String) => a.length + b.length should equal ((a
 * + b).length + 1) // Should fail } </pre>
 *
 * <p> Will fail with an error message that includes: </p>
 *
 * <pre> Occurred when passed generated values ( arg0 = "", arg1 = "" ) </pre>
 *
 * <a name="supplyingGenerators"></a><h2>Supplying generators</h2>
 *
 * <p> ScalaCheck provides a nice library of compositors that makes it easy to create your own
 * custom generators. If you want to supply custom generators to a property check, place them in
 * parentheses after <code>forAll</code>, before the property check function (a curried form of
 * <code>forAll</code>). </p>
 *
 * <p> For example, to create a generator of even integers between (and including) -2000 and 2000,
 * you could write this: </p>
 *
 * <pre class="stHighlight"> import org.scalacheck.Gen
 *
 * val evenInts = for (n <- Gen.choose(-1000, 1000)) yield 2 * n </pre>
 *
 * <p> Given this generator, you could use it on a property check like this: </p>
 *
 * <pre class="stHighlight"> forAll (evenInts) { (n) => n % 2 should equal (0) } </pre>
 *
 * <p> Custom generators are necessary when you want to pass data types not supported by
 * ScalaCheck's arbitrary generators, but are also useful when some of the values in the full range
 * for the passed types are not valid. For such values you would use a <code>whenever</code> clause.
 * In the <code>Fraction</code> class shown above, neither the passed numerator or denominator can
 * be <code>Integer.MIN_VALUE</code>, and the passed denominator cannot be zero. This shows up in
 * the <code>whenever</code> clause like this: </p>
 *
 * <pre class="stHighlight"> whenever (d != 0 && d != Integer.MIN_VALUE && n != Integer.MIN_VALUE) {
 * ... </pre>
 *
 * <p> You could in addition define generators for the numerator and denominator that only produce
 * valid values, like this: </p>
 *
 * <pre class="stHighlight"> val validNumers = for (n <- Gen.choose(Integer.MIN_VALUE + 1,
 * Integer.MAX_VALUE)) yield n val validDenoms = for (d <- validNumers if d != 0) yield d </pre>
 *
 * <p> You could then use them in the property check like this: </p>
 *
 * <pre class="stHighlight"> forAll (validNumers, validDenoms) { (n: Int, d: Int) =>
 *
 * whenever (d != 0 && d != Integer.MIN_VALUE && n != Integer.MIN_VALUE) {
 *
 * val f = new Fraction(n, d)
 *
 * if (n < 0 && d < 0 || n > 0 && d > 0) f.numer should be > 0 else if (n != 0) f.numer should be <
 * 0 else f.numer should be === 0
 *
 * f.denom should be > 0 } } </pre>
 *
 * <p> Note that even if you use generators that don't produce the invalid values, you still need
 * the <code>whenever</code> clause. The reason is that once a property fails, ScalaCheck will try
 * and shrink the values to the smallest values that still cause the property to fail. During this
 * shrinking process ScalaCheck may pass invalid values. The <code>whenever</code> clause is still
 * needed to guard against those values. (The <code>whenever</code> clause also clarifies to readers
 * of the code exactly what the property is in a succinct way, without requiring that they find and
 * understand the generator definitions.) </p>
 *
 * <a name="supplyingGeneratorsAndArgNames"></a><h2>Supplying both generators and argument
 * names</h2>
 *
 * <p> If you want to supply both generators and named arguments, you can do so by providing a list
 * of <code>(&lt;generator&gt;, &lt;name&gt;)</code> pairs in parentheses after <code>forAll</code>,
 * before the property function. Here's an example: </p>
 *
 * <pre class="stHighlight"> forAll ((validNumers, "n"), (validDenoms, "d")) { (n: Int, d: Int) =>
 *
 * whenever (d != 0 && d != Integer.MIN_VALUE && n != Integer.MIN_VALUE) {
 *
 * val f = new Fraction(n, d)
 *
 * if (n < 0 && d < 0 || n > 0 && d > 0) f.numer should be > 0 else if (n != 0) f.numer should be <
 * 0 else f.numer should be === 0
 *
 * f.denom should be > 0 } } </pre>
 *
 * <p> Were this property check to fail, it would mention the names n and d in the error message,
 * like this: </p>
 *
 * <pre> Occurred when passed generated values ( n = 17, d = 21 ) </pre>
 *
 * <a name="propCheckConfig"></a><h2>Property check configuration</h2>
 *
 * <p> The property checks performed by the <code>forAll</code> methods of this trait can be
 * flexibly configured via the services provided by supertrait <code>Configuration</code>. The five
 * configuration parameters for property checks along with their default values and meanings are
 * described in the following table: </p>
 *
 * <table style="border-collapse: collapse; border: 1px solid black"> <tr> <th
 * style="background-color: #CCCCCC; border-width: 1px; padding: 3px; text-align: center; border:
 * 1px solid black"> <strong>Configuration Parameter</strong> </th> <th style="background-color:
 * #CCCCCC; border-width: 1px; padding: 3px; text-align: center; border: 1px solid black">
 * <strong>Default Value</strong> </th> <th style="background-color: #CCCCCC; border-width: 1px;
 * padding: 3px; text-align: center; border: 1px solid black"> <strong>Meaning</strong> </th> </tr>
 * <tr> <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: center">
 * minSuccessful </td> <td style="border-width: 1px; padding: 3px; border: 1px solid black;
 * text-align: center"> 100 </td> <td style="border-width: 1px; padding: 3px; border: 1px solid
 * black; text-align: left"> the minimum number of successful property evaluations required for the
 * property to pass </td> </tr> <tr> <td style="border-width: 1px; padding: 3px; border: 1px solid
 * black; text-align: center"> maxDiscardedFactor </td> <td style="border-width: 1px; padding: 3px;
 * border: 1px solid black; text-align: center"> 500 </td> <td style="border-width: 1px; padding:
 * 3px; border: 1px solid black; text-align: left"> the maximum discarded factor property
 * evaluations allowed during a property check </td> </tr> <tr> <td style="border-width: 1px;
 * padding: 3px; border: 1px solid black; text-align: center"> minSize </td> <td
 * style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: center"> 0 </td> <td
 * style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: left"> the minimum
 * size parameter to provide to ScalaCheck, which it will use when generating objects for which size
 * matters (such as strings or lists) </td> </tr> <tr> <td style="border-width: 1px; padding: 3px;
 * border: 1px solid black; text-align: center"> sizeRange </td> <td style="border-width: 1px;
 * padding: 3px; border: 1px solid black; text-align: center"> 100 </td> <td style="border-width:
 * 1px; padding: 3px; border: 1px solid black; text-align: left"> the size range parameter to
 * provide to ScalaCheck, which it will use when generating objects for which size matters (such as
 * strings or lists) </td> </tr> <tr> <td style="border-width: 1px; padding: 3px; border: 1px solid
 * black; text-align: center"> workers </td> <td style="border-width: 1px; padding: 3px; border: 1px
 * solid black; text-align: center"> 1 </td> <td style="border-width: 1px; padding: 3px; border: 1px
 * solid black; text-align: left"> specifies the number of worker threads to use during property
 * evaluation </td> </tr> </table>
 *
 * <p> The <code>forAll</code> methods of trait <code>ScalaCheckDrivenPropertyChecks</code> each
 * take a <code>PropertyCheckConfiguration</code> object as an implicit parameter. This object
 * provides values for each of the five configuration parameters. Trait <code>Configuration</code>
 * provides an implicit <code>val</code> named <code>generatorDrivenConfig</code> with each
 * configuration parameter set to its default value. If you want to set one or more configuration
 * parameters to a different value for all property checks in a suite you can override this val (or
 * hide it, for example, if you are importing the members of the
 * <code>ScalaCheckDrivenPropertyChecks</code> companion object rather than mixing in the trait.)
 * For example, if you want all parameters at their defaults except for <code>minSize</code> and
 * <code>sizeRange</code>, you can override <code>generatorDrivenConfig</code>, like this:
 *
 * <pre class="stHighlight"> implicit override val generatorDrivenConfig =
 * PropertyCheckConfiguration(minSize = 10, sizeRange = 10) </pre>
 *
 * <p> Or, hide it by declaring a variable of the same name in whatever scope you want the changed
 * values to be in effect: </p>
 *
 * <pre class="stHighlight"> implicit val generatorDrivenConfig = PropertyCheckConfiguration(minSize
 * \= 10, sizeRange = 10) </pre>
 *
 * <p> In addition to taking a <code>PropertyCheckConfiguration</code> object as an implicit
 * parameter, the <code>forAll</code> methods of trait <code>ScalaCheckDrivenPropertyChecks</code>
 * also take a variable length argument list of <code>PropertyCheckConfigParam</code> objects that
 * you can use to override the values provided by the implicit
 * <code>PropertyCheckConfiguration</code> for a single <code>forAll</code> invocation. For example,
 * if you want to set <code>minSuccessful</code> to 500 for just one particular <code>forAll</code>
 * invocation, you can do so like this: </p>
 *
 * <pre class="stHighlight"> forAll (minSuccessful(500)) { (n: Int, d: Int) => ... </pre>
 *
 * <p> This invocation of <code>forAll</code> will use 500 for <code>minSuccessful</code> and
 * whatever values are specified by the implicitly passed <code>PropertyCheckConfiguration</code>
 * object for the other configuration parameters. If you want to set multiple configuration
 * parameters in this way, just list them separated by commas: </p>
 *
 * <pre class="stHighlight"> forAll (minSuccessful(500), maxDiscardedFactor(0.6)) { (n: Int, d: Int)
 * \=> ... </pre>
 *
 * <p> If you are using an overloaded form of <code>forAll</code> that already takes an initial
 * parameter list, just add the configuration parameters after the list of generators, names, or
 * generator/name pairs, as in: </p>
 *
 * <pre class="stHighlight"> // If providing argument names forAll ("n", "d", minSuccessful(500),
 * maxDiscardedFactor(0.6)) { (n: Int, d: Int) => ...
 *
 * // If providing generators forAll (validNumers, validDenoms, minSuccessful(500),
 * maxDiscardedFactor(0.6)) { (n: Int, d: Int) => ...
 *
 * // If providing (&lt;generators&gt;, &lt;name&gt;) pairs forAll ((validNumers, "n"),
 * (validDenoms, "d"), minSuccessful(500), maxDiscardedFactor(0.6)) { (n: Int, d: Int) => ... </pre>
 *
 * <p> For more information, see the documentation for supertrait <a
 * href="Configuration.html"><code>Configuration</code></a>. </p>
 *
 * @author
 *   Bill Venners
 */
trait ScalaCheckDrivenPropertyChecks {

  /**
   * Performs a property check by applying the specified property check function to arguments
   * supplied by implicitly passed generators.
   *
   * <p> Here's an example: </p>
   *
   * <pre class="stHighlight"> forAll { (a: String) => a.length should equal ((a).length) } </pre>
   *
   * @param test
   *   the property check function to apply to the generated arguments
   */
  def forAll[A, ASSERTION](propertyA: PropertyT[A])(test: A => ASSERTION)(implicit
      config: PropertyConfig,
      asserting: CheckerAsserting[ASSERTION],
      pos: Position
  ): asserting.CheckResult = {
    val property = for {
      a <- propertyA
    } yield try {
      test(a)
      Result.success
    } catch {
      case e: Exception => Result.error(e)
    }
    asserting.check(property, config, pos)
  }

  /**
   * Performs a property check by applying the specified property check function to arguments
   * supplied by implicitly passed generators.
   *
   * <p> Here's an example: </p>
   *
   * <pre class="stHighlight"> forAll { (a: String) => a.length should equal ((a).length) } </pre>
   *
   * @param test
   *   the property check function to apply to the generated arguments
   */
  def forAll[A, ASSERTION](genA: Gen[A])(test: A => ASSERTION)(implicit
      config: PropertyConfig,
      asserting: CheckerAsserting[ASSERTION],
      pos: Position
  ): asserting.CheckResult =
    forAll(genA.forAll)(test)

  /**
   * Performs a property check by applying the specified property check function to arguments
   * supplied by the specified generators.
   *
   * <p> Here's an example: </p>
   *
   * <pre class="stHighlight"> import org.scalacheck.Gen
   *
   * // Define your own string generator: val famousLastWords = for { s <- Gen.oneOf("the",
   * "program", "compiles", "therefore", "it", "should", "work") } yield s
   *
   * forAll (famousLastWords, famousLastWords) { (a: String, b: String) => a.length + b.length
   * should equal ((a + b).length) } </pre>
   *
   * @param fun
   *   the property check function to apply to the generated arguments
   */
  def forAll[A, B, ASSERTION](propertyA: PropertyT[A], propertyB: PropertyT[B])(
      fun: (A, B) => ASSERTION
  )(implicit
      config: PropertyConfig,
      asserting: CheckerAsserting[ASSERTION],
      pos: Position
  ): asserting.CheckResult = {
    val property = for {
      a <- propertyA
      b <- propertyB
    } yield try {
      fun(a, b)
      Result.success
    } catch {
      case e: Exception => Result.error(e)
    }
    asserting.check(property, config, pos)
  }

  /**
   * Performs a property check by applying the specified property check function to arguments
   * supplied by the specified generators.
   *
   * <p> Here's an example: </p>
   *
   * <pre class="stHighlight"> import org.scalacheck.Gen
   *
   * // Define your own string generator: val famousLastWords = for { s <- Gen.oneOf("the",
   * "program", "compiles", "therefore", "it", "should", "work") } yield s
   *
   * forAll (famousLastWords, famousLastWords) { (a: String, b: String) => a.length + b.length
   * should equal ((a + b).length) } </pre>
   *
   * @param fun
   *   the property check function to apply to the generated arguments
   */
  def forAll[A, B, ASSERTION](genA: Gen[A], genB: Gen[B])(fun: (A, B) => ASSERTION)(implicit
      config: PropertyConfig,
      asserting: CheckerAsserting[ASSERTION],
      pos: Position
  ): asserting.CheckResult = forAll(genA.forAll, genB.forAll)(fun)

  /**
   * Performs a property check by applying the specified property check function to arguments
   * supplied by the specified generators.
   *
   * <p> Here's an example: </p>
   *
   * <pre class="stHighlight"> import org.scalacheck.Gen
   *
   * // Define your own string generator: val famousLastWords = for { s <- Gen.oneOf("the",
   * "program", "compiles", "therefore", "it", "should", "work") } yield s
   *
   * forAll (famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String)
   * \=> a.length + b.length + c.length should equal ((a + b + c).length) } </pre>
   *
   * @param fun
   *   the property check function to apply to the generated arguments
   */
  def forAll[A, B, C, ASSERTION](
      propertyA: PropertyT[A],
      propertyB: PropertyT[B],
      propertyC: PropertyT[C]
  )(
      fun: (A, B, C) => ASSERTION
  )(implicit
      config: PropertyConfig,
      asserting: CheckerAsserting[ASSERTION],
      pos: Position
  ): asserting.CheckResult = {
    val property = for {
      a <- propertyA
      b <- propertyB
      c <- propertyC
    } yield try {
      fun(a, b, c)
      Result.success
    } catch {
      case e: Exception => Result.error(e)
    }
    asserting.check(property, config, pos)
  }

  /**
   * Performs a property check by applying the specified property check function to arguments
   * supplied by the specified generators.
   *
   * <p> Here's an example: </p>
   *
   * <pre class="stHighlight"> import org.scalacheck.Gen
   *
   * // Define your own string generator: val famousLastWords = for { s <- Gen.oneOf("the",
   * "program", "compiles", "therefore", "it", "should", "work") } yield s
   *
   * forAll (famousLastWords, famousLastWords, famousLastWords) { (a: String, b: String, c: String)
   * \=> a.length + b.length + c.length should equal ((a + b + c).length) } </pre>
   *
   * @param fun
   *   the property check function to apply to the generated arguments
   */
  def forAll[A, B, C, ASSERTION](genA: Gen[A], genB: Gen[B], genC: Gen[C])(
      fun: (A, B, C) => ASSERTION
  )(implicit
      config: PropertyConfig,
      asserting: CheckerAsserting[ASSERTION],
      pos: Position
  ): asserting.CheckResult = forAll(genA.forAll, genB.forAll, genC.forAll)(fun)

  /**
   * Performs a property check by applying the specified property check function to arguments
   * supplied by the specified generators.
   *
   * <p> Here's an example: </p>
   *
   * <pre class="stHighlight"> import org.scalacheck.Gen
   *
   * // Define your own string generator: val famousLastWords = for { s <- Gen.oneOf("the",
   * "program", "compiles", "therefore", "it", "should", "work") } yield s
   *
   * forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b:
   * String, c: String, d: String) => a.length + b.length + c.length + d.length should equal ((a + b
   * + c + d).length) } </pre>
   *
   * @param fun
   *   the property check function to apply to the generated arguments
   */
  def forAll[A, B, C, D, ASSERTION](
      propertyA: PropertyT[A],
      propertyB: PropertyT[B],
      propertyC: PropertyT[C],
      propertyD: PropertyT[D]
  )(fun: (A, B, C, D) => ASSERTION)(implicit
      config: PropertyConfig,
      asserting: CheckerAsserting[ASSERTION],
      pos: Position
  ): asserting.CheckResult = {
    val property = for {
      a <- propertyA
      b <- propertyB
      c <- propertyC
      d <- propertyD
    } yield try {
      fun(a, b, c, d)
      Result.success
    } catch {
      case e: Exception => Result.error(e)
    }
    asserting.check(property, config, pos)
  }

  /**
   * Performs a property check by applying the specified property check function to arguments
   * supplied by the specified generators.
   *
   * <p> Here's an example: </p>
   *
   * <pre class="stHighlight"> import org.scalacheck.Gen
   *
   * // Define your own string generator: val famousLastWords = for { s <- Gen.oneOf("the",
   * "program", "compiles", "therefore", "it", "should", "work") } yield s
   *
   * forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords) { (a: String, b:
   * String, c: String, d: String) => a.length + b.length + c.length + d.length should equal ((a + b
   * + c + d).length) } </pre>
   *
   * @param fun
   *   the property check function to apply to the generated arguments
   */
  def forAll[A, B, C, D, ASSERTION](
      genA: Gen[A],
      genB: Gen[B],
      genC: Gen[C],
      genD: Gen[D]
  )(fun: (A, B, C, D) => ASSERTION)(implicit
      config: PropertyConfig,
      asserting: CheckerAsserting[ASSERTION],
      pos: Position
  ): asserting.CheckResult = forAll(genA.forAll, genB.forAll, genC.forAll, genD.forAll)(fun)

  /**
   * Performs a property check by applying the specified property check function to arguments
   * supplied by the specified generators.
   *
   * <p> Here's an example: </p>
   *
   * <pre class="stHighlight"> import org.scalacheck.Gen
   *
   * // Define your own string generator: val famousLastWords = for { s <- Gen.oneOf("the",
   * "program", "compiles", "therefore", "it", "should", "work") } yield s
   *
   * forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) {
   * (a: String, b: String, c: String, d: String, e: String) => a.length + b.length + c.length +
   * d.length + e.length should equal ((a + b + c + d + e).length) } </pre>
   *
   * @param fun
   *   the property check function to apply to the generated arguments
   */
  def forAll[A, B, C, D, E, ASSERTION](
      propertyA: PropertyT[A],
      propertyB: PropertyT[B],
      propertyC: PropertyT[C],
      propertyD: PropertyT[D],
      propertyE: PropertyT[E]
  )(fun: (A, B, C, D, E) => ASSERTION)(implicit
      config: PropertyConfig,
      asserting: CheckerAsserting[ASSERTION],
      pos: Position
  ): asserting.CheckResult = {
    val property = for {
      a <- propertyA
      b <- propertyB
      c <- propertyC
      d <- propertyD
      e <- propertyE
    } yield try {
      fun(a, b, c, d, e)
      Result.success
    } catch {
      case e: Exception => Result.error(e)
    }
    asserting.check(property, config, pos)
  }

  /**
   * Performs a property check by applying the specified property check function to arguments
   * supplied by the specified generators.
   *
   * <p> Here's an example: </p>
   *
   * <pre class="stHighlight"> import org.scalacheck.Gen
   *
   * // Define your own string generator: val famousLastWords = for { s <- Gen.oneOf("the",
   * "program", "compiles", "therefore", "it", "should", "work") } yield s
   *
   * forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords) {
   * (a: String, b: String, c: String, d: String, e: String) => a.length + b.length + c.length +
   * d.length + e.length should equal ((a + b + c + d + e).length) } </pre>
   *
   * @param fun
   *   the property check function to apply to the generated arguments
   */
  def forAll[A, B, C, D, E, ASSERTION](
      genA: Gen[A],
      genB: Gen[B],
      genC: Gen[C],
      genD: Gen[D],
      genE: Gen[E]
  )(fun: (A, B, C, D, E) => ASSERTION)(implicit
      config: PropertyConfig,
      asserting: CheckerAsserting[ASSERTION],
      pos: Position
  ): asserting.CheckResult =
    forAll(genA.forAll, genB.forAll, genC.forAll, genD.forAll, genE.forAll)(fun)

  /**
   * Performs a property check by applying the specified property check function to arguments
   * supplied by the specified generators.
   *
   * <p> Here's an example: </p>
   *
   * <pre class="stHighlight"> import org.scalacheck.Gen
   *
   * // Define your own string generator: val famousLastWords = for { s <- Gen.oneOf("the",
   * "program", "compiles", "therefore", "it", "should", "work") } yield s
   *
   * forAll (famousLastWords, famousLastWords, famousLastWords, famousLastWords, famousLastWords,
   * famousLastWords) { (a: String, b: String, c: String, d: String, e: String, f: String) =>
   * a.length + b.length + c.length + d.length + e.length + f.length should equal ((a + b + c + d +
   * e + f).length) } </pre>
   *
   * @param fun
   *   the property check function to apply to the generated arguments
   */
  def forAll[A, B, C, D, E, F, ASSERTION](
      genA: Gen[A],
      genB: Gen[B],
      genC: Gen[C],
      genD: Gen[D],
      genE: Gen[E],
      genF: Gen[F]
  )(fun: (A, B, C, D, E, F) => ASSERTION)(implicit
      config: PropertyConfig,
      asserting: CheckerAsserting[ASSERTION],
      pos: Position
  ): asserting.CheckResult = {
    val property = for {
      a <- genA.forAll
      b <- genB.forAll
      c <- genC.forAll
      d <- genD.forAll
      e <- genE.forAll
      f <- genF.forAll
    } yield try {
      fun(a, b, c, d, e, f)
      Result.success
    } catch {
      case e: Exception => Result.error(e)
    }
    asserting.check(property, config, pos)
  }
}

object ScalaCheckDrivenPropertyChecks extends ScalaCheckDrivenPropertyChecks
