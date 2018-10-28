package hedgehog

import hedgehog.core._
import hedgehog.runner._

object ErrorTest extends Properties {

  def tests: List[Prop] =
    List(
      example("tests with no generators don't throw exceptions", noGen)
    , example("tests with generators that throw exception in map will shrink", shrinkMap)
    , example("tests with generators that throw exception in flatMap will shrink", shrinkFlatMap)
    )

  def noGen: Result = {
    val e = new RuntimeException()
    val prop = Prop("", throw e)
    val r = Property.checkRandom(PropertyConfig.default, prop.result).value
    getErrorLog(r.status) ==== List(Error(e))
  }

  def shrinkMap: Result = {
    val e = new RuntimeException()
    val p = Gen.int(Range.linear(0, 100)).forAll.map(i =>
      if (i > 5) throw e else Result.success
    )
    val r = Property.checkRandom(PropertyConfig.default, p).value
    getErrorLog(r.status) ==== List(Info("6"), Error(e))
  }

  def shrinkFlatMap: Result = {
    val e = new RuntimeException()
    val p = Gen.int(Range.linear(0, 100)).forAll.flatMap(i =>
      if (i > 5) throw e else Property.point(Result.success)
    )
    val r = Property.checkRandom(PropertyConfig.default, p).value
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
