package hedgehog
package munit

class HedgehogSuiteIntegrationTest extends HedgehogSuite {

  lazy val intGen = Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).forAll

  property("`property` works just like `test`, when it passes, it passes.") {
    for {
      x <- intGen
    } yield Result.diff(x, x + 0)(_ == _)
  }

  property("`Result.type` members are directly available for use") {
    for {
      x <- intGen
    } yield diff(x, x + 0)(_ == _)
  }

  property("`property` tests accept munit Assertions-like `assert*` results") {
    for {
      x <- intGen
    } yield assertEquals(clue(x), clue(x + 0))

  }

  // uncomment to see compilation errors related to deprecation encouraging
  // non-clue diff and diff delegating assertions
  // property("passing clues explicitly will cause a deprecation warning for any munit-like assertions in HedgehogAssertions"){
  //   for{
  //     x <- intGenfor
  //   } yield assertEquals(x, 2, clues(clue(x), clue(2)))
  // }

  // uncomment to see what HedgehogAssertions munit-like assert* test reports
  // look like
  // property("doing what the above deprecation says to do will result in a nice clue-like diff with a minimal failing example, hedgehog-style"){
  //   for{
  //     x <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).forAll
  //   } yield assertEquals(x, 2)
  // }

  test(
    "regular unit returning munit assertions work just fine, you just have to turn off the hedgehog assertions."
  ) {
    withMunitAssertions(assertions => assertions.assertEquals(1, 1))
  }
}
