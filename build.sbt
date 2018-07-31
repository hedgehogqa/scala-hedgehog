import sbt._, Keys._

lazy val scalaz = Seq(
    "org.scalaz" %% "scalaz-core" % "7.2.9"
  , "org.scalaz" %% "scalaz-effect" % "7.2.9"
  )

lazy val scalacheck = Seq(
    "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"
  )

lazy val hedgehog = Project(
    id = "hedgehog"
  , base = file(".")
  )
  .settings(standardSettings)
  .aggregate(core)

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
      scalaz
    , scalacheck
  ).flatten))

lazy val sbtTest = Project(
    id = "sbt-test"
  , base = file("sbt-test")
  ).settings(standardSettings ++ Seq(
    name := "hedgehog-sbt"
  ) ++ Seq(libraryDependencies ++= Seq(
      "org.scala-sbt" % "test-interface" % "1.0"
    ))
  ).dependsOn(core)

lazy val compilationSettings = Seq(
    javacOptions ++= Seq("-source", "1.6", "-target", "1.6")
  , maxErrors := 10
  , scalacOptions in Compile ++= Seq(
      "-target:jvm-1.6"
    , "-deprecation"
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
  , addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3")
  )
