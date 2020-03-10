package hedgehog.predef

/**
 * Convenience syntax for `Applicative` usage.
 *
 * Please forgive the use of overloading here, which avoids having to have an `ApplicativeBuilder`.
 */
trait ApplicativeSyntax {

  def forTupled[M[_], A, B](ma: M[A], mb: M[B])(implicit F: Applicative[M]): M[(A, B)] =
    F.ap(mb)(F.ap(ma)(F.point((a: A) => (b: B) => (a, b))))

  def forTupled[M[_], A, B, C](ma: M[A], mb: M[B], mc: M[C])(implicit F: Applicative[M]): M[(A, B, C)] =
    F.ap(mc)(F.ap(mb)(F.ap(ma)(F.point((a: A) => (b: B) => (c: C) => (a, b, c)))))

  def forTupled[M[_], A, B, C, D](ma: M[A], mb: M[B], mc: M[C], md: M[D])(implicit F: Applicative[M]): M[(A, B, C, D)] =
    F.ap(md)(F.ap(mc)(F.ap(mb)(F.ap(ma)(F.point((a: A) => (b: B) => (c: C) => (d: D) => (a, b, c, d))))))

  def forTupled[M[_], A, B, C, D, E](ma: M[A], mb: M[B], mc: M[C], md: M[D], me: M[E])(implicit F: Applicative[M]): M[(A, B, C, D, E)] =
    F.ap(me)(F.ap(md)(F.ap(mc)(F.ap(mb)(F.ap(ma)(F.point((a: A) => (b: B) => (c: C) => (d: D) => (e: E) => (a, b, c, d, e)))))))

  def forTupled[M[_], A, B, C, D, E, F](ma: M[A], mb: M[B], mc: M[C], md: M[D], me: M[E], mf: M[F])(implicit F: Applicative[M]): M[(A, B, C, D, E, F)] =
    F.ap(mf)(F.ap(me)(F.ap(md)(F.ap(mc)(F.ap(mb)(F.ap(ma)(F.point((a: A) => (b: B) => (c: C) => (d: D) => (e: E) => (f: F) => (a, b, c, d, e, f))))))))

  def forTupled[M[_], A, B, C, D, E, F, G](ma: M[A], mb: M[B], mc: M[C], md: M[D], me: M[E], mf: M[F], mg: M[G])(implicit F: Applicative[M]): M[(A, B, C, D, E, F, G)] =
    F.ap(mg)(F.ap(mf)(F.ap(me)(F.ap(md)(F.ap(mc)(F.ap(mb)(F.ap(ma)(F.point((a: A) => (b: B) => (c: C) => (d: D) => (e: E) => (f: F) => (g: G) => (a, b, c, d, e, f, g)))))))))

  def forTupled[M[_], A, B, C, D, E, F, G, H](ma: M[A], mb: M[B], mc: M[C], md: M[D], me: M[E], mf: M[F], mg: M[G], mh: M[H])(implicit F: Applicative[M]): M[(A, B, C, D, E, F, G, H)] =
    F.ap(mh)(F.ap(mg)(F.ap(mf)(F.ap(me)(F.ap(md)(F.ap(mc)(F.ap(mb)(F.ap(ma)(F.point((a: A) => (b: B) => (c: C) => (d: D) => (e: E) => (f: F) => (g: G) => (h: H) => (a, b, c, d, e, f, g, h))))))))))

  def forTupled[M[_], A, B, C, D, E, F, G, H, I](ma: M[A], mb: M[B], mc: M[C], md: M[D], me: M[E], mf: M[F], mg: M[G], mh: M[H], mi: M[I])(implicit F: Applicative[M]): M[(A, B, C, D, E, F, G, H, I)] =
    F.ap(mi)(F.ap(mh)(F.ap(mg)(F.ap(mf)(F.ap(me)(F.ap(md)(F.ap(mc)(F.ap(mb)(F.ap(ma)(F.point((a: A) => (b: B) => (c: C) => (d: D) => (e: E) => (f: F) => (g: G) => (h: H) => (i: I) => (a, b, c, d, e, f, g, h, i)))))))))))
}
