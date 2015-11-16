import sbt.Package.ManifestAttributes

name := "torcsnet"

unmanagedJars in Compile += file("libs/CIContest-driver.jar")

version := "0.1"

crossPaths := false

autoScalaLibrary := false

packageOptions := Seq(ManifestAttributes(
  ("Driver", "DefaultDriver")))

libraryDependencies += "org.apache.commons" % "commons-math3" % "3.5"
