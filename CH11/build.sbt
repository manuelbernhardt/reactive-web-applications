name := """CH11"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % Test,
  "org.scalatestplus" %% "play" % "1.4.0-M3" % Test
)

testOptions in Test += Tests.Argument(
  "-F",
  sys.props.getOrElse("SCALING_FACTOR", default = "1")
)

routesGenerator := InjectedRoutesGenerator
