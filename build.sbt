import sbt._, Keys._
import sbtcrossproject.crossProject

lazy val noPublish = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false,
  skip in publish := true
)

lazy val projectSettings: Seq[Setting[_]] = Seq(
    name := "hedgehog"
  , fork in run := true
  )

lazy val standardSettings: Seq[Setting[_]] = Seq(
    Defaults.coreDefaultSettings
  , projectSettings
  , compilationSettings
  , Seq(
      Compile / doc / sources := (Def.taskDyn {
        (if (isDotty.value)
          Def.task(Seq.empty[File])
        else
          Def.task((Compile / doc / sources).value))
      }).value
    )
  ).flatten

ThisBuild / organization := "qa.hedgehog"
ThisBuild / version := "1.0.0"
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
ThisBuild / scalaVersion := "2.13.3"
ThisBuild / crossScalaVersions := Seq("2.11.12", "2.12.12", scalaVersion.value, "3.0.0-M2")

lazy val hedgehog = Project(
    id = "hedgehog"
  , base = file(".")
  )
  .settings(standardSettings)
  .settings(noPublish)
  .aggregate(coreJVM, coreJS, runnerJVM, runnerJS, sbtTestJVM, sbtTestJS, testJVM, testJS, exampleJVM, exampleJS, minitestJVM, minitestJS)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .in(file("core"))
  .settings(standardSettings ++ bintrarySettings ++ Seq(
    name := "hedgehog-core"
  ) ++ Seq(libraryDependencies ++= Seq(
  ).flatten))
lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val example = crossProject(JVMPlatform, JSPlatform)
  .in(file("example"))
  .settings(standardSettings ++ noPublish ++ Seq(
    name := "hedgehog-example"
  ) ++ Seq(libraryDependencies ++= Seq(
  ))
  ).dependsOn(core, runner, sbtTest)
lazy val exampleJVM = example.jvm
lazy val exampleJS = example.js

lazy val runner = crossProject(JVMPlatform, JSPlatform)
  .in(file("runner"))
  .settings(standardSettings ++ bintrarySettings ++ Seq(
    name := "hedgehog-runner"
  ) ++ Seq(libraryDependencies ++= Seq(
      ("org.portable-scala" %%% "portable-scala-reflect" % "1.0.0")
        .withDottyCompat(scalaVersion.value)
    ))
  ).dependsOn(core)
lazy val runnerJVM = runner.jvm
lazy val runnerJS = runner.js

lazy val sbtTest = crossProject(JVMPlatform, JSPlatform)
  .in(file("sbt-test"))
  .settings(standardSettings ++ testingSettings ++ bintrarySettings ++ Seq(
    name := "hedgehog-sbt",
    libraryDependencies +=
      ("org.portable-scala" %%% "portable-scala-reflect" % "1.0.0")
        .withDottyCompat(scalaVersion.value)
  ))
  .jvmSettings(
    libraryDependencies +=
      ("org.scala-sbt" % "test-interface" % "1.0")
        .withDottyCompat(scalaVersion.value)
  )
  .jsSettings(
    libraryDependencies +=
      ("org.scala-js" %% "scalajs-test-interface" % "1.3.0")
        .withDottyCompat(scalaVersion.value)
  )
  .dependsOn(core, runner)
lazy val sbtTestJVM = sbtTest.jvm
lazy val sbtTestJS = sbtTest.js

lazy val minitest = crossProject(JVMPlatform, JSPlatform)
  .in(file("minitest"))
  .settings(standardSettings ++ bintrarySettings ++ Seq(
    name := "hedgehog-minitest"
  ) ++ Seq(
    libraryDependencies ++= Seq(
        ("org.portable-scala" %%% "portable-scala-reflect" % "1.0.0")
          .withDottyCompat(scalaVersion.value)
      ) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2L, 11L)) =>
          Seq("io.monix" %%% "minitest" % "2.8.2")
        case _ =>
          Seq("io.monix" %%% "minitest" % "2.9.1")
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

lazy val compilationSettings = Seq(
    maxErrors := 10
  , scalacOptions in Compile ++= (if (isDotty.value) {
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
  , scalacOptions in (Compile,console) := Seq("-language:_", "-feature")
  , scalacOptions in (Test,console) := Seq("-language:_", "-feature")
  , unmanagedSourceDirectories in Compile ++= {
      (unmanagedSourceDirectories in Compile).value.map { dir =>
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, 13)) | Some((3, 0)) => file(dir.getPath ++ "-2.13+")
          case _             => file(dir.getPath ++ "-2.13-")
        }
      }
    }
  , libraryDependencies ++= (
      if (isDotty.value)
        Seq.empty[ModuleID]
      else
        Seq(
          compilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3" cross CrossVersion.binary)
        )
    )
  )

lazy val testingSettings = Seq(
    testFrameworks += TestFramework("hedgehog.sbt.Framework")
  )

lazy val bintrarySettings = Seq(
    bintrayOrganization := Some("hedgehogqa")
  , bintrayRepository := sys.env.getOrElse("BINTRAY_REPO", "scala-hedgehog")
  , bintrayVcsUrl := Some("https://github.com/hedgehogqa/scala-hedgehog")
  , licenses += ("Apache-2.0", url("https://opensource.org/licenses/Apache-2.0"))
  )
