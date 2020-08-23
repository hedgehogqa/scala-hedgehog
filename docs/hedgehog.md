---
id: 'hedgehog'
title: 'Hedgehog for Scala'
sidebar_label: 'Hedgehog'
---
## Hedgehog
[![Release Status](https://github.com/hedgehogqa/scala-hedgehog/workflows/Release/badge.svg)](https://github.com/hedgehogqa/scala-hedgehog/actions?workflow=Release)


| Project | Maven Central | Maven Central (JS) | Bintray   |  
|:-------:|:-------------:|:-------------:|:---------:|
| hedgehog-core | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/qa.hedgehog/hedgehog-core_2.13/badge.svg)](https://search.maven.org/artifact/qa.hedgehog/hedgehog-core_2.13) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/qa.hedgehog/hedgehog-core_sjs1_2.13/badge.svg)](https://search.maven.org/artifact/qa.hedgehog/hedgehog-core_sjs1_2.13) | [ ![Download](https://api.bintray.com/packages/hedgehogqa/scala-hedgehog-maven/hedgehog-core/images/download.svg) ](https://bintray.com/hedgehogqa/scala-hedgehog-maven/hedgehog-core/_latestVersion) |
| hedgehog-runner | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/qa.hedgehog/hedgehog-runner_2.13/badge.svg)](https://search.maven.org/artifact/qa.hedgehog/hedgehog-runner_2.13) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/qa.hedgehog/hedgehog-runner_sjs1_2.13/badge.svg)](https://search.maven.org/artifact/qa.hedgehog/hedgehog-runner_sjs1_2.13) | [ ![Download](https://api.bintray.com/packages/hedgehogqa/scala-hedgehog-maven/hedgehog-runner/images/download.svg) ](https://bintray.com/hedgehogqa/scala-hedgehog-maven/hedgehog-runner/_latestVersion) |
| hedgehog-sbt | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/qa.hedgehog/hedgehog-sbt_2.13/badge.svg)](https://search.maven.org/artifact/qa.hedgehog/hedgehog-sbt_2.13) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/qa.hedgehog/hedgehog-sbt_sjs1_2.13/badge.svg)](https://search.maven.org/artifact/qa.hedgehog/hedgehog-sbt_sjs1_2.13) | [ ![Download](https://api.bintray.com/packages/hedgehogqa/scala-hedgehog-maven/hedgehog-sbt/images/download.svg) ](https://bintray.com/hedgehogqa/scala-hedgehog-maven/hedgehog-sbt/_latestVersion) |
| hedgehog-minitest | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/qa.hedgehog/hedgehog-minitest_2.13/badge.svg)](https://search.maven.org/artifact/qa.hedgehog/hedgehog-minitest_2.13) | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/qa.hedgehog/hedgehog-minitest_sjs1_2.13/badge.svg)](https://search.maven.org/artifact/qa.hedgehog/hedgehog-minitest_sjs1_2.13) | [ ![Download](https://api.bintray.com/packages/hedgehogqa/scala-hedgehog-maven/hedgehog-minitest/images/download.svg) ](https://bintray.com/hedgehogqa/scala-hedgehog-maven/hedgehog-minitest/_latestVersion) |

> Hedgehog will eat all your bugs.

<img src="../img/hedgehog-logo-256x256.png" align="right"/>

[Hedgehog](http://hedgehog.qa/) is a modern property-based testing
system, in the spirit of QuickCheck (and ScalaCheck). Hedgehog uses integrated shrinking,
so shrinks obey the invariants of generated values by construction.

- [Current Status](#current-status)
- [Features](#features)
- [Getting Started](getting-started.md)
  - [SBT Binary Dependency](getting-started.md#sbt-binary-dependency)
  - [SBT Source Dependency](getting-started.md#sbt-source-dependency)
  - [SBT Testing](getting-started.md#sbt-testing)
  - [IntelliJ](getting-started.md#intellij)
- [Example](getting-started.md#example)
- [Guides](guides/guides.md)
  - [Tutorial](guides/tutorial.md)
  - [State Tutorial](guides/state-tutorial.md)
  - [State Tutorial - Vars](guides/state-tutorial-vars.md)
  - [Migration from ScalaCheck](guides/migration-scalacheck.md)
  - [Differences to Haskell Hedgehog](guides/haskell-differences.md)
- [Motivation](motivation.md)
  - [Design Considerations](motivation.md#design-considerations)
- [Resources](resources.md)
- [Alternatives](alternatives.md)
- Integration with other test libraries
  - [Minitest](integration/minitest.md)

## Current Status

This project is still in some form of **early release**. The API may break during this stage
until (if?) there is a wider adoption.

Please drop us a line if you start using scala-hedgehog in anger, we'd love to hear from you.


## Features

- Integrated shrinking, shrinks obey invariants by construction.
- [Abstract state machine testing.](https://github.com/hedgehogqa/scala-hedgehog/tree/master/example/jvm/src/main/scala/hedgehog/examples/state)
- Range combinators for full control over the scope of generated numbers and collections.
- [SBT test runner](#sbt-testing)
- Currently _no_ external dependencies in the core module

