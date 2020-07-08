package hedgehog.runner

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