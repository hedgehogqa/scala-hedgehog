/*
 * Copyright 2001-2022 Artima, Inc.
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

import org.scalactic.{Prettifier, SizeLimit}
import org.scalatest._
import org.scalatest.exceptions.GeneratorDrivenPropertyCheckFailedException

class ScalaCheckDrivenPropertyChecksSpec extends funspec.AnyFunSpec with ScalaCheckDrivenPropertyChecks {

  describe("ScalaCheckDrivnePropertyChecks") {
    it("should truncate argument using passed in Prettifier") {
      val listGen: Gen[List[Int]] = {
        val gens = List(Gen.chooseNum(1, 2), Gen.chooseNum(3, 4), Gen.chooseNum(5, 6))
        Gen.sequence[List[Int], Int](gens)
      }

      var failingList: List[Int] = null
      implicit val prettifer = Prettifier.truncateAt(SizeLimit(2))

      val e =
        intercept[GeneratorDrivenPropertyCheckFailedException] {
          forAll(listGen) { list =>
            failingList = list
            assert(list.length != 3)
          }
        }

      assert(e.getMessage().contains("List(" + failingList.take(2).mkString(", ") + ", ...)"))
    }
  }

}