---
title: 'Integration with MUnit'
sidebar_position: 2
sidebar_label: 'MUnit'
slug: '/integration-munit'
---
## MUnit

Scala Hedgehog provides an integration module for [munit](https://scalameta.org/munit/). This allows you to define property-based and example-based Hedgehog tests within a munit test suite. If you use this integration, you won't need to Scala Hedgehog sbt testing extension, because you're using the one provided by munit:

```scala
val hedgehogVersion = "@VERSION@"
libraryDependencies += "qa.hedgehog" %% "hedgehog-munit" % hedgehogVersion % Test
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

Here's an example of using `hedgehog-munit`:

```scala
import hedgehog.munit.HedgehogSuite
import hedgehog._

class ReverseSuite extends HedgehogSuite {
  property("reverse alphabetic strings") {
    for {
      xs <- Gen.alpha.list(Range.linear(0, 100)).forAll
    } yield assertEquals(xs.reverse.reverse, xs)
  }
  
  test("reverse hello") {
    withMunitAssertions{ assertions =>
	  assertions.assertEquals("hello".reverse, "olleh")
	}
    "hello".reverse ==== "olleh"
  }
}
```

`HedgehogSuite` provides `munit`-like assertions, along with all the `hedgehog.Result` methods and members, that return results in the standard hedgehog report format while satisfying munit's exception-based test failures.
