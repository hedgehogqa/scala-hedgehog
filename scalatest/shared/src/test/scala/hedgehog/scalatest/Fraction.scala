package hedgehog.scalatest

import hedgehog.Gen
import hedgehog.core.PropertyConfig
import hegehog.scalatest.HedgehogDrivenPropertyChecks
import org.scalatest.DoNotDiscover
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

@DoNotDiscover // This is an example test suite, not meant to be run as part of the main test suite.
class Fraction extends AnyFunSpec with HedgehogDrivenPropertyChecks with Matchers {

  class Fraction(n: Int, d: Int) {

    require(d != 0)
    require(d != Integer.MIN_VALUE)
    require(n != Integer.MIN_VALUE)

    val numer: Int = if (d < 0) -1 * n else n
    val denom: Int = d.abs

    override def toString = s"$numer / $denom"
  }

  implicit val config: PropertyConfig = PropertyConfig.default

  it("should only allow valid fractions and normalize them") {
    val allIntegers = Gen.int(hedgehog.Range.linear(Integer.MIN_VALUE, Integer.MAX_VALUE))
    forAll(allIntegers, allIntegers) { (n, d) =>
      whenever(d != 0 && d != Integer.MIN_VALUE && n != Integer.MIN_VALUE) {

        val f = new Fraction(n, d)

        if (n < 0 && d < 0 || n > 0 && d > 0) f.numer should be > 0
        else if (n != 0)
          f.numer should be < 0
        else f.numer should equal(0)

        f.denom should be > 0
      }
    }
  }

  it("should only generate valid integers") {
    val validNumers = Gen.int(hedgehog.Range.linear(Integer.MIN_VALUE + 1, Integer.MAX_VALUE))
    val validDenoms = validNumers.filter(_ != 0)
    forAll(validNumers, validDenoms) { (n, d) =>
      val f = new Fraction(n, d)

      if (n < 0 && d < 0 || n > 0 && d > 0) f.numer should be > 0
      else if (n != 0) f.numer should be < 0
      else f.numer should equal(0)

      f.denom should be > 0
    }
  }
}
