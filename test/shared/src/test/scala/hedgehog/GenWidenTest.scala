package hedgehog

import hedgehog.core._
import hedgehog.runner._

object GenWidenTest extends Properties {

  sealed trait Animal
  final case class Cat(name: String, lives: Int) extends Animal
  final case class Dog(name: String) extends Animal

  def tests: List[Test] =
    List(
      property("widened generators combine into a supertype generator", testCombine)
    , property("widened generators combine when the supertype is named once on the combinator", testCombine2)
    , property("widen matches the map-based upcast, including shrinks", testSemantics)
    , example("widen returns the same generator instance", testZeroCost)
    )

  def genName: Gen[String] = Gen.string(Gen.alpha, Range.linear(1, 10))

  def genCat: Gen[Cat] =
    for {
      name <- genName
      lives <- Gen.int(Range.linear(1, 9))
    } yield Cat(name, lives)

  def genDog: Gen[Dog] = genName.map(Dog(_))

  def genAnimal: Gen[Animal] =
    Gen.choice1(genCat.widen[Animal], genDog.widen[Animal])

  def genAnimal2: Gen[Animal] =
    Gen.choice1[Animal](genCat.widen, genDog.widen)

  def testCombine: Property =
    for {
      animal <- genAnimal.log("animal")
    } yield animal match {
      case Cat(name, _) => Result.assert(name.nonEmpty)
      case Dog(name) => Result.assert(name.nonEmpty)
    }

  def testCombine2: Property =
    for {
      animal <- genAnimal2.log("animal")
    } yield animal match {
      case Cat(name, _) => Result.assert(name.nonEmpty)
      case Dog(name) => Result.assert(name.nonEmpty)
    }

  def testSemantics: Property =
    for {
      seed <- Gen.long(Range.linearFrom(0L, Long.MinValue, Long.MaxValue)).log("seed")
    } yield {
      val widened = genCat.widen[Animal].run(Size(10), Seed.fromLong(seed))
      val mapped = genCat.map(c => (c: Animal)).run(Size(10), Seed.fromLong(seed))
      TTree.fromTree(3, 10, widened) ==== TTree.fromTree(3, 10, mapped)
    }

  def testZeroCost: Result = {
    val gen = genCat
    Result.assert(gen.widen[Animal] eq gen)
  }
}
