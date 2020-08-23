---
title: 'Integration with other test libraries'
sidebar_label: 'Minitest'
slug: '/integration-minitest'
---
## Integration with other test libraries

### Minitest

Scala Hedgehog provides an integration module for [minitest](https://github.com/monix/minitest). This allows you to define property-based and example-based Hedgehog tests within a minitest test suite. If you use this integration, you won't need to Scala Hedgehog sbt testing extension, because you're using the one provided by minitest:

```scala
val hedgehogVersion = "${COMMIT}"
libraryDependencies ++= "qa.hedgehog" %% "hedgehog-minitest" % hedgehogVersion

resolvers += "bintray-scala-hedgehog" at "https://dl.bintray.com/hedgehogqa/scala-hedgehog"

testFrameworks += new TestFramework("minitest.runner.Framework")
```

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
