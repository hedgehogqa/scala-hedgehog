---
id: 'hedgehog'
title: 'Hedgehog for Scala'
sidebar_label: 'Hedgehog'
slug: /
---
## Hedgehog
[![Release Status](https://github.com/hedgehogqa/scala-hedgehog/workflows/Release/badge.svg)](https://github.com/hedgehogqa/scala-hedgehog/actions?workflow=Release)


|      Project      | Maven Central | Maven Central (JS) |  
|:-----------------:|:-------------:|:-------------:|
|   hedgehog-core   | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/qa.hedgehog/hedgehog-core_2.13/badge.svg)](https://search.maven.org/artifact/qa.hedgehog/hedgehog-core_2.13) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/qa.hedgehog/hedgehog-core_sjs1_2.13/badge.svg)](https://search.maven.org/artifact/qa.hedgehog/hedgehog-core_sjs1_2.13) |
|  hedgehog-runner  | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/qa.hedgehog/hedgehog-runner_2.13/badge.svg)](https://search.maven.org/artifact/qa.hedgehog/hedgehog-runner_2.13) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/qa.hedgehog/hedgehog-runner_sjs1_2.13/badge.svg)](https://search.maven.org/artifact/qa.hedgehog/hedgehog-runner_sjs1_2.13) |
|   hedgehog-sbt    | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/qa.hedgehog/hedgehog-sbt_2.13/badge.svg)](https://search.maven.org/artifact/qa.hedgehog/hedgehog-sbt_2.13) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/qa.hedgehog/hedgehog-sbt_sjs1_2.13/badge.svg)](https://search.maven.org/artifact/qa.hedgehog/hedgehog-sbt_sjs1_2.13) |
| hedgehog-minitest | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/qa.hedgehog/hedgehog-minitest_2.13/badge.svg)](https://search.maven.org/artifact/qa.hedgehog/hedgehog-minitest_2.13) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/qa.hedgehog/hedgehog-minitest_sjs1_2.13/badge.svg)](https://search.maven.org/artifact/qa.hedgehog/hedgehog-minitest_sjs1_2.13) |
|  hedgehog-munit   | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/qa.hedgehog/hedgehog-munit_2.13/badge.svg)](https://search.maven.org/artifact/qa.hedgehog/hedgehog-munit_2.13) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/qa.hedgehog/hedgehog-munit_sjs1_2.13/badge.svg)](https://search.maven.org/artifact/qa.hedgehog/hedgehog-munit_sjs1_2.13) |

* Supported Scala Versions: @SUPPORTED_SCALA_VERSIONS@

> Hedgehog will eat all your bugs.

<img src="../img/hedgehog-logo-256x256.png" align="right"/>

[Hedgehog](http://hedgehog.qa/) is a modern property-based testing
system, in the spirit of QuickCheck (and ScalaCheck). Hedgehog uses integrated shrinking,
so shrinks obey the invariants of generated values by construction.

- [Current Status](#current-status)
- [Features](#features)
- [Getting Started](hedgehog/getting-started.md)
  - [SBT Binary Dependency](hedgehog/getting-started.md#sbt-binary-dependency)
  - [SBT Source Dependency](hedgehog/getting-started.md#sbt-source-dependency)
  - [SBT Testing](hedgehog/getting-started.md#sbt-testing)
  - [IntelliJ](hedgehog/getting-started.md#intellij)
- [Example](hedgehog/getting-started.md#example)
- [Guides](../guides/guides.md)
  - [Tutorial](../guides/tutorial.md)
  - [State Tutorial](../guides/state-tutorial.md)
  - [State Tutorial - Vars](../guides/state-tutorial-vars.md)
  - [Migration from ScalaCheck](../guides/migration-scalacheck.md)
  - [Differences to Haskell Hedgehog](../guides/haskell-differences.md)
- [Motivation](hedgehog/motivation.md)
  - [Design Considerations](hedgehog/motivation.md#design-considerations)
- [Resources](hedgehog/resources.md)
- [Alternatives](hedgehog/alternatives.md)
- [Integration with other test libraries](../integration/integration.md)
  - [Minitest](../integration/minitest.md)
  - [MUnit](../integration/munit.md)

## Current Status

This project is still in some form of **early release**. The API may break during this stage
until (if?) there is a wider adoption.

Please drop us a line if you start using scala-hedgehog in anger, we'd love to hear from you.


## Features

- Integrated shrinking, shrinks obey invariants by construction.
- [Abstract state machine testing.](https://github.com/hedgehogqa/scala-hedgehog/tree/master/example/jvm/src/main/scala/hedgehog/examples/state)
- Range combinators for full control over the scope of generated numbers and collections.
- [SBT test runner](hedgehog/getting-started.md#sbt-testing)
- Currently _no_ external dependencies in the core module

