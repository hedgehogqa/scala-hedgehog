package hedgehog.scalatest

import hedgehog.core.PropertyConfig
import hegehog.scalatest.HedgehogSupport
import org.scalatest.funspec.AnyFunSpec

/** Runs the example tests from the example subproject using Scalatest */
class Examples extends AnyFunSpec with HedgehogSupport {

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
}
