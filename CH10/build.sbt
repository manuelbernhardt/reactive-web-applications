name := """ch10"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  ws,
  "com.typesafe.play.extras" %% "iteratees-extras" % "1.5.0",
  "com.typesafe.play" %% "play-streams-experimental" % "2.4.2",
  "com.typesafe.akka" % "akka-stream-experimental_2.11" % "1.0"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

resolvers += "Typesafe private" at "https://private-repo.typesafe.com/typesafe/maven-releases"

routesGenerator := InjectedRoutesGenerator
