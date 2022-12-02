ThisBuild / organization := "qa.hedgehog"
ThisBuild / developers := List(
  Developer("charleso", "Charles O'Farrell", "charleso@gmail.com", url("https://github.com/charleso")),
)
ThisBuild / homepage := Some(url("https://hedgehog.qa"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/hedgehogqa/scala-hedgehog"),
    "scm:git@github.com:hedgehogqa/scala-hedgehog.git",
  ),
)

ThisBuild / scalaVersion := props.ProjectScalaVersion
ThisBuild / crossScalaVersions := props.CrossScalaVersions
ThisBuild / licenses := List("Apache-2.0" -> url("https://opensource.org/licenses/Apache-2.0"))

lazy val hedgehog = Project(
  id = "hedgehog",
  base = file("."),
)
  .settings(standardSettings)
  .settings(noPublish)
  .aggregate(
    coreJVM, coreJS,
    runnerJVM, runnerJS,
    sbtTestJVM, sbtTestJS,
    testJVM, testJS,
    exampleJVM, exampleJS,
    minitestJVM, minitestJS,
    munitJVM, munitJS
  )

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .in(file("core"))
  .settings(
    standardSettings ++ Seq(
      name := "hedgehog-core",
    ),
  )
lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val example = crossProject(JVMPlatform, JSPlatform)
  .in(file("example"))
  .settings(
    standardSettings ++ noPublish ++ Seq(
      name := "hedgehog-example",
    ),
  )
  .dependsOn(core, runner, sbtTest)
lazy val exampleJVM = example.jvm
lazy val exampleJS = example.js

lazy val runner = crossProject(JVMPlatform, JSPlatform)
  .in(file("runner"))
  .settings(
    standardSettings ++ Seq(
      name := "hedgehog-runner",
    ) ++ Seq(
      libraryDependencies ++= Seq(
        ("org.portable-scala" %%% "portable-scala-reflect" % props.PortableScalaReflectVersion)
          .cross(CrossVersion.for3Use2_13),
      ),
    ),
  )
  .dependsOn(core)
lazy val runnerJVM = runner.jvm
lazy val runnerJS = runner.js

lazy val sbtTest = crossProject(JVMPlatform, JSPlatform)
  .in(file("sbt-test"))
  .settings(
    standardSettings ++ testingSettings ++ Seq(
      name := "hedgehog-sbt",
      libraryDependencies +=
        ("org.portable-scala" %%% "portable-scala-reflect" % props.PortableScalaReflectVersion)
          .cross(CrossVersion.for3Use2_13),
    ),
  )
  .jvmSettings(
    libraryDependencies +=
      "org.scala-sbt" % "test-interface" % "1.0",
  )
  .jsSettings(
    libraryDependencies +=
      ("org.scala-js" %% "scalajs-test-interface" % "1.10.0")
        .cross(CrossVersion.for3Use2_13),
  )
  .dependsOn(core, runner)
lazy val sbtTestJVM = sbtTest.jvm
lazy val sbtTestJS = sbtTest.js

lazy val minitest = crossProject(JVMPlatform, JSPlatform)
  .in(file("minitest"))
  .settings(
    standardSettings ++ Seq(
      name := "hedgehog-minitest",
    ) ++ Seq(
      libraryDependencies ++= Seq(
        ("org.portable-scala" %%% "portable-scala-reflect" % props.PortableScalaReflectVersion)
          .cross(CrossVersion.for3Use2_13),
      ) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2L, 11L)) =>
          Seq("io.monix" %%% "minitest" % props.MinitestVersion_2_11)
        case _               =>
          Seq("io.monix" %%% "minitest" % props.MinitestVersion)
      }),
    ) ++ Seq(
      testFrameworks += TestFramework("minitest.runner.Framework"),
    ),
  )
  .dependsOn(runner)
lazy val minitestJVM = minitest.jvm
lazy val minitestJS = minitest.js

lazy val munit = crossProject(JVMPlatform, JSPlatform)
  .in(file("munit"))
  .settings(
    standardSettings ++ Seq(
      name := "hedgehog-munit",
      libraryDependencies ++= Seq("org.scalameta" %%% "munit" % props.MunitVersion) ++
        (if (scalaBinaryVersion.value.startsWith("2.11")) {
          val silencerVersion = "1.7.8"
          Seq(
            "org.scala-lang.modules" %% "scala-collection-compat" % "2.7.0",
            compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
            "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full,
          )
        } else {
          Seq.empty
        }),
    )
  )
  .dependsOn(runner)

lazy val munitJVM = munit.jvm
lazy val munitJS = munit.js

