name := "CH07"

version := "1.0"

lazy val `ch07` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  "mysql" % "mysql-connector-java" % "5.1.35",
  "org.jooq" % "jooq" % "3.6.0",
  "org.jooq" % "jooq-codegen-maven" % "3.6.0",
  "org.jooq" % "jooq-meta" % "3.6.0"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

val generateJOOQ = taskKey[Seq[File]]("Generate JooQ classes")

val generateJOOQTask = (sourceManaged, dependencyClasspath in Compile, runner in Compile, streams) map { (src, cp, r, s) =>
  toError(r.run("org.jooq.util.GenerationTool", cp.files, Array("conf/authentication.xml"), s.log))
  ((src / "main/generated") ** "*.scala").get
}

generateJOOQ <<= generateJOOQTask

sourceGenerators in Compile <+= generateJOOQTask