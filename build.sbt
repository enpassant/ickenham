name := """ickenham"""

version := "1.0-SNAPSHOT"

//scalaVersion := "2.12.4"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-feature", "-deprecation")

javaOptions += "-Xmx264m"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "com.storm-enroute" %% "scalameter" % "0.9" % "test",
  "com.github.spullara.mustache.java" % "compiler" % "0.9.4" % "test",
  "me.lessis" %% "fixie-grips-json4s" % "0.1.0" % "test",
  "org.json4s" %% "json4s-jackson" % "3.5.3"
)

testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework")
