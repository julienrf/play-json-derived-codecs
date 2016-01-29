organization := "org.julienrf"

name := "play-json-derived-codecs"

enablePlugins(GitVersioning)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless" % "2.2.5",
  "org.specs2" %% "specs2-scalacheck" % "3.7" % "test",
  "com.typesafe.play" %% "play-json" % "2.4.6"
)

publishTo := {
  val nexus = "https://oss.sonatype.org"
  if (isSnapshot.value) Some("snapshots" at s"$nexus/content/repositories/snapshots")
  else Some("releases" at s"$nexus/service/local/staging/deploy/maven2")
}

pomExtra := (
  <url>http://github.com/julienrf/play-json-derived-codecs</url>
    <licenses>
      <license>
        <name>MIT License</name>
        <url>http://opensource.org/licenses/mit-license.php</url>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:julienrf/play-json-derived-codecs.git</url>
      <connection>scm:git:git@github.com:julienrf/play-json-derived-codecs.git</connection>
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

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked",
  "-Yinline-warnings",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused-import",
  "-Ywarn-value-discard",
  "-Xlint",
  "-Xfuture"
)

scalacOptions in Test += "-Yrangepos"