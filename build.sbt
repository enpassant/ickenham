import sbtcrossproject.{crossProject, CrossType}
import com.typesafe.sbt.pgp.PgpKeys.publishSigned

lazy val commonSettings = Seq(
  version := "1.4.0",
  organization := "com.github.enpassant",
  scalaVersion := "2.11.12",
  crossScalaVersions := Seq("2.11.12", "2.12.4"),
  scalacOptions ++= Seq("-feature", "-deprecation"),
  javaOptions += "-Xmx264m"
)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  pomExtra := (
    <url>http://github.com/enpassant/ickenham</url>
    <licenses>
      <license>
        <name>Apache-style</name>
        <url>http://opensource.org/licenses/Apache-2.0</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:enpassant/ickenham.git</url>
      <connection>scm:git:git@github.com:enpassant/ickenham.git</connection>
    </scm>
    <developers>
      <developer>
        <id>enpassant</id>
        <name>Enpassant</name>
        <url>http://github.com/enpassant</url>
      </developer>
    </developers>)
)

lazy val ickenham =
  crossProject(JVMPlatform, NativePlatform)
    .withoutSuffixFor(JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("modules/ickenham"))
    .settings(
      commonSettings,
      publishSettings,
      name := "ickenham"
    )
    .jvmSettings()
    .nativeSettings()

lazy val ickenhamJVM = ickenham.jvm
lazy val ickenhamNative = ickenham.native

lazy val json4s = project
  .in(file("modules/adapters/json4s"))
  .settings(
    commonSettings,
    publishSettings,
    name := "ickenham-json4s",
    libraryDependencies ++= Seq(
      "org.json4s" %% "json4s-jackson" % "3.5.3"
    )
  )
  .dependsOn(ickenhamJVM)

lazy val springmvc = project
  .in(file("modules/spring-mvc"))
  .settings(
    commonSettings,
    publishSettings,
    name := "ickenham-spring-mvc",
    libraryDependencies ++= Seq(
      "org.springframework" % "spring-webmvc" % "3.0.6.RELEASE",
      "javax.servlet" % "servlet-api" % "2.5" % "provided",
      "commons-logging" % "commons-logging-api" % "1.1" % "provided",
      "log4j" % "log4j" % "1.2.16" % "provided",
      "org.slf4j" % "slf4j-api" % "1.6.2" % "provided"
    )
  )
  .dependsOn(ickenhamJVM)

lazy val nativeExample = project
  .in(file("modules/native-example"))
  .settings(
    commonSettings,
    publish := {},
    name := "ickenham-native-example",
    libraryDependencies ++= Seq(
    )
  )
  .dependsOn(ickenhamNative)

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    commonSettings,
    Defaults.itSettings,
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.4" % "it,test",
      "com.storm-enroute" %% "scalameter" % "0.9" % "it,test",
      "com.github.spullara.mustache.java" % "compiler" % "0.9.4" % "it,test",
      "me.lessis" %% "fixie-grips-json4s" % "0.1.0" % "it,test",
      "org.json4s" %% "json4s-jackson" % "3.5.3" % "it,test"
    ),
    publish := {},
    publishLocal := {},
    publishSigned := {},
    testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework")
  )
  .aggregate(json4s)
  .aggregate(springmvc)
  .aggregate(ickenhamJVM)
  .dependsOn(json4s)
