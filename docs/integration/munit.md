---
title: 'Integration with other test libraries'
sidebar_label: 'MUnit'
slug: '/integration-munit'
---
## Integration with other test libraries

### munit

Scala Hedgehog provides an integration module for [munit](https://scalameta.org/munit/). This allows you to define property-based and example-based Hedgehog tests within a munit test suite. If you use this integration, you won't need to Scala Hedgehog sbt testing extension, because you're using the one provided by munit:

```scala
val hedgehogVersion = "@VERSION@"
libraryDependencies += "qa.hedgehog" %% "hedgehog-munit" % hedgehogVersion

testFrameworks += TestFramework("munit.runner.Framework")
```

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
	  asertions.assertEqual("hello".reverse, "olleh")
	}
    "hello".reverse ==== "olleh"
  }
}
```

HedgehogSuite provides `munit`-like assertions, along with all the `hedgehog.Result` methods and members, that return results in the standard hedgehog report format while satisfying munit's exception-based test failures.
