import sbt.Package.ManifestAttributes

name := "torcsnet"

unmanagedJars in Compile += file("libs/CIContest-driver.jar")

version := "0.2"

crossPaths := false

autoScalaLibrary := false

excludeFilter in unmanagedSources := "DefaultDriverAlgorithm.java"

packageOptions := Seq(ManifestAttributes(
  ("Driver", "DefaultDriver")))

test in assembly := {}

libraryDependencies += "org.apache.commons" % "commons-math3" % "3.5"

libraryDependencies += "com.novocode" % "junit-interface" % "0.10" % "test"

libraryDependencies += "org.ini4j" % "ini4j" % "0.5.4"

libraryDependencies += "junit" % "junit" % "4.11" % "test"

libraryDependencies += "org.knowm.xchart" % "xchart" % "2.6.0" exclude("de.erichseifert.vectorgraphics2d", "VectorGraphics2D") withSources()

libraryDependencies += "com.thoughtworks.xstream" % "xstream" % "1.3.1" % "provided"

libraryDependencies += "org.encog" % "encog-core" % "3.3.0"
