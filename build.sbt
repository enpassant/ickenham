name := "root"

organization := "com.github.enpassant"

//scalaVersion := "2.12.4"

scalaVersion := "2.11.11"

crossScalaVersions := Seq("2.11.11", "2.12.4")

scalacOptions ++= Seq("-feature", "-deprecation")

javaOptions += "-Xmx264m"

lazy val ickenham = (project in file("modules/ickenham"))

lazy val json4s = (project in file("modules/adapters/json4s"))
  .dependsOn(ickenham)

lazy val springmvc = (project in file("modules/spring-mvc"))
  .dependsOn(ickenham)

lazy val root = (project in file("."))
  .settings(
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.4" % "test",
      "com.storm-enroute" %% "scalameter" % "0.9" % "test",
      "com.github.spullara.mustache.java" % "compiler" % "0.9.4" % "test",
      "me.lessis" %% "fixie-grips-json4s" % "0.1.0" % "test",
      "org.json4s" %% "json4s-jackson" % "3.5.3" % "test"
    ),
    testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework")
  )
  .aggregate(ickenham)
  .dependsOn(json4s)
