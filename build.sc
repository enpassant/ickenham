import mill._, scalalib._, publish._, scalanativelib._
import ammonite.ops._

trait CommonModule extends SbtModule {
  def scalaVersion = "2.12.4"
}

trait CommonPublishModule extends SbtModule with PublishModule {
  def scalaVersion = "2.12.4"
  def publishVersion = "1.5.0-SNAPSHOT"
  def pomSettings = PomSettings(
    description = "Ickenham",
    organization = "com.github.enpassant",
    url = "https://github.com/enpassant/ickenham",
    licenses = Seq(License.`Apache-2.0`),
    versionControl = VersionControl.github("lihaoyi", "mill"),
    developers = Seq(
      Developer("enpassant", "Enpassant","https://github.com/enpassant")
    )
  )
}

object modules extends CommonModule {
  def akkaVersion = "2.5.4"
  def scalacOptions = Seq("-feature", "-deprecation")

  def ivyDeps = Agg(
    ivy"com.lihaoyi::utest::0.5.4",
    ivy"com.lihaoyi::acyclic:0.1.5",
    ivy"com.storm-enroute::scalameter::0.9",
    ivy"com.chuusai::shapeless::2.3.2",
    ivy"com.tumblr::colossus::0.11.0",
    ivy"org.json4s::json4s-jackson::3.5.3",
    ivy"com.typesafe.akka::akka-actor::${akkaVersion}",
    ivy"com.typesafe.akka::akka-persistence::${akkaVersion}",
    ivy"com.typesafe.akka::akka-stream::${akkaVersion}",
    ivy"com.typesafe.akka::akka-stream-kafka::0.17",
    ivy"com.typesafe.akka::akka-slf4j::${akkaVersion}",
    ivy"com.typesafe.akka::akka-http::10.0.6",
    ivy"io.monix::monix::3.0.0-RC1",
    ivy"org.eclipse.jetty:jetty-server::9.4.8.v20171121",
    ivy"ch.qos.logback:logback-classic::1.2.3",
    ivy"org.typelevel::cats-core::1.1.0",
    ivy"org.typelevel::cats-free::1.1.0",
    ivy"org.typelevel::cats-effect::1.0.0-RC",
    ivy"com.github.enpassant::ickenham::1.4.1",
    ivy"org.scalaz::scalaz-zio::0.1.0-dc8b6a3",
    ivy"com.googlecode.json-simple:json-simple::1.1.1"
  )

  object adapters extends CommonModule {
    object json4s extends CommonPublishModule {
      def scalaVersion = "2.12.4"
      def moduleDeps = super.moduleDeps ++ Seq(ickenham)

      def ivyDeps = Agg(
        ivy"org.json4s::json4s-jackson:3.5.3"
      )
    }
  }

  object ickenham extends CommonPublishModule {
  }

  object ickenhamNative extends CommonPublishModule with ScalaNativeModule {
    def scalaNativeVersion = "0.3.8"
    def scalaVersion = "2.11.12"
    override def millSourcePath = super.millSourcePath / up / "ickenham"
  }

  object `native-example` extends CommonPublishModule with ScalaNativeModule {
    def scalaNativeVersion = "0.3.8"
    def scalaVersion = "2.11.12"
    def moduleDeps = super.moduleDeps ++ Seq(modules.ickenhamNative)
    def releaseMode = ReleaseMode.Release
  }

  object `spring-mvc` extends CommonPublishModule {
    def moduleDeps = super.moduleDeps ++ Seq(modules.ickenham)

    def ivyDeps = Agg(
      ivy"org.springframework:spring-webmvc:3.0.6.RELEASE",
      ivy"javax.servlet:servlet-api:2.5",
      ivy"commons-logging:commons-logging-api:1.1",
      ivy"log4j:log4j:1.2.16",
      ivy"org.slf4j:slf4j-api:1.6.2"
    )
  }
}

object main extends CommonModule {
  object test extends Tests {
    override def millSourcePath = super.millSourcePath / up
    def moduleDeps = super.moduleDeps ++
      Seq(modules.ickenhamNative, modules.`spring-mvc`, modules.adapters.json4s)

    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.0.4",
      ivy"com.storm-enroute::scalameter:0.9",
      ivy"com.github.spullara.mustache.java:compiler:0.9.4",
      //ivy"me.lessis::fixie-grips-json4s:0.1.0",
      ivy"org.json4s::json4s-jackson:3.5.3",
      //ivy"com.lihaoyi::utest:0.6.5",
      ivy"org.scalatest::scalatest::3.0.4"
    )
    def testFrameworks = Seq("org.scalatest.tools.Framework")
  }
}
