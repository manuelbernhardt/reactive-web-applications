import com.typesafe.sbt.packager.archetypes.ServerLoader

name := """ch10"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(
    PlayScala,
    DebianPlugin,
    JavaServerAppPackaging
  )

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "org.webjars" % "jquery" % "2.1.4",
  "org.seleniumhq.selenium" % "selenium-firefox-driver" % "2.53.0",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalatestplus" %% "play" % "1.4.0-M4" % "test"
)

routesGenerator := InjectedRoutesGenerator

pipelineStages := Seq(rjs)

RjsKeys.mainModule := "application"

RjsKeys.mainConfig := "application"

maintainer := "Manuel Bernhardt <manuel@bernhardt.io>"

packageSummary in Linux := "Chapter 10 of Reactive Web Applications"

packageDescription := "This package installs the Play Application used as an example in Chapter 10 of the book Reactive Web Applications (Manning)"

serverLoading in Debian := ServerLoader.Systemd

dockerExposedPorts in Docker := Seq(9000, 9443)
