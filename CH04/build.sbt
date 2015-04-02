name := "simple-vocabulary-teacher"

version := "1.0"

scalaVersion := "2.11.5"

lazy val `simple-vocabulary-teacher` = (project in file(".")).enablePlugins(PlayScala)

com.typesafe.sbt.SbtScalariform.scalariformSettings

PlayKeys.routesImport += "binders.PathBinders._"
PlayKeys.routesImport += "binders.QueryStringBinders._"

libraryDependencies += filters

