package object hedgehog {

  type Gen[A] = GenT[scalaz.effect.IO, A]

  def genT[M[_]]: GenTOps[M] =
    new GenTOps[M] {}
}
