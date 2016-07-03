organization := "org.julienrf"

name := "play-json-derived-codecs"

enablePlugins(GitVersioning)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless" % "2.3.1",
  "org.scalatest" %% "scalatest" % "2.2.6" % Test,
  "org.scalacheck" %% "scalacheck" % "1.12.5" % Test,
  "com.typesafe.play" %% "play-json" % "2.5.2"
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

lazy val homePage = settingKey[File]("Path to the project home page")

lazy val publishDoc = taskKey[Unit]("Publish the documentation")

homePage := Path.userHome / "sites" / "julienrf.github.com"

publishDoc := {
  IO.copyDirectory((doc in Compile).value, homePage.value / "play-json-derived-codecs" / version.value / "api")
}
