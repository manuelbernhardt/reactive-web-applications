name := "CH05"

version := "1.0"

lazy val `ch05` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  ws,
  specs2,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.0.play24"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  