lazy val test = crossProject(JVMPlatform, JSPlatform)
  .settings(
    standardSettings ++ noPublish ++ Seq(
      name := "hedgehog-test",
    ) ++ testingSettings,
  )
  .dependsOn(core, runner, sbtTest)
lazy val testJVM = test.jvm
lazy val testJS = test.js

lazy val docs = (project in file("generated-docs"))
  .enablePlugins(MdocPlugin, DocusaurPlugin, ScalaUnidocPlugin)
  .settings(
    name := "docs",
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full),
    mdocVariables := Map(
      "VERSION" -> {
        import sys.process._
        "git fetch --tags".!
        val tag = "git rev-list --tags --max-count=1".!!.trim
        s"git describe --tags $tag".!!.trim.stripPrefix("v")
      },
      "SUPPORTED_SCALA_VERSIONS" -> {
        val versions = props
          .CrossScalaVersions
          .map(CrossVersion.binaryScalaVersion)
          .map(v => s"`$v`")
        if (versions.length > 1)
          s"${versions.init.mkString(", ")} and ${versions.last}"
        else
          versions.mkString
      },
    ),
    gitHubPagesPublishRequestTimeout := 60.seconds,
    docusaurDir := (ThisBuild / baseDirectory).value / "website",
    docusaurBuildDir := docusaurDir.value / "build",
    ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(coreJVM, runnerJVM, exampleJVM, minitestJVM, munitJVM),
    ScalaUnidoc / unidoc / target := docusaurDir.value / "static" / "api",
    cleanFiles += (ScalaUnidoc / unidoc / target).value,
    docusaurBuild := docusaurBuild.dependsOn(Compile / unidoc).value,
  )
  .settings(noPublish)
  .dependsOn(coreJVM, runnerJVM, exampleJVM, minitestJVM, munitJVM)

lazy val compilationSettings = Seq(
  maxErrors := 10,
  Compile / scalacOptions ++=
    (if (scalaVersion.value.startsWith("3.")) {
       Seq(
         "-deprecation",
         "-unchecked",
         "-feature",
         "-Xfatal-warnings",
         "-source:3.0-migration",
         "-Ykind-projector",
         "-language:" + List(
           "dynamics",
           "existentials",
           "higherKinds",
           "reflectiveCalls",
           "experimental.macros",
           "implicitConversions",
         ).mkString(",")
       )
     } else {
       Seq(
         "-deprecation",
         "-unchecked",
         "-feature",
         "-language:_",
         "-Ywarn-value-discard",
         "-Xlint",
         "-Xfatal-warnings",
       ) ++ (
         CrossVersion.partialVersion(scalaVersion.value) match {
           case Some((2, 10)) =>
             Seq("-Yno-adapted-args")
           case Some((2, 13)) =>
             Seq.empty
           case _             =>
             Seq("-Yno-adapted-args", "-Ywarn-unused-import")
         }
       )
     }),
  Compile / console / scalacOptions := Seq("-language:_", "-feature"),
  Test / console / scalacOptions := Seq("-language:_", "-feature"),
  Compile / unmanagedSourceDirectories ++= {
    (Compile / unmanagedSourceDirectories).value.map { dir =>
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 13)) | Some((3, _)) =>
          file(dir.getParent + "/scala-2.13+")
        case _                            =>
          file(dir.getParent + "/scala-2.13-")
      }
    }
  },
  libraryDependencies ++= (
    if (scalaVersion.value.startsWith("3."))
      Seq.empty[ModuleID]
    else
      Seq(
        compilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full),
      )
  ),
)

lazy val props = new {
  val ProjectScalaVersion = "2.13.5"
  val CrossScalaVersions = Seq("2.11.12", "2.12.13", ProjectScalaVersion, "3.1.3")

  val PortableScalaReflectVersion = "1.1.1"

  val MinitestVersion_2_11 = "2.8.2"
  val MinitestVersion = "2.9.6"

  val MunitVersion = "0.7.27"
}

lazy val projectSettings: Seq[Setting[_]] = Seq(
  name := "hedgehog",
  run / fork := true,
  licenses := List("Apache-2.0" -> url("https://opensource.org/licenses/Apache-2.0")),
)

lazy val standardSettings: Seq[Setting[_]] = Seq(
  Defaults.coreDefaultSettings,
  projectSettings,
  compilationSettings,
).flatten

lazy val testingSettings = Seq(
  testFrameworks += TestFramework("hedgehog.sbt.Framework"),
)

lazy val noPublish = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false,
  publish / skip := true,
)
