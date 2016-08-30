organization := "org.julienrf"

name := "play-json-derived-codecs"

enablePlugins(GitVersioning)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless" % "2.3.0",
  "org.scalatest" %% "scalatest" % "2.2.6" % Test,
  "org.scalacheck" %% "scalacheck" % "1.12.5" % Test,
  "com.typesafe.play" %% "play-json" % "2.5.2"
)

publishTo := {
  val nexus = "https://oss.sonatype.org"
  if (isSnapshot.value) Some("snapshots" at s"$nexus/content/repositories/snapshots")
  else Some("releases" at s"$nexus/service/local/staging/deploy/maven2")
}

pomExtra :=
  <developers>
    <developer>
      <id>julienrf</id>
      <name>Julien Richard-Foy</name>
      <url>http://julien.richard-foy.fr</url>
    </developer>
  </developers>

homepage := Some(url(s"https://github.com/julienrf/${name.value}"))

licenses := Seq("MIT License" -> url("http://opensource.org/licenses/mit-license.php"))

scmInfo := Some(
  ScmInfo(
    url(s"https://github.com/julienrf/${name.value}"),
    s"scm:git:git@github.com:julienrf/${name.value}.git"
  )
)

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
