package hedgehog.core

import hedgehog.predef.LazyList

object Shrink extends XCompat {

  /**
   * Shrink an integral number by edging towards a destination.
   *
   * {{{
   * scala> towards(0, 100)
   * List(0, 50, 75, 88, 94, 97, 99)
   *
   * scala> towards(500, 1000)
   * List(500, 750, 875, 938, 969, 985, 993, 997, 999)
   *
   * scala> towards(-50,  -26)
   * List(-50, -38, -32, -29, -27)
   * }}}
   *
   * ''Note we always try the destination first, as that is the optimal shrink.''
   */
  def towards[A](destination: A, x: A)(implicit I: Integral[A]): List[A] =
    if (destination == x) {
      Nil
    } else {
      // Halve the operands before subtracting them so they don't overflow.
      // Consider `min` and `max` for a fixed sized type like 'Int'.
      val diff = I.minus(I.quot(x, I.fromInt(2)), I.quot(destination, I.fromInt(2)))
      consNub(destination, halves(diff).map(I.minus(x, _)))
    }

  /**
   * Shrink a floating-point number by edging towards a destination.
   *
   * {{{
   * scala> towards(0.0, 100)
   * List(0.0, 50.0, 75.0, 87.5, 93.75, 96.875, 98.4375...)
   *
   * scala> towards(1.0, 0.5)
   * List(1.0, 0.75, 0.625, 0.5625, 0.53125, 0.515625, 0.5078125...)
   * }}}
   *
   * ''Note we always try the destination first, as that is the optimal shrink.''
   */
  def towardsFloat[A](destination: Double, x: Double): List[Double] =
    if (destination == x) {
      Nil
    } else {
      val diff = x - destination
      StreamOrLazyList
        .iterate(diff)(_ / 2)
        .map(x - _)
        .takeWhile(y => y != x && !y.isNaN && !y.isInfinite)
        // In _extremely_ rare cases it's possible to generate a value outside of our range if the diff is very very small
        .filter(y => if (destination < x) destination <= y && y <= x else x <= y && y <= destination)
        .toList
    }

  /**
   * Shrink a list by edging towards the empty list.
   *
   * {{{
   * scala> list(List(1, 2, 3))
   * List(List(), List(2, 3), List(1, 3), List(1, 2))
   *
   * >>> list("abcd".toList)
   * List("", "cd", "ab", "bcd", "acd", "abd", "abc")
   * }}}
   *
   * ''Note we always try the empty list first, as that is the optimal shrink.''
   */
  def list[A](xs: List[A]): List[List[A]] =
    halves(xs.length).flatMap(removes(_, xs))

  /** Lazy `list`: each candidate is only built when it is tried. */
  private[hedgehog] def listLazy[A](xs: List[A]): LazyList[List[A]] = {
    val n = xs.length
    def go(ks: List[Int], i: Int): LazyList[List[A]] =
      ks match {
        case Nil =>
          LazyList.nil
        case k :: rest =>
          if (!canRemove(k, i, n))
            go(rest, 0)
          else
            LazyList.cons(remove(k, i, xs), go(ks, i + 1))
      }
    go(halves(n), 0)
  }

  /**
   * Produce all permutations of removing 'k' elements from a list.
   *
   * {{{
   * scala> removes(2, "abcdef".toList)
   * List("cdef", "abef", "abcd")
   * }}}
   */
  def removes[A](k: Int, xs: List[A]): List[List[A]] = {
    val n = xs.length
    List.tabulate(if (k <= 0) 0 else n / k)(remove(k, _, xs))
  }

  /** Whether chunk `i` of size `k` fits in a list of length `n`. */
  private def canRemove(k: Int, i: Int, n: Int): Boolean =
    k > 0 && i < n / k

  /** Removes chunk `i` of size `k`. */
  private def remove[A](k: Int, i: Int, xs: List[A]): List[A] =
    xs.take(i * k) ++ xs.drop((i + 1) * k)

  /**
   * Produce a list containing the progressive halving of an integral.
   *
   * {{{
   * scala> halves(15)
   * List(15, 7, 3, 1)
   *
   * scala> halves(100)
   * List(100, 50, 25, 12, 6, 3, 1)
   *
   * scala> halves(-26)
   * List(-26, -13, -6, -3, -1)
   * }}}
   */
  def halves[A](a: A)(implicit I: Integral[A]): List[A] =
    StreamOrLazyList.iterate(a)(I.quot(_, I.fromInt(2))).takeWhile(_ != 0).toList

  /**
   * Cons an element on to the front of a list unless it is already there.
   */
  def consNub[A](x: A, ys0: List[A]): List[A] =
     ys0 match {
       case Nil =>
         x :: Nil
       case y :: ys =>
         if (x == y) y :: ys else x :: y :: ys
     }
}
