name := """twitter-stream"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

resolvers += "Typesafe private" at "https://private-repo.typesafe.com/typesafe/maven-releases"

libraryDependencies ++= Seq(
  cache,
  ws,
  "com.typesafe.play.extras" %% "iteratees-extras" % "1.4.0"
)