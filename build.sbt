organization := "com.github.julienrf"

name := "play-json-variants"

version := "0.2-SNAPSHOT"

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
  "org.specs2" %% "specs2" % "2.3.12" % "test",
  "com.typesafe.play" %% "play-json" % "2.3.0"
)

resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.0" cross CrossVersion.full)

publishTo := Some(Resolver.file("Github Pages", Path.userHome / "sites" / "julienrf.github.com" / (if (version.value.trim.endsWith("SNAPSHOT")) "repo-snapshots" else "repo") asFile))

// scalacOptions += "-Ymacro-debug-lite"