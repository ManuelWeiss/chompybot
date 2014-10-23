import play.Project._

name := """chompybot"""

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.2.0", 
  "org.webjars" % "bootstrap" % "2.3.1",
  "com.github.nscala-time" %% "nscala-time" % "1.4.0",
  jdbc,
  anorm,
  cache
)

playScalaSettings
