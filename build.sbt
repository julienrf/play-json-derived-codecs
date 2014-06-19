name := "play-json-variants"

organization := "com.github.julienrf"

version := "0.2-SNAPSHOT"

scalaVersion := "2.11.1"

crossScalaVersions := Seq("2.11.1", "2.10.3")

val paradiseVersion = "2.0.0"


addCompilerPlugin("org.scalamacros" % "paradise" % paradiseVersion cross CrossVersion.full)

resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",
  "org.specs2" %% "specs2" % "2.3.12" % "test",
  "com.typesafe.play" %% "play-json" % "2.3.0"
)

libraryDependencies ++= (
  if (scalaVersion.value.startsWith("2.10")) List("org.scalamacros" %% "quasiquotes" % paradiseVersion)
  else Nil
)

publishTo := Some(Resolver.file("Github Pages", Path.userHome / "sites" / "julienrf.github.com" / (if (version.value.trim.endsWith("SNAPSHOT")) "repo-snapshots" else "repo") asFile))

// scalacOptions += "-Ymacro-debug-lite"