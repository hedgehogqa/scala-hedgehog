import hedgehog.core._
import hedgehog.extra._
import hedgehog.predef.ApplicativeSyntax

package object hedgehog extends ApplicativeSyntax {

  /**
   * This is _purely_ to make consuming this library a nicer experience,
   * mainly due to Scala's type inference problems and higher kinds.
   */
  object Gen
    extends GenTOps
    with ByteOps
    with CharacterOps
    with StringOps
  type Gen[A] = GenT[A]

  type Property = PropertyT[Result]
  object Property extends PropertyTOps

  type PropertyR[A] = core.PropertyR[A]
  val PropertyR = core.PropertyR

  type Result = hedgehog.core.Result
  val Result = hedgehog.core.Result

  type MonadGen[M[_]] = MonadGenT[M]
  def MonadGen[M[_]] =
    new MonadGenOps[M] {}

  def propertyT: PropertyTOps =
    new PropertyTOps {}

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
