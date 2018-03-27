name := "root"

organization := "com.github.enpassant"

scalaVersion := "2.11.12"

scalacOptions ++= Seq("-feature", "-deprecation")

javaOptions += "-Xmx264m"

coverageEnabled := true

lazy val ickenham = (project in file("modules/ickenham"))

lazy val json4s = (project in file("modules/adapters/json4s"))
  .dependsOn(ickenham)

lazy val springmvc = (project in file("modules/spring-mvc"))
  .dependsOn(ickenham)

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.4" % "it,test",
      "com.storm-enroute" %% "scalameter" % "0.9" % "it,test",
      "com.github.spullara.mustache.java" % "compiler" % "0.9.4" % "it,test",
      "me.lessis" %% "fixie-grips-json4s" % "0.1.0" % "it,test",
      "org.json4s" %% "json4s-jackson" % "3.5.3" % "it,test"
    ),
    testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework")
  )
  .aggregate(ickenham)
  .dependsOn(json4s)
