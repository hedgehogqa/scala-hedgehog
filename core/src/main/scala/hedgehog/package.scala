import hedgehog.core._
import hedgehog.extra._

package object hedgehog {

  type HM[A] = predef.Identity[A]

  /**
   * This is _purely_ to make consuming this library a nicer experience,
   * mainly due to Scala's type inference problems and higher kinds.
   */
  object Gen
    extends GenTOps[HM]
    with ByteOps[HM]
    with CharacterOps[HM]
    with StringOps[HM]
  type Gen[A] = GenT[HM, A]

  def genT[M[_]] =
    new GenTOps[M]
    with ByteOps[M]
    with CharacterOps[M]
    with StringOps[M] {}

  type Property[A] = PropertyT[HM, A]
  object Property extends PropertyTOps[HM]

  def propertyT[M[_]]: PropertyTOps[M] =
    new PropertyTOps[M] {}

  implicit class Syntax[A](a1: A) {

     // FIX Is there a way to get this to work with PropertyT and type-inference?
     def ===(a2: A): Property[Unit] = {
       val p = Property
       if (a1 == a2)
         p.success
       else
         for {
           _ <- p.info("=== Not Equal ===")
           _ <- p.info(a1.toString)
           _ <- p.info(a2.toString)
           _ <- p.failure
         } yield ()
     }

    // To avoid name clashes
    def ====(a2: A): Property[Unit] =
      ===(a2)
  }
}
