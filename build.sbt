ThisBuild / version := "0.1.0"

ThisBuild / scalaVersion := "3.4.0"

lazy val root = (project in file("."))
  .settings(
    name := "in-memory-caching-lib",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.18" % "test"
  )
