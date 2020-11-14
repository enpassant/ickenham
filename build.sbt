import sbtcrossproject.{crossProject, CrossType}
import com.typesafe.sbt.pgp.PgpKeys.publishSigned

val dotty26 = "0.26.0"

val scala2_11 = "2.11.12"
val scala2_12 = "2.12.12"
val scala2_13 = "2.13.3"

val mainVersion = "1.5.0"

lazy val commonSettingsDotty = Seq(
  version := mainVersion,
  organization := "com.github.enpassant",
  scalaVersion := dotty26,
  crossScalaVersions := Seq(
    scala2_11, scala2_12, scala2_13,
    dotty26
  ),
  scalacOptions ++= Seq("-feature", "-deprecation"),
  javaOptions += "-Xmx512m"
)

lazy val commonSettings = Seq(
  version := mainVersion,
  organization := "com.github.enpassant",
  scalaVersion := scala2_13,
  crossScalaVersions := Seq(scala2_11, scala2_12, scala2_13),
  scalacOptions ++= Seq("-feature", "-deprecation"),
  javaOptions += "-Xmx264m"
)

lazy val commonSettingsTest = Seq(
  version := mainVersion,
  organization := "com.github.enpassant",
  scalaVersion := scala2_11,
  crossScalaVersions := Seq(scala2_11),
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
  publishConfiguration := publishConfiguration.value.withOverwrite(true),
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
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "3.2.2" % Test,
      ),
      publishSettings,
      sources in (Compile, doc) := Seq.empty,
      name := "ickenham"
    ).jvmSettings(
      commonSettingsDotty
    ).nativeSettings(
      commonSettingsTest
    )

lazy val ickenhamJVM = ickenham.jvm
lazy val ickenhamNative = ickenham.native

lazy val json4s = project
  .in(file("modules/adapters/json4s"))
  .settings(
    commonSettings,
    publishSettings,
    name := "ickenham-json4s",
    libraryDependencies ++= Seq(
      "com.github.enpassant" %% "ickenham" % mainVersion,
      "org.json4s" %% "json4s-jackson" % "3.6.10"
    )
  )

lazy val springmvc = project
  .in(file("modules/spring-mvc"))
  .settings(
    commonSettings,
    publishSettings,
    name := "ickenham-spring-mvc",
    libraryDependencies ++= Seq(
      "com.github.enpassant" %% "ickenham" % mainVersion,
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
    commonSettingsTest,
    publish := {},
    name := "ickenham-native-example",
    libraryDependencies ++= Seq(
      "com.github.enpassant" %% "ickenham" % mainVersion,
    )
  )

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    commonSettingsTest,
    Defaults.itSettings,
    libraryDependencies ++= Seq(
      "com.github.enpassant" %% "ickenham" % mainVersion % "it,test",
      "com.github.enpassant" %% "ickenham-json4s" % mainVersion % "it,test",
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
