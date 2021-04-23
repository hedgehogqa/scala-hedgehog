import sbt._, Keys._

lazy val noPublish = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false,
  publish / skip := true
)

lazy val projectSettings: Seq[Setting[_]] = Seq(
    name := "hedgehog"
  , run / fork := true
  , licenses := List("Apache-2.0" -> url("https://opensource.org/licenses/Apache-2.0"))
  )

lazy val standardSettings: Seq[Setting[_]] = Seq(
    Defaults.coreDefaultSettings
  , projectSettings
  , compilationSettings
  ).flatten

val ProjectScalaVersion = "2.13.5"
val CrossScalaVersions = Seq("2.11.12", "2.12.13", ProjectScalaVersion, "3.0.0-RC1", "3.0.0-RC2")

ThisBuild / organization := "qa.hedgehog"
ThisBuild / developers := List(
    Developer("charleso", "Charles O'Farrell", "charleso@gmail.com", url("https://github.com/charleso"))
  )
ThisBuild / homepage := Some(url("https://hedgehog.qa"))
ThisBuild / scmInfo := Some(
    ScmInfo(
      url("https://github.com/hedgehogqa/scala-hedgehog"),
      "scm:git@github.com:hedgehogqa/scala-hedgehog.git"
    )
  )

ThisBuild / scalaVersion := ProjectScalaVersion
ThisBuild / crossScalaVersions := CrossScalaVersions
ThisBuild / licenses := List("Apache-2.0" -> url("https://opensource.org/licenses/Apache-2.0"))

lazy val hedgehog = Project(
    id = "hedgehog"
  , base = file(".")
  )
  .settings(standardSettings)
  .settings(noPublish)
  .aggregate(coreJVM, coreJS, runnerJVM, runnerJS, sbtTestJVM, sbtTestJS, testJVM, testJS, exampleJVM, exampleJS, minitestJVM, minitestJS)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .in(file("core"))
  .settings(
    standardSettings ++ Seq(
      name := "hedgehog-core"
    )
  )
lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val example = crossProject(JVMPlatform, JSPlatform)
  .in(file("example"))
  .settings(
    standardSettings ++ noPublish ++ Seq(
      name := "hedgehog-example"
    )
  ).dependsOn(core, runner, sbtTest)
lazy val exampleJVM = example.jvm
lazy val exampleJS = example.js

lazy val runner = crossProject(JVMPlatform, JSPlatform)
  .in(file("runner"))
  .settings(standardSettings ++ Seq(
    name := "hedgehog-runner"
  ) ++ Seq(libraryDependencies ++= Seq(
      ("org.portable-scala" %%% "portable-scala-reflect" % portableScalaReflectVersion)
        .withDottyCompat(scalaVersion.value)
    ))
  ).dependsOn(core)
lazy val runnerJVM = runner.jvm
lazy val runnerJS = runner.js

lazy val sbtTest = crossProject(JVMPlatform, JSPlatform)
  .in(file("sbt-test"))
  .settings(standardSettings ++ testingSettings ++ Seq(
    name := "hedgehog-sbt",
    libraryDependencies +=
      ("org.portable-scala" %%% "portable-scala-reflect" % portableScalaReflectVersion)
        .withDottyCompat(scalaVersion.value)
  ))
  .jvmSettings(
    libraryDependencies +=
      ("org.scala-sbt" % "test-interface" % "1.0")
        .withDottyCompat(scalaVersion.value)
  )
  .jsSettings(
    libraryDependencies +=
      ("org.scala-js" %% "scalajs-test-interface" % "1.5.1")
        .withDottyCompat(scalaVersion.value)
  )
  .dependsOn(core, runner)
lazy val sbtTestJVM = sbtTest.jvm
lazy val sbtTestJS = sbtTest.js

