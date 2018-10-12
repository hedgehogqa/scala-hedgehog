package hedgehog

import hedgehog.core._
import hedgehog.runner._

object ErrorTest extends Properties {

  def tests: List[Prop] =
    List(
      Prop("tests with no generators don't throw exceptions", noGen)
    , Prop("tests with generators that throw exception in map will shrink", shrinkMap)
    , Prop("tests with generators that throw exception in flatMap will shrink", shrinkFlatMap)
    )

  def noGen: Property[Unit] = {
    val e = new RuntimeException()
    val prop = Prop("", throw e)
    val r = prop.result.checkRandom.value
    getErrorLog(r.status) ==== List(Error(e))
  }

  def shrinkMap: Property[Unit] = {
    val e = new RuntimeException()
    val p = Gen.int(Range.linear(0, 100)).forAll.map(i =>
      if (i > 5) throw e else ()
    )
    val r = p.checkRandom.value
    getErrorLog(r.status) ==== List(Info("6"), Error(e))
  }

  def shrinkFlatMap: Property[Unit] = {
    val e = new RuntimeException()
    val p = Gen.int(Range.linear(0, 100)).forAll.flatMap(i =>
      if (i > 5) throw e else Property.success
    )
    val r = p.checkRandom.value
    getErrorLog(r.status) ==== List(Info("6"), Error(e))
  }

  def getErrorLog(status: Status): List[Log] =
    status match {
      case Failed(_, l) =>
        l
      case _ =>
        Nil
    }
}
