organization := "com.github.julienrf"

name := "play-json-variants"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
  "org.specs2" %% "specs2" % "2.3.4" % "test",
  "com.typesafe.play" %% "play-json" % "2.2.1"
)

resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.sonatypeRepo("snapshots")
)

addCompilerPlugin("org.scala-lang.plugins" % "macro-paradise" % "2.0.0-SNAPSHOT" cross CrossVersion.full)

publishTo := Some(Resolver.file("Github Pages", Path.userHome / "sites" / "julienrf.github.com" / (if (version.value.trim.endsWith("SNAPSHOT")) "repo-snapshots" else "repo") asFile))

// scalacOptions += "-Ymacro-debug-lite"