scalaVersion := "3.2.2"

libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test

lazy val root = (project in file(".")).settings(name := "truck-customs", version := "1.0")