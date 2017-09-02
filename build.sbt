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

// Define macros in this project.
lazy val macros = (project in file("."))
  .settings(
    commonSettings,
    name := "taggy"
  )

lazy val example = project
  .settings(
    commonSettings,
    name := "taggy-example",
    libraryDependencies ++= Seq(
      "com.chuusai" %% "shapeless" % "2.3.2"
    )
  )
  .dependsOn(macros)
