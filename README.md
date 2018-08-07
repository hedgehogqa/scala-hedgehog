[![Build Status](https://travis-ci.org/hedgehogqa/scala-hedgehog.svg?branch=master)](https://travis-ci.org/hedgehogqa/scala-hedgehog)

**NOTE** This project is in some form of beta, and _hasn't_ been used in anger.

> Hedgehog will eat all your bugs.

<img src="https://github.com/hedgehogqa/haskell-hedgehog/raw/master/img/hedgehog-logo.png" width="307" align="right"/>

[Hedgehog](http://hedgehog.qa/) is a modern property-based testing
system, in the spirit of QuickCheck (and ScalaCheck). Hedgehog uses integrated shrinking,
so shrinks obey the invariants of generated values by construction.


## Features

- Integrated shrinking, shrinks obey invariants by construction.
- Abstract state machine testing.
- Generators allow monadic effects.
- Range combinators for full control over the scope of generated numbers and collections.
- SBT test runner


## Usage

**NOTE** This libraries is still a WIP and so for now the easiest way to add the library
of the dependency is as a [subproject](https://www.scala-sbt.org/1.x/docs/Multi-Project.html).

```
lazy val root =
  (project in file("."))
    .dependsOn(RootProject(uri("https://github.com/hedgehogqa/scala-hedgehog.git#master")))
```

Scala Hedgehog comes with a _very_ primitive runner interface, and supports the
[SBT testing extension](https://www.scala-sbt.org/1.x/docs/Testing.html#Using+Extensions).

```
testFrameworks := Seq(TestFramework("hedgehog.sbt.Framework"))
```


## Example

See the [example](example/) module for a complete version.

```scala
import hedgehog._
import hedgehog.Gen._
import hedgehog.runner._

object PropertyTest extends Properties {

  def tests: List[Prop] =
    List(
      Prop("example1", example1)
    )

  def example1: Property[Unit] =
    for {
      x <- Gen.char('a', 'z').log("x")
      y <- integral(Range.linear(0, 50)).log("y")
      _ <- if (y % 2 == 0) discard else success
      _ <- assert(y < 87 && x <= 'r')
    } yield ()
}
```


## Design Considerations


As a general rule, the current API is intended to be direct port of
[haskell-hedgehog](https://github.com/hedgehogqa/haskell-hedgehog), much like
[scalacheck](https://github.com/rickynils/scalacheck) was for [QuickCheck](http://hackage.haskell.org/package/QuickCheck).
The idea being that people familiar with one of the libraries will be comfortable with the other.
It also makes it easier not having to re-invent any wheels (or APIs).
There will obviously be exceptions where Scala forces us to make a different trade-off, such as the
[Gen](src/main/scala/hedgehoge/Gen.scala) type alias of `GenT` to assist with type-inference.


## Alternatives

In Scala there are other property-testing alternatives:

- https://github.com/melrief/sonic

  This is another port of Hedgegot, based on cats and monix.

- https://github.com/rickynils/scalacheck
- https://github.com/scalaprops/scalaprops
- https://github.com/japgolly/nyaya
- https://github.com/scalaz/testz

  A more general FP testing library, with
  [plans to implementing hedgehog-like shrinking](https://github.com/scalaz/testz/issues/5)
