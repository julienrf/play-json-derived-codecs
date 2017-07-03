scalaVersion in ThisBuild := "2.12.1"

crossScalaVersions in ThisBuild := Seq(scalaVersion.value, "2.11.8")

val library =
  crossProject.crossType(CrossType.Pure)
    .enablePlugins(GitVersioning)
    .settings(
      organization := "org.julienrf",
      name := "play-json-derived-codecs",
      libraryDependencies ++= Seq(
        "com.chuusai" %%% "shapeless" % "2.3.2",
        "org.scalatest" %%% "scalatest" % "3.0.1" % Test,
        "org.scalacheck" %%% "scalacheck" % "1.13.4" % Test,
        "com.typesafe.play" %%% "play-json" % "2.6.0-M3"
      ),
      publishTo := {
        val nexus = "https://oss.sonatype.org"
        if (isSnapshot.value) Some("snapshots" at s"$nexus/content/repositories/snapshots")
        else Some("releases" at s"$nexus/service/local/staging/deploy/maven2")
      },
      pomExtra :=
        <developers>
          <developer>
            <id>julienrf</id>
            <name>Julien Richard-Foy</name>
            <url>http://julien.richard-foy.fr</url>
          </developer>
        </developers>,
      homepage := Some(url(s"https://github.com/julienrf/${name.value}")),
      licenses := Seq("MIT License" -> url("http://opensource.org/licenses/mit-license.php")),
      scmInfo := Some(
        ScmInfo(
          url(s"https://github.com/julienrf/${name.value}"),
          s"scm:git:git@github.com:julienrf/${name.value}.git"
        )
      ),
      scalacOptions ++= Seq(
        "-deprecation",
        "-encoding", "UTF-8",
        "-feature",
        "-unchecked",
        //  "-Yinline-warnings",
        "-Yno-adapted-args",
        "-Ywarn-dead-code",
        "-Ywarn-numeric-widen",
        "-Ywarn-unused-import",
        "-Ywarn-value-discard",
        "-Xlint",
        "-Xfuture"
      ),
      scalacOptions in Test += "-Yrangepos"
    )

val libraryJVM = library.jvm
val libraryJS = library.js

val `play-json-derived-codecs` =
  project.in(file(".")).aggregate(libraryJVM, libraryJS)
