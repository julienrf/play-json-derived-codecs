organization := "org.julienrf"

name := "play-json-variants"

version := "1.0.1"

crossScalaVersions := Seq("2.10.4", "2.11.4")

libraryDependencies ++= (Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.specs2" %% "specs2" % "2.3.12" % "test",
  "com.typesafe.play" %% "play-json" % "2.3.0"
) ++ (
  if (scalaVersion.value.startsWith("2.10")) Seq(
    "org.scalamacros" %% "quasiquotes" % "2.0.0",
    compilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)
  ) else Seq.empty
))

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

publishTo := {
  val nexus = "https://oss.sonatype.org"
  if (isSnapshot.value) Some("snapshots" at s"$nexus/content/repositories/snapshots")
  else Some("releases" at s"$nexus/service/local/staging/deploy/maven2")
}

pomExtra := (
  <url>http://github.com/julienrf/play-json-variants</url>
    <licenses>
      <license>
        <name>MIT License</name>
        <url>http://opensource.org/licenses/mit-license.php</url>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:julienrf/play-json-variants.git</url>
      <connection>scm:git:git@github.com:julienrf/play-json-variants.git</connection>
    </scm>
    <developers>
      <developer>
        <id>julienrf</id>
        <name>Julien Richard-Foy</name>
        <url>http://julien.richard-foy.fr</url>
      </developer>
    </developers>
  )

useGpg := true

scalacOptions ++= Seq("-feature", "-Xlint", "-deprecation"/*, "-Ymacro-debug-lite"*/)
