name := "simple-vocabulary-teacher"

version := "1.0"

scalaVersion := "2.11.6"

lazy val `simple-vocabulary-teacher` = (project in file(".")).enablePlugins(PlayScala)

routesGenerator := InjectedRoutesGenerator

com.typesafe.sbt.SbtScalariform.scalariformSettings

routesImport += "binders.PathBinders._"
routesImport += "binders.QueryStringBinders._"

libraryDependencies += filters

