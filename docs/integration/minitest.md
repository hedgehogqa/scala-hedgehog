---
title: 'Integration with Minitest'
sidebar_position: 1
sidebar_label: 'Minitest'
slug: '/integration-minitest'
---
## Minitest

Scala Hedgehog provides an integration module for [minitest](https://github.com/monix/minitest). This allows you to define property-based and example-based Hedgehog tests within a minitest test suite. If you use this integration, you won't need to Scala Hedgehog sbt testing extension, because you're using the one provided by minitest:

```scala
val hedgehogVersion = "@VERSION@"
libraryDependencies += "qa.hedgehog" %% "hedgehog-minitest" % hedgehogVersion % Test
```

:::info NOTE
If you're using sbt version `1.9.0` or **lower**, you need to add the following line to your `build.sbt` file:
```scala
testFrameworks += TestFramework("hedgehog.sbt.Framework")
```
:::

:::info NOTE
For sbt version `1.9.1` or **higher**, this step is not necessary, as [Hedgehog is supported by default](https://github.com/sbt/sbt/pull/7287).
:::

Here's an example of using `hedgehog-minitest`:

```scala
import minitest.SimpleTestSuite
import hedgehog.minitest.HedgehogSupport
import hedgehog._

object ReverseTest extends SimpleTestSuite with HedgehogSupport {
  property("reverse alphabetic strings") {
    for {
      xs <- Gen.alpha.list(Range.linear(0, 100)).forAll
    } yield xs.reverse.reverse ==== xs
  }
  example("reverse hello") {
    "hello".reverse ==== "olleh"
  }
}
```
