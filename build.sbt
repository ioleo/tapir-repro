import sbt._
import Dependencies._

lazy val root = (project in file("."))
  .settings(
    name := "tapir-repro",
    scalaVersion := v.scala,
    libraryDependencies ++= rootDeps,
    version := "0.1.0-SNAPSHOT",
    organization := "com.github",
    Compile / scalacOptions ++= Seq(
      "-Ymacro-annotations"
    ),
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("snapshots")
    )
  )
