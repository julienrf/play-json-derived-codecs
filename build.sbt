import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

scalaVersion in ThisBuild := "2.12.6"

crossScalaVersions in ThisBuild := Seq(scalaVersion.value, "2.11.12", "2.13.0-M5")

val library =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .enablePlugins(GitVersioning)
    .settings(
      organization := "org.julienrf",
      name := "play-json-derived-codecs",
      libraryDependencies ++= Seq(
        "com.chuusai" %%% "shapeless" % "2.3.3",
        "org.scalatest" %%% "scalatest" % "3.0.6-SNAP6" % Test,
        "org.scalacheck" %%% "scalacheck" % "1.14.0" % Test,
        "com.typesafe.play" %%% "play-json" % "2.7.0"
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
      scalacOptions ++= {
        Seq(
          "-deprecation",
          "-encoding", "UTF-8",
          "-feature",
          "-unchecked",
          "-Ywarn-dead-code",
          "-Ywarn-numeric-widen",
          "-Ywarn-value-discard",
          "-Xlint",
          "-Xfuture"
        ) ++
        (CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, n)) if n >= 13 => Seq("-Xsource:2.14")
          case _ => Seq("-Yno-adapted-args", "-Ywarn-unused-import")
        })
      },
      scalacOptions in Test += "-Yrangepos"
    )

val libraryJVM = library.jvm
val libraryJS = library.js

val `play-json-derived-codecs` =
  project.in(file(".")).aggregate(libraryJVM, libraryJS)
