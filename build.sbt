import sbt._, Keys._

lazy val noPublish = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false,
  skip in publish := true
)

lazy val hedgehog = Project(
    id = "hedgehog"
  , base = file(".")
  )
  .settings(standardSettings)
  .settings(noPublish)
  .aggregate(core, runner, sbtTest, test, example)

lazy val standardSettings = Seq(
    Defaults.coreDefaultSettings
  , projectSettings
  , compilationSettings
  ).flatten

lazy val projectSettings = Seq(
    name := "hedgehog"
  , version in ThisBuild := "1.0.0"
  , organization := "qa.hedgehog"
  , scalaVersion := "2.12.8"
  , crossScalaVersions := Seq("2.10.7", "2.11.12", scalaVersion.value, "2.13.0")
  , fork in run  := true
  )

lazy val core = Project(
    id = "core"
  , base = file("core")
  ).settings(standardSettings ++ bintrarySettings ++ Seq(
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
  ).settings(standardSettings ++ bintrarySettings ++ Seq(
    name := "hedgehog-runner"
  ) ++ Seq(libraryDependencies ++= Seq(
      "com.github.scopt" %% "scopt" % "4.0.0-RC2"
    ))
  ).dependsOn(core)

lazy val sbtTest = Project(
    id = "sbt-test"
  , base = file("sbt-test")
  ).settings(standardSettings ++ testingSettings ++ bintrarySettings ++ Seq(
    name := "hedgehog-sbt"
  ) ++ Seq(libraryDependencies ++= Seq(
      "org.scala-sbt" % "test-interface" % "1.0"
    ))
  ).dependsOn(core, runner)

lazy val test = Project(
      id = "test"
    , base = file("test")
  ).settings(standardSettings ++ noPublish ++ Seq(
    name := "hedgehog-test"
  ) ++ testingSettings ++ Seq(libraryDependencies ++= Seq(
    ))
  ).dependsOn(core, runner, sbtTest)

lazy val compilationSettings = Seq(
    maxErrors := 10
  , scalacOptions in Compile ++= Seq(
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
  , scalacOptions in (Compile,console) := Seq("-language:_", "-feature")
  , scalacOptions in (Test,console) := Seq("-language:_", "-feature")
  , unmanagedSourceDirectories in Compile ++= {
      (unmanagedSourceDirectories in Compile).value.map { dir =>
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, 13)) => file(dir.getPath ++ "-2.13+")
          case _             => file(dir.getPath ++ "-2.13-")
        }
      }
    }
  , libraryDependencies += compilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3" cross CrossVersion.binary)
  )

lazy val testingSettings = Seq(
    testFrameworks += TestFramework("hedgehog.sbt.Framework")
  )

lazy val bintrarySettings = Seq(
    bintrayOrganization := Some("hedgehogqa")
  , bintrayRepository := "scala-hedgehog"
  , licenses += ("Apache-2.0", url("https://opensource.org/licenses/Apache-2.0"))
  )
