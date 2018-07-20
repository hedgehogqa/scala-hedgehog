[![Build Status](https://travis-ci.org/hedgehogqa/scala-hedgehog.svg?branch=master)](https://travis-ci.org/hedgehogqa/scala-hedgehog)

# WARNING

This project is _only_ a proof of concept at the moment. It was ported
almost verbatim from the [haskell](https://github.com/hedgehogqa/haskell-hedgehog)
implementation in a few days, without consideration of how it was consumed
in Scala.

Since then there are been independent efforts that may or may not be more
mature and prove to be a better place to start.

- https://github.com/melrief/sonic

  This is another port, based on cats and monix.
  It's worth noting that scala-hedgehog currently uses scalaz, but this was
  for prototyping and the vague plan was to make it agnostic (if possible).

- https://github.com/scalaz/testz

  A more general FP testing library, with
  [plans to implementing hedgehog-like shrinking](https://github.com/scalaz/testz/issues/5)


# scala-hedgehog

An alternative property-based testing system for Scala, in the spirit of John
Hughes & Koen Classen's [QuickCheck](https://web.archive.org/web/20160319204559/http://www.cs.tufts.edu/~nr/cs257/archive/john-hughes/quick.pdf).

The key improvement is that shrinking comes for free — instead of generating
a random value and using a shrinking function after the fact, we generate the
random value and all the possible shrinks in a rose tree, all at once.

## Alternatives

In Scala there are other property-testing alternatives:

- https://github.com/rickynils/scalacheck
- https://github.com/scalaprops/scalaprops
- https://github.com/japgolly/nyaya
