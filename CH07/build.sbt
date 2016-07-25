name := "CH07"

version := "1.0"

lazy val `ch07` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

resolvers += "Spy Repository" at "http://files.couchbase.com/maven2"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  evolutions,
  "com.github.mumoshu" %% "play2-memcached-play24" % "0.7.0",
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  "org.jooq" % "jooq" % "3.7.0",
  "org.jooq" % "jooq-codegen-maven" % "3.7.0",
  "org.jooq" % "jooq-meta" % "3.7.0",
  "joda-time" % "joda-time" % "2.7",
  "com.github.ironfish" %% "akka-persistence-mongo-casbah"  % "0.7.6"
)

routesGenerator := InjectedRoutesGenerator

val generateJOOQ = taskKey[Seq[File]]("Generate JooQ classes")

val generateJOOQTask = (baseDirectory, dependencyClasspath in Compile, runner in Compile, streams) map { (base, cp, r, s) =>
  toError(r.run(
    "org.jooq.util.GenerationTool",
    cp.files,
    Array("conf/chapter7.xml"),
    s.log))
  ((base / "app" / "generated") ** "*.scala").get
}

generateJOOQ <<= generateJOOQTask

libraryDependencies += "com.ning" % "async-http-client" % "1.9.29"
