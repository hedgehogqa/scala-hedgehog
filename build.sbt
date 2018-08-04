import sbt._, Keys._

lazy val noPublish = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

lazy val hedgehog = Project(
    id = "hedgehog"
  , base = file(".")
  )
  .settings(standardSettings)
  .settings(noPublish)
  .aggregate(core, runner, sbtTest, test)

lazy val standardSettings = Seq(
    Defaults.coreDefaultSettings
  , projectSettings
  , compilationSettings
  ).flatten

lazy val projectSettings = Seq(
    name := "hedgehog"
  , version in ThisBuild := "1.0.0"
  , organization := "hedgehog"
  , scalaVersion := "2.11.6"
  , crossScalaVersions := Seq(scalaVersion.value)
  , fork in run  := true
  )

lazy val core = Project(
    id = "core"
  , base = file("core")
  ).settings(standardSettings ++ Seq(
    name := "hedgehog-core"
  ) ++ Seq(libraryDependencies ++= Seq(
  ).flatten))

lazy val example = Project(
    id = "example"
  , base = file("example")
  ).settings(standardSettings ++ Seq(
    name := "hedgehog-example"
  ) ++ Seq(libraryDependencies ++= Seq(
  ))
  ).dependsOn(core, runner, sbtTest)

lazy val runner = Project(
    id = "runner"
  , base = file("runner")
  ).settings(standardSettings ++ Seq(
    name := "hedgehog-runner"
  ) ++ Seq(libraryDependencies ++= Seq(
    ))
  ).dependsOn(core)

lazy val sbtTest = Project(
    id = "sbt-test"
  , base = file("sbt-test")
  ).settings(standardSettings ++ testingSettings ++ Seq(
    name := "hedgehog-sbt"
  ) ++ Seq(libraryDependencies ++= Seq(
      "org.scala-sbt" % "test-interface" % "1.0"
    ))
  ).dependsOn(core, runner)

lazy val test = Project(
      id = "test"
    , base = file("test")
  ).settings(standardSettings ++ Seq(
    name := "hedgehog-test"
  , testFrameworks := Seq(TestFramework("hedgehog.sbt.Framework"))
  ) ++ Seq(libraryDependencies ++= Seq(
    ))
  ).dependsOn(core, runner, sbtTest)

lazy val compilationSettings = Seq(
    javacOptions ++= Seq("-source", "1.6", "-target", "1.6")
  , maxErrors := 10
  , scalacOptions in Compile ++= Seq(
      "-deprecation"
    , "-unchecked"
    , "-feature"
    , "-language:_"
    , "-Ywarn-value-discard"
    , "-Yno-adapted-args"
    , "-Xlint"
    , "-Xfatal-warnings"
    , "-Yinline-warnings"
    , "-Ywarn-unused-import"
    )
  , scalacOptions in (Compile,doc) := Seq("-language:_", "-feature")
  , scalacOptions in (Compile,console) := Seq("-language:_", "-feature")
  , scalacOptions in (Test,console) := Seq("-language:_", "-feature")
  , libraryDependencies += compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.7")
  )

lazy val testingSettings = Seq(
    testFrameworks := Seq(TestFramework("hedgehog.sbt.HedgehogFramework"))
  )
