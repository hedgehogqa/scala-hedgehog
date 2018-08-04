package hedgehog

import hedgehog.random._

case class Seed(seed: MersenneTwister64) {

  def chooseLong(from: Long, to: Long): (Seed, Long) = {
    val next =
      if(from == to) {
        (seed.nextLong._1, from)
      } else {
        val min = math.min(from, to)
        val max = math.max(from, to)
        @annotation.tailrec
        def loop(state: MersenneTwister64): (MersenneTwister64, Long) = {
          val next = state.nextLong
          if (min <= next._2 && next._2 <= max) {
            next
          } else if(0 < (max - min)){
            val x = (next._2 % (max - min + 2)) + min - 1
            if (min <= x && x <= max) {
              (next._1, x)
            } else {
              loop(next._1)
            }
          } else {
            loop(next._1)
          }
        }
        loop(seed)
      }
    (Seed(next._1), next._2)
  }

  def chooseDouble(from: Double, to: Double): (Seed, Double) = {
    val (s2, next) = seed.nextDouble
    (Seed(s2), next * (to - from) + from)
  }
}

object Seed {

  // FIX: predef IO
  def fromTime(): Seed =
    fromLong(System.nanoTime)

  def fromLong(seed: Long): Seed =
    Seed(MersenneTwister64.fromSeed(seed))
}

