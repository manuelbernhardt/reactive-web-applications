lazy val scalaV = "2.11.6"

lazy val root = (project in file(".")).settings(
  scalaVersion := scalaV,
  scalaJSProjects := Seq(client),
  pipelineStages := Seq(scalaJSProd),
  libraryDependencies ++= Seq(
    "com.vmunier" %% "play-scalajs-scripts" % "0.2.2"
  )
).enablePlugins(PlayScala).aggregate(client)

lazy val client = (project in file("modules/client")).settings(
  scalaVersion := scalaV,
  persistLauncher := true,
  persistLauncher in Test := false,
  unmanagedSourceDirectories in Compile := Seq((scalaSource in Compile).value),
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.8.0"
  ),
  skip in packageJSDependencies := false

).enablePlugins(ScalaJSPlugin, ScalaJSPlay)