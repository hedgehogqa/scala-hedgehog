package hedgehog.scalatest

import hedgehog.Gen
import hedgehog.core.PropertyConfig
import hegehog.scalatest.HedgehogDrivenPropertyChecks
import org.scalatest.DoNotDiscover
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

/** Runs the example tests from the example subproject using Scalatest */
@DoNotDiscover // Not meant as a test suite, because it contains test that fail on purpose.
class Examples extends AnyFunSpec with HedgehogDrivenPropertyChecks with Matchers {

  implicit val config: PropertyConfig = PropertyConfig.default

  describe("CoverageTest") {
    hedgehog.examples.CoverageTest.tests.foreach { test =>
      it(test.name) {
        check(test)
      }
    }
  }

  describe("PropertyTest") {
    hedgehog.examples.PropertyTest.tests.foreach { test =>
      it(test.name) {
        check(test)
      }
    }
  }

  describe("PropertyRTest") {
    hedgehog.examples.PropertyRTest.tests.foreach { test =>
      it(test.name) {
        check(test)
      }
    }
  }

  describe("ReverseTest") {
    hedgehog.examples.ReverseTest.tests.foreach { test =>
      it(test.name) {
        check(test)
      }
    }
  }

  it("should only generate event ints") {
    val eventInts = for (n <- Gen.int(hedgehog.Range.linear(-1000, 1000))) yield 2 * n
    forAll(eventInts) { n => n % 2 should equal(0) }
  }
}
