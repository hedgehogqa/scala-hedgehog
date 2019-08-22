package hedgehog

import hedgehog.core._
import hedgehog.runner._

object CoverageTest extends Properties {

  override def tests: List[Test] =
    List(
      example("test cover passes", testCoverPass)
    , example("test cover fails", testCoverFail)
    )

  def testCoverPass: Result = {
    val g =
      for {
        _ <- Gen.boolean.forAll
          .cover(30, "true", x => x)
          .cover(30, "false", x => !x)
      } yield Result.success

    val r = Property.checkRandom(PropertyConfig.default, g)
    Result.all(List(
      r.coverage.labels.keys.toList.sortBy(_.render) ==== List(LabelName("false"), LabelName("true"))
    , r.status ==== Status.ok
    ))
  }

  def testCoverFail: Result = {
    val g =
      for {
        _ <- Gen.boolean.forAll
          .cover(70, "true", x => x)
          .cover(30, "false", x => !x)
      } yield Result.success

    val r = Property.checkRandom(PropertyConfig.default, g)
    Result.all(List(
      r.coverage.labels.keys.toList.sortBy(_.render) ==== List(LabelName("false"), LabelName("true"))
    , r.status match {
        case Failed(_, _) =>
          Result.success
        case s =>
          Result.failure.log(s.toString)
      }
    ))
  }
}
