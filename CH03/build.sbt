name := "reactive-play-chapter-3"

version := "1.0"

libraryDependencies += "joda-time" % "joda-time" % "2.7"

lazy val main = (project in file(".")).aggregate(play)

lazy val play = (project in file("play")).enablePlugins(PlayScala)
