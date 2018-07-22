package object hedgehog {

  /**
   * This is _purely_ to make consuming this library a nicer experience,
   * mainly due to Scala's type inference problems and higher kinds.
   *
   * NOTE: GenT needs to be trampolined as well.
   */
  object Gen extends GenTOps[scalaz.effect.IO]
  type Gen[A] = GenT[scalaz.effect.IO, A]

  def genT[M[_]]: GenTOps[M] =
    new GenTOps[M] {}

  type Property[A] = PropertyT[scalaz.effect.IO, A]
  object Property extends PropertyTOps[scalaz.effect.IO]

  def propertyT[M[_]]: PropertyTOps[M] =
    new PropertyTOps[M] {}
}
