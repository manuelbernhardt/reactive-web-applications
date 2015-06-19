lazy val scalaV = "2.11.6"

lazy val `ch08` = (project in file(".")).settings(
  scalaVersion := scalaV,
  scalaJSProjects := Seq(client),
  pipelineStages := Seq(scalaJSProd),
  libraryDependencies ++= Seq(
    "com.vmunier" %% "play-scalajs-scripts" % "0.2.2",
    "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
    jdbc,
    "org.jooq" % "jooq" % "3.6.0",
    "org.jooq" % "jooq-codegen-maven" % "3.6.0",
    "org.jooq" % "jooq-meta" % "3.6.0"
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
    "org.webjars.bower" % "angular" % "1.4.0" / "angular.min.js",
    "org.webjars.bower" % "angular-route" % "1.4.0" / "angular-route.min.js" dependsOn "angular.min.js",
    "org.webjars.bower" % "angular-websocket" % "1.0.13" / "dist/angular-websocket.min.js" dependsOn "angular.min.js"
  ),
  skip in packageJSDependencies := false
).enablePlugins(ScalaJSPlugin, ScalaJSPlay, SbtWeb)

val generateJOOQ = taskKey[Seq[File]]("Generate JooQ classes")

val generateJOOQTask = (sourceManaged, dependencyClasspath in Compile, runner in Compile, streams) map { (src, cp, r, s) =>
  toError(r.run("org.jooq.util.GenerationTool", cp.files, Array("conf/chapter7.xml"), s.log))
  ((src / "main/generated") ** "*.scala").get
}

generateJOOQ <<= generateJOOQTask

sourceGenerators in Compile <+= generateJOOQTask
