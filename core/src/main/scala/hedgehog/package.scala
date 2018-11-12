import hedgehog.core._
import hedgehog.extra._
import hedgehog.predef.ApplicativeSyntax

package object hedgehog extends ApplicativeSyntax {

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

  type Property = PropertyT[HM, Result]
  object Property extends PropertyTOps[HM]

  type PropertyR[A] = core.PropertyR[A]
  val PropertyR = core.PropertyR

  type Result = hedgehog.core.Result
  val Result = hedgehog.core.Result

  def propertyT[M[_]]: PropertyTOps[M] =
    new PropertyTOps[M] {}

  implicit class Syntax[A](a1: A) {

     // FIX Is there a way to get this to work with PropertyT and type-inference?
     def ====(a2: A): Result = {
       if (a1 == a2)
         Result.success
       else
         Result.failure
           .log("=== Not Equal ===")
           .log(a1.toString)
           .log(a2.toString)
     }

  }
}
