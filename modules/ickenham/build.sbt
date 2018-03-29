name := "ickenham"

organization := "com.github.enpassant"

version := "1.2.1-SNAPSHOT"

scalaVersion := "2.11.11"

crossScalaVersions := Seq("2.11.11", "2.12.4")

scalacOptions ++= Seq("-feature", "-deprecation")

javaOptions += "-Xmx264m"

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

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

