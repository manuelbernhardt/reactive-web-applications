name := "CH06"

version := "1.0"

lazy val `ch06` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  ws,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.7.play24",
  "com.typesafe.akka" %% "akka-actor" % "2.4.0",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.0"

)

routesGenerator := InjectedRoutesGenerator

libraryDependencies += "com.ning" % "async-http-client" % "1.9.29"
