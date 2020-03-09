package hedgehog.extra

import hedgehog._
import hedgehog.runner._

object CharacterTest extends Properties {

  def tests: List[Test] =
    List(
      property("unicode", testUnicode).withTests(1000)
    )

  def testUnicode: Property =
    for {
      c <- Gen.unicode.forAll
    } yield
      Result.all(List(
        Result.assert(!Character.isSurrogate(c)).log(c.toInt.toString).log("surrogate")
      , Result.assert(!(c == 65534 || c == 65535)).log("non-character")
      ))
}
