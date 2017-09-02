lazy val commonSettings: Seq[Def.Setting[_]] = Seq(
  crossScalaVersions := Seq("2.11.11", "2.12.3"),
  resolvers += Resolver.sonatypeRepo("releases"),
  resolvers += Resolver.bintrayRepo("scalameta", "maven"),
  libraryDependencies += "org.scalameta" %% "scalameta" % "1.8.0",
  addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M10" cross CrossVersion.full),
  scalacOptions += "-Xplugin-require:macroparadise",
  // temporary workaround for https://github.com/scalameta/paradise/issues/10
  scalacOptions in (Compile, console) ~= (_ filterNot (_ contains "paradise"))
)

// Define macros in the root project.
lazy val root = (project in file("."))
  
commonSettings

name := "taggy"

version := "0.0.1-SNAPSHOT"

organization := "com.acjay"

homepage := Some(url("https://github.com/acjay/taggy"))

licenses +=("MIT", url("https://opensource.org/licenses/MIT"))

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomExtra := (
  <scm>
    <url>git@github.com:acjay/taggy.git</url>
    <connection>scm:git:git@github.com:acjay/taggy.git</connection>
  </scm>
  <developers>
    <developer>
      <id>acjay</id>
      <name>Alan Johnson</name>
      <url>http://www.acjay.com</url>
    </developer>
  </developers>)

// Declare separate project for demo.
lazy val example = project
  .settings(
    commonSettings,
    name := "taggy-example",
    publishArtifact := false,
    libraryDependencies ++= Seq(
      "com.chuusai" %% "shapeless" % "2.3.2"
    )
  )
  .dependsOn(root)
