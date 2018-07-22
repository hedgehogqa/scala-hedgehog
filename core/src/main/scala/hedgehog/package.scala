package object hedgehog {

  type HM[A] = scalaz.Scalaz.Identity[A]

  /**
   * This is _purely_ to make consuming this library a nicer experience,
   * mainly due to Scala's type inference problems and higher kinds.
   */
  object Gen extends GenTOps[HM]
  type Gen[A] = GenT[HM, A]

  def genT[M[_]]: GenTOps[M] =
    new GenTOps[M] {}

  type Property[A] = PropertyT[HM, A]
  object Property extends PropertyTOps[HM]

  def propertyT[M[_]]: PropertyTOps[M] =
    new PropertyTOps[M] {}
}
