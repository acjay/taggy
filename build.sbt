import sbtcrossproject.{crossProject, CrossType}
import com.typesafe.sbt.pgp.PgpKeys._

// Project/Maven metadata
name         in ThisBuild :=  "taggy"
version      in ThisBuild :=  "1.0.0"
organization in ThisBuild :=  "com.acjay"
homepage     in ThisBuild :=  Some(url("https://github.com/acjay/taggy"))
licenses     in ThisBuild +=  ("MIT", url("https://opensource.org/licenses/MIT"))
scmInfo      in ThisBuild :=  Some(
                                ScmInfo(
                                  url("https://github.com/acjay/taggy"),
                                  "scm:git@github.com:acjay/taggy.git"
                                )
                              )
developers   in ThisBuild :=  List(
                                Developer(
                                  id    = "acjay",
                                  name  = "Alan Johnson",
                                  email = "alan@breakrs.com",
                                  url   = url("http://www.acjay.com")
                                )
                              )

// Publication config
lazy val publishSettings = Seq(
  moduleName := "taggy",
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false
)

lazy val noPublishSettings = Seq(
    publishTo := Some(Resolver.file("Unused transient repository", target.value / "fakepublish")),
    publishArtifact := false,
    publishLocal := {},
    publishLocalSigned := {},       // doesn't work
    publishSigned := {},            // doesn't work
    packagedArtifacts := Map.empty
)

// Macro config
lazy val commonSettings: Seq[Def.Setting[_]] = Seq(
  crossScalaVersions := Seq("2.11.11", "2.12.3"),
  resolvers += Resolver.sonatypeRepo("releases"),
  resolvers += Resolver.bintrayRepo("scalameta", "maven"),
  libraryDependencies += "org.scalameta" %%% "scalameta" % "1.8.0",
  addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M10" cross CrossVersion.full),
  scalacOptions += "-Xplugin-require:macroparadise",
  // temporary workaround for https://github.com/scalameta/paradise/issues/10
  scalacOptions in (Compile, console) ~= (_ filterNot (_ contains "paradise"))
)

lazy val root = project.in(file("."))
  .aggregate(taggyJS, taggyJVM)
  .settings(noPublishSettings)

// Define macros in the root project.
lazy val taggy = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("."))
  .settings(
    commonSettings, 
    publishSettings,
    name := "taggy")
lazy val taggyJS = taggy.js
lazy val taggyJVM = taggy.jvm

// Declare separate project for demo.
lazy val example = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .settings(
    commonSettings,
    noPublishSettings,
    name := "taggy-example"
  )
  .dependsOn(taggy)
lazy val exampleJS = example.js
lazy val exampleJVM = example.jvm
