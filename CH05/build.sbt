name := "CH05"

version := "1.0"

lazy val `ch05` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  ws,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23"
//  "org.specs2" %% "specs2-core" % "2.4.15" % "test"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  
