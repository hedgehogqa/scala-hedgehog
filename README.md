[![Build Status](https://travis-ci.org/hedgehogqa/scala-hedgehog.svg?branch=master)](https://travis-ci.org/hedgehogqa/scala-hedgehog)

> Hedgehog will eat all your bugs.

<img src="https://github.com/hedgehogqa/haskell-hedgehog/raw/master/img/hedgehog-logo.png" width="307" align="right"/>

[Hedgehog](http://hedgehog.qa/) is a modern property-based testing
system, in the spirit of QuickCheck (and ScalaCheck). Hedgehog uses integrated shrinking,
so shrinks obey the invariants of generated values by construction.

- [Current Status](#current-status)
- [Features](#features)
- [Getting Started](#getting-started)
  - [SBT Binary Dependency](#sbt-binary-dependency)
  - [SBT Source Dependency](#sbt-source-dependency)
  - [SBT Testing](#sbt-testing)
  - [IntelliJ](#intellij)
- [Example](#example)
- [Guides](#guides)
  - [Tutorial](doc/tutorial.md)
  - [Migration from ScalaCheck](doc/migration-scalacheck.md)
- [Motivation](#motivation)
  - [Design Considerations](#design-considerations)
- [Resources](#resources)
- [Alternatives](#alternatives)


## Current Status

This project is still in some form of **early release**. The API may break during this stage
until (if?) there is a wider adoption.

Please drop us a line if you start using scala-hedgehog in anger, we'd love to hear from you.


## Features

- Integrated shrinking, shrinks obey invariants by construction.
- [Abstract state machine testing.](example/src/test/scala/hedgehog/examples/state)
- Range combinators for full control over the scope of generated numbers and collections.
- [SBT test runner](#sbt-testing)
- Currently _no_ external dependencies in the core module


## Getting Started


### SBT Binary Dependency

In your `build.sbt` you will unfortunately need to add a
[custom resolver](https://www.scala-sbt.org/1.x/docs/Resolvers.html#Custom+Layout).
Hedgehog is released for every commit and so the "version" will be a git commit hash.
You can find the [bintray repository here](https://bintray.com/hedgehogqa/scala-hedgehog).

```scala
val hedgehogVersion = "${COMMIT}"

libraryDependencies ++= Seq(
  "hedgehog" %% "hedgehog-core" % hedgehogVersion,
  "hedgehog" %% "hedgehog-runner" % hedgehogVersion,
  "hedgehog" %% "hedgehog-sbt" % hedgehogVersion
)

resolvers += Resolver.url("bintray-scala-hedgehog",
    url("https://dl.bintray.com/hedgehogqa/scala-hedgehog")
  )(Resolver.ivyStylePatterns)
```

### SBT Source Dependency

This project can be added as an SBT [subproject](https://www.scala-sbt.org/1.x/docs/Multi-Project.html).

```scala
// This can also be a branch name, like 'master'`, if you want to live on the edge
val hedgehogVersion = "${COMMIT}"
val hedgehogUri = uri("https://github.com/hedgehogqa/scala-hedgehog.git#" + hedgehogVersion)

lazy val root =
  (project in file("."))
    .dependsOn(ProjectRef(hedgehogUri, "core"))
    .dependsOn(ProjectRef(hedgehogUri, "runner"))
    .dependsOn(ProjectRef(hedgehogUri, "sbt-test"))
```

NOTE: Depending on your scala version(s) SBT might [not resolve](https://github.com/sbt/sbt/issues/2901).

### SBT Testing

Scala Hedgehog comes with a _very_ primitive runner interface, and supports the
[SBT testing extension](https://www.scala-sbt.org/1.x/docs/Testing.html#Using+Extensions).

```
testFrameworks := Seq(TestFramework("hedgehog.sbt.Framework"))
```

### IntelliJ

The IntelliJ scala plugin only has
[hard-coded support for the most popular test frameworks](https://github.com/JetBrains/intellij-scala/tree/idea183.x/scala/runners/src/org/jetbrains/plugins/scala/testingSupport).
While Hedgehog is obviously not included in that list, an may never  be, by extending the runner
`Properties` tests can be run as an application (as `Properties` includes a handy `main` function).
NOTE: This requires the test to be an `object` and _not_ a `class`.


## Example

See the [examples](example/src/main/scala/hedgehog/examples/) module for working versions.

```scala
import hedgehog._
import hedgehog.runner._

object PropertyTest extends Properties {

  def tests: List[Test] =
    List(
      property("reverse", testReverse)
    )

  def testReverse: Property =
    for {
      xs <- Gen.alpha.list(Range.linear(0, 100)).forAll
    } yield xs.reverse.reverse ==== xs
}
```

## Guides

- [Tutorial](doc/tutorial.md)
- [Migration from ScalaCheck](doc/migration-scalacheck.md)


## Motivation

The background and motivation for Hedgehog in general is still best described by the original
author in this excellent presenation:

- [Gens N’ Roses: Appetite for Reduction](https://www.youtube.com/watch?v=AIv_9T0xKEo)

A very quick summary is that the original QuickCheck and it's derivatives (like ScalaCheck)
separate the generation of data from the shrinking, which results in something that cannot be
composed easily. It turns out it's fairly simple to combine them in a single data-type.

If you've used ScalaCheck before, it's exactly the same as writing your normal `Gen` functions,
but now those generated value will shrink without any extra information. Magic!


### Design Considerations


As a general rule, the current Scala API is intended to be _direct_ port of
[haskell-hedgehog](https://github.com/hedgehogqa/haskell-hedgehog), much like
[scalacheck](https://github.com/rickynils/scalacheck) was for [QuickCheck](http://hackage.haskell.org/package/QuickCheck).
The idea being that people familiar with one of the libraries will be comfortable with the other.
It also makes it easier not having to re-invent any wheels (or APIs).
There will obviously be exceptions where Scala forces us to make a different trade-off.
See [haskell-differences](doc/haskell-differences.md) for examples and more explanation.


## Resources

Fortunately there is much in common across property-testing material, and as such the following
are still relevant despite whatever language they are presented with.

### Blogs

- [Choosing properties for property-based testing](https://fsharpforfunandprofit.com/posts/property-based-testing-2/)
- [An introduction to property-based testing](https://fsharpforfunandprofit.com/posts/property-based-testing/)

### Presentations

- [Property-Based Testing The Ugly Parts: Case Studies from Komposition](https://www.youtube.com/watch?v=z2ete8VZnZY) - Oskar Wickström at flatMap (Oslo) 2019
- [Types vs Tests](https://skillsmatter.com/skillscasts/12648-types-vs-tests) - Julien Truffaut at Scala eXchange 2018
- [Appetite for dysfunction](https://www.youtube.com/watch?v=k8k2rwWImy8) - Andrew McCluskey at Compose Melbourne 2018
- [Property-based State Machine Testing](https://www.youtube.com/watch?v=boBD1qhCQ94) - Andrew McCluskey at YOW! Lambda Jam 2018.
- [Gens N’ Roses: Appetite for Reduction](https://www.youtube.com/watch?v=AIv_9T0xKEo) - Jacob Stanley at YOW! Lambda Jam 2017.
- [Find More Bugs with Less Effort](https://www.youtube.com/watch?v=hP-VstNdFGo) - Charles O'Farrell at YOW! Night Singapore 2017.
- [Property-based Testing in Practice](https://www.infoq.com/presentations/hypothesis-afl-property-testing) - Alex Chan at QCon 2017.
- [Functions and Determinism in Property-based Testing](https://www.youtube.com/watch?v=UlZGZh7hfbs) - Erik Osheim at Philly ETE 2017
- [Practical Property-Based Testing](https://www.youtube.com/watch?v=F3XJNt21Ido) - Charles O’Farrell at YOW! Lambda Jam 2015.
- [Property-Based Testing for Better Code](https://www.youtube.com/watch?v=shngiiBfD80) - Jessica Kerr at Midwest.io 2014.
- [I Dream of Gen’ning: ScalaCheck Beyond the Basics](http://functional.tv/post/97738967579) - Kelsey Gilmore-Innis at Scala By The Bay 2014.
- [Testing the Hard Stuff and Staying Sane](http://www.infoq.com/presentations/testing-techniques-case-study) - John Hughes on property-based testing using Quviq QuickCheck.
- [Testing Stateful Systems with ScalaCheck](http://parleys.com/play/53a7d2d0e4b0543940d9e566) - Rickard Nilsson at ScalaDays 2014.

### Books

- [ScalaCheck: The Definitive Guide](https://www.artima.com/shop/scalacheck) by Rickard Nilsson (2014)
- [Property-Based Testing with PropEr, Erlang, and Elixir](https://pragprog.com/book/fhproper/property-based-testing-with-proper-erlang-and-elixir) by Fred Hebert (2018)


## Alternatives

In Scala there are other property-testing alternatives:

- https://github.com/rickynils/scalacheck

  The original port of QuickCheck in Scala.

- https://github.com/melrief/sonic

  This is another port of Hedgehog, based on cats and monix.

- https://github.com/scalaprops/scalaprops

  Makes some improvements on the ScalaCheck implementation.

- https://github.com/japgolly/nyaya

  Another fast data generator and property testing library in Scala.
