lazy val scalaV = "2.11.6"

lazy val root = (project in file(".")).settings(
  scalaVersion := scalaV,
  scalaJSProjects := Seq(client),
  pipelineStages := Seq(scalaJSProd),
  libraryDependencies ++= Seq(
    "com.vmunier" %% "play-scalajs-scripts" % "0.2.2"
  ),
  WebKeys.importDirectly := true
).enablePlugins(PlayScala).dependsOn(client).aggregate(client)

lazy val client = (project in file("modules/client")).settings(
  scalaVersion := scalaV,
  persistLauncher := true,
  persistLauncher in Test := false,
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.8.0",
    "biz.enef"     %%% "scalajs-angulate" % "0.2"
  ),
  jsDependencies ++= Seq(
    "org.webjars" % "angularjs" % "1.3.15" / "angular.min.js",
    "org.webjars" % "angularjs" % "1.3.15" / "angular-route.min.js" dependsOn "angular.min.js",
    "org.webjars.bower" % "angular-websocket" % "1.0.13" / "angular-websocket.min.js"
  ),
  skip in packageJSDependencies := false

).enablePlugins(ScalaJSPlugin, ScalaJSPlay, SbtWeb)