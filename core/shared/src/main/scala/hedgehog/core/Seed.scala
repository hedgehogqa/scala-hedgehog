package hedgehog.core

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
    // Previously was doing something like:
    // https://github.com/rickynils/scalacheck/blob/86bd34e20ef4ce91a5f3ae5d70b6d96bfac885cf/src/main/scala/org/scalacheck/Gen.scala#L366-L379
    // Have updated to use the more stable haskell version
    // http://hackage.haskell.org/package/random-1.1/docs/src/System.Random.html#randomRFloating
    val (s2, next) = seed.nextDouble
    (Seed(s2), 2.0 * (0.5 * from + next * (0.5 * to - 0.5 * from)))
  }
}

object Seed {

  // FIX: predef IO
  def fromTime(): Seed =
    fromLong(System.nanoTime)

  def fromLong(seed: Long): Seed =
    Seed(MersenneTwister64.fromSeed(seed))
}

sealed trait SeedSource extends Product with Serializable {
  def seed: Long
  def renderLog: String = this match {
    case SeedSource.FromTime(seed) => 
      s"Using random seed: $seed"
    case SeedSource.FromEnv(seed) => 
      s"Using seed from environment variable HEDGEHOG_SEED: $seed"
    case SeedSource.FromLong(seed) =>
      s"Using seed: $seed"
  }
}
object SeedSource {
  def fromTime(seed: Long): SeedSource = FromTime(seed)
  def fromEnv(seed: Long): SeedSource = FromEnv(seed)
  def fromLong(seed: Long): SeedSource = FromLong(seed)

  def fromEnvOrTime(): SeedSource =
    sys.env
      .get("HEDGEHOG_SEED")
      .flatMap(s => scala.util.Try(s.toLong).toOption)
      .map(SeedSource.fromEnv)
      .getOrElse(SeedSource.fromTime(System.nanoTime()))

  final case class FromTime(seed: Long) extends SeedSource
  final case class FromEnv(seed: Long) extends SeedSource
  final case class FromLong(seed: Long) extends SeedSource
}

