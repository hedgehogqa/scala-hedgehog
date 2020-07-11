package hedgehog.minitest

import minitest.SimpleTestSuite
import hedgehog.core.Result
import hedgehog._

object MinitestIntegrationTest extends SimpleTestSuite with HedgehogSupport {
  test("standard minitest test case") {
    assert(3 + 5 == 8)
  }
  example("hedgehog example as minitest test case") {
    Result.assert(3 + 5 == 8)  
  }
  property("hedgehog property as minitest test case") {
    for {
      x <- Gen.int(Range.linear(-500, 500)).forAll
      y <- Gen.int(Range.linear(-500, 500)).forAll  
    } yield x + y ==== y + x
  }
}
