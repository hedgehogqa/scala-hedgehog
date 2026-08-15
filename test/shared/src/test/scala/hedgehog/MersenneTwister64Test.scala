package hedgehog

import hedgehog.random.MersenneTwister64
import hedgehog.runner._

object MersenneTwister64Test extends Properties {

  def tests: List[Test] =
    List(
      property("generators with equal states have equal hashCodes", testEqualStatesHashEqually).withTests(500)
    , example("issue #308: differently-seeded generators do not share a hashCode", testDistinctSeedsHashDistinctly)
    , example("advancing a generator changes its hashCode", testAdvancedGeneratorHashesDistinctly)
    )

  def testEqualStatesHashEqually: Property =
    for {
      seed <- Gen.long(Range.linearFrom(0L, Long.MinValue, Long.MaxValue)).log("seed")
      // 400 crosses the block-regeneration boundary at 312
      steps <- Gen.frequency1(
          70 -> Gen.int(Range.linear(0, 311)),
          30 -> Gen.int(Range.linear(312, 400)),
        ).log("steps")
        .cover(60, "0 <= steps < 312", steps => steps >= 0 && steps < 312)
        .cover(20, "312 <= steps <= 400", steps => steps >= 312 && steps <= 400)
    } yield {
      val g1 = advance(MersenneTwister64.fromSeed(seed), steps)
      val g2 = advance(MersenneTwister64.fromSeed(seed), steps)
      Result.all(List(
        Result.assert(g1 === g2).log(
          s"""generators built identically should be equal
             |    g1=$g1
             |    g2=$g2
             |""".stripMargin)
      , (g1.hashCode ==== g2.hashCode)
          .log(
            s"""g1.hashCode should be equal to g2.hashCode.
               |  ⎬ g1.hashCode=${g1.hashCode}
               |  ⎩ g2.hashCode=${g2.hashCode}
               |""".stripMargin)
      , (g1.## ==== g2.##)
          .log(
            s"""g1.## should be equal to g2.##.
               |  ⎬ g1.##=${g1.##}
               |  ⎩ g2.##=${g2.##}
               |""".stripMargin)
      ))
    }

  def testDistinctSeedsHashDistinctly: Result = {
    val seeds = List(0L, 1L, -1L, 42L, 9999L, Long.MinValue, Long.MaxValue)
    val hashes = seeds.map(seed => MersenneTwister64.fromSeed(seed).hashCode)
    hashes.distinct.length ==== seeds.length
  }

  def testAdvancedGeneratorHashesDistinctly: Result = {
    /* Past the initial regeneration, states within a block share the same state
     * array and differ only in mti, so this fails for any hash of mt0 alone.
     */
    val g1 = MersenneTwister64.fromSeed(42L).next
    val g2 = g1.next
    Result.diffNamed(
      "hashCode should change when only mti differs",
      g1.hashCode,
      g2.hashCode
    )(_ != _)
  }

  @annotation.tailrec
  private def advance(g: MersenneTwister64, steps: Int): MersenneTwister64 =
    if (steps <= 0) g else advance(g.next, steps - 1)

}
