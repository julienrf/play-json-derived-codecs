import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

ThisBuild / scalaVersion := "2.13.3"

ThisBuild / crossScalaVersions := Seq(scalaVersion.value, "2.12.8")

ThisBuild / versionPolicyIntention := Compatibility.BinaryCompatible

val library =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .settings(
      organization := "org.julienrf",
      name := "play-json-derived-codecs",
      libraryDependencies ++= Seq(
        "com.chuusai" %%% "shapeless" % "2.3.3",
        "org.scalatest" %%% "scalatest" % "3.2.3" % Test,
        "org.scalacheck" %%% "scalacheck" % "1.15.2" % Test,
        "org.scalatestplus" %%% "scalacheck-1-15" % "3.2.3.0" % Test,
        "com.typesafe.play" %%% "play-json" % "2.9.2"
      ),
      developers := List(
        Developer(
          "julienrf",
          "Julien Richard-Foy",
          "julien@richard-foy.fr",
          url("http://julien.richard-foy.fr")
        )
      ),
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
          "-Xlint"
        ) ++
        (CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, n)) if n >= 13 => Seq("-Xsource:3")
          case _ => Seq("-Yno-adapted-args", "-Ywarn-unused-import", "-Xfuture")
        })
      },
      Test / scalacOptions += "-Yrangepos"
    )

val libraryJVM = library.jvm
val libraryJS = library.js

val `play-json-derived-codecs` =
  project.in(file(".")).aggregate(libraryJVM, libraryJS)
