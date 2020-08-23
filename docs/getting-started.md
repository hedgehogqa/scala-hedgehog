---
id: 'getting-started'
title: 'Getting Started'
sidebar_label: 'Getting Started'
---

## Getting Started


### SBT Binary Dependency

In your `build.sbt` you will unfortunately need to add a
[custom resolver](https://www.scala-sbt.org/1.x/docs/Resolvers.html#Custom+Layout).
Hedgehog is released for every commit and so the "version" will be a git commit hash.
You can find the [bintray repository here](https://bintray.com/hedgehogqa/scala-hedgehog).

```scala
val hedgehogVersion = "${COMMIT}"

libraryDependencies ++= Seq(
  "qa.hedgehog" %% "hedgehog-core" % hedgehogVersion,
  "qa.hedgehog" %% "hedgehog-runner" % hedgehogVersion,
  "qa.hedgehog" %% "hedgehog-sbt" % hedgehogVersion
)

resolvers += "bintray-scala-hedgehog" at "https://dl.bintray.com/hedgehogqa/scala-hedgehog"
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
testFrameworks += TestFramework("hedgehog.sbt.Framework")
```

### IntelliJ

The IntelliJ scala plugin only has
[hard-coded support for the most popular test frameworks](https://github.com/JetBrains/intellij-scala/tree/idea183.x/scala/runners/src/org/jetbrains/plugins/scala/testingSupport).
While Hedgehog is obviously not included in that list, an may never  be, by extending the runner
`Properties` tests can be run as an application (as `Properties` includes a handy `main` function).
NOTE: This requires the test to be an `object` and _not_ a `class`.

## Example

See the [examples](https://github.com/hedgehogqa/scala-hedgehog/tree/master/example/shared/src/main/scala/hedgehog/examples/) module for working versions.

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

[Guides](guides/)