lazy val minitest = crossProject(JVMPlatform, JSPlatform)
  .in(file("minitest"))
  .settings(
    standardSettings ++ Seq(
      name := "hedgehog-minitest"
    ) ++ Seq(
      libraryDependencies ++= Seq(
        ("org.portable-scala" %%% "portable-scala-reflect" % portableScalaReflectVersion)
          .withDottyCompat(scalaVersion.value)
      ) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2L, 11L)) =>
          Seq("io.monix" %%% "minitest" % "2.8.2")
        case _ =>
          Seq("io.monix" %%% "minitest" % "2.9.4")
      })
    ) ++ Seq(
      testFrameworks += TestFramework("minitest.runner.Framework")
    )
  ).dependsOn(runner)
lazy val minitestJVM = minitest.jvm
lazy val minitestJS = minitest.js

lazy val test = crossProject(JVMPlatform, JSPlatform)
  .settings(standardSettings ++ noPublish ++ Seq(
    name := "hedgehog-test"
  ) ++ testingSettings ++ Seq(libraryDependencies ++= Seq(
    ))
  ).dependsOn(core, runner, sbtTest)
lazy val testJVM = test.jvm
lazy val testJS = test.js

lazy val docs = (project in file("generated-docs"))
  .enablePlugins(MdocPlugin, DocusaurPlugin)
  .settings(
    name := "docs"
  , mdocVariables := Map(
      "VERSION" -> {
        import sys.process._
        "git fetch --tags".!
        val tag = "git rev-list --tags --max-count=1".!!.trim
        s"git describe --tags $tag".!!.trim.stripPrefix("v")
      },
      "SUPPORTED_SCALA_VERSIONS" -> {
        val versions = CrossScalaVersions
          .map(CrossVersion.binaryScalaVersion)
          .map(v => s"`$v`")
        if (versions.length > 1)
          s"${versions.init.mkString(", ")} and ${versions.last}"
        else
          versions.mkString
      }
    )
  , docusaurDir := (ThisBuild / baseDirectory).value / "website"
  , docusaurBuildDir := docusaurDir.value / "build"

  , gitHubPagesOrgName := "hedgehogqa"
  , gitHubPagesRepoName := "scala-hedgehog"
  )
  .settings(noPublish)
  .dependsOn(coreJVM, runnerJVM, exampleJVM, minitestJVM)


lazy val compilationSettings = Seq(
    maxErrors := 10
  , Compile / scalacOptions ++= (if (scalaVersion.value.startsWith("3.")) {
      Seq(
        "-deprecation"
        , "-unchecked"
        , "-feature"
        , "-Xfatal-warnings"
        , "-source:3.0-migration"
        , "-Ykind-projector"
        , "-language:" + List(
          "dynamics",
          "existentials",
          "higherKinds",
          "reflectiveCalls",
          "experimental.macros",
          "implicitConversions"
        ).mkString(",")
        , "-siteroot", "./dotty-docs"
      )
    } else {
      Seq(
        "-deprecation"
        , "-unchecked"
        , "-feature"
        , "-language:_"
        , "-Ywarn-value-discard"
        , "-Xlint"
        , "-Xfatal-warnings"
      ) ++ (
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, 10)) =>
            Seq("-Yno-adapted-args")
          case Some((2, 13)) =>
            Seq.empty
          case _ =>
            Seq("-Yno-adapted-args", "-Ywarn-unused-import")
        }
      )
    })
  , Compile / console / scalacOptions := Seq("-language:_", "-feature")
  , Test / console / scalacOptions := Seq("-language:_", "-feature")
  , Compile / unmanagedSourceDirectories ++= {
      (Compile / unmanagedSourceDirectories).value.map { dir =>
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, 13)) | Some((3, 0)) => file(dir.getPath ++ "-2.13+")
          case _             => file(dir.getPath ++ "-2.13-")
        }
      }
    }
  , libraryDependencies ++= (
      if (scalaVersion.value.startsWith("3."))
        Seq.empty[ModuleID]
      else
        Seq(
          compilerPlugin("org.typelevel" %% "kind-projector" % "0.11.3" cross CrossVersion.full)
        )
    )
  )

lazy val testingSettings = Seq(
    testFrameworks += TestFramework("hedgehog.sbt.Framework")
  )


lazy val portableScalaReflectVersion = "1.1.1"
