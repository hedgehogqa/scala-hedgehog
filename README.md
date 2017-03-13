[![Build Status](https://travis-ci.org/hedgehogqa/scala-hedgehog.svg?branch=master)](https://travis-ci.org/hedgehogqa/scala-hedgehog)

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
