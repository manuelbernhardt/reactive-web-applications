name := "CH06"

version := "1.0"

lazy val `ch06` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

resolvers += "Akka snapshots" at "http://repo.akka.io/snapshots"

libraryDependencies ++= Seq(
  ws,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23",
  "com.typesafe.akka" %% "akka-actor" % "2.4-SNAPSHOT"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  