package hedgehog.core

/**
 * A slightly different way to express a property, with the added benefit of exposing a pure "test".
 * This enables running the test with specific examples, either as a "golden" test or from the shell. Or both.
 * The trade-off is that the `A` needs to be exposed/declared, and it's likely to be some horrible multi-value tuple.
 */
class PropertyR[A](
    val gen : PropertyT[A]
  , val test: A => Result
  ) {

  def property: PropertyT[Result] =
    gen.map(test)
}

object PropertyR {

  /** Constructor function with split arguments to help type-inference */
  def apply[A](gen: PropertyT[A])(test: A => Result): PropertyR[A] =
    new PropertyR[A](gen, test)
}
