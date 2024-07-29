ThisBuild / scalaVersion := "2.13.12"

lazy val root = project
    .in(file("."))
    .settings(
        name := "cpu",
        libraryDependencies ++= Seq(
            "org.chipsalliance" %% "chisel" % "6.5.0",
            "edu.berkeley.cs" %% "chiseltest" % "6.0.0"
        ),
        scalacOptions ++= Seq(
            "-language:reflectiveCalls",
            "-deprecation",
            "-feature",
            "-Xcheckinit"
        ),
        addCompilerPlugin("org.chipsalliance" %% "chisel-plugin" % "6.5.0" cross CrossVersion.full)
    )
