package com.github.enpassant.ickenham

import com.github.jknack.handlebars.{ Context, Handlebars, Template }
import fixiegrips.{ Json4sHelpers, Json4sResolver }
import org.json4s.JsonAST._
import org.scalameter.api._

object IckenhamBench extends Bench.LocalTime {
  val discussion = JObject(
    "_id" -> JString("5"),
    "comments" -> JArray(List(
      JObject(
        "_id" -> JString("5"),
        "commentId" -> JString("7"),
        "userName" -> JString("John"),
        "content" -> JString("<h1>Test comment 1</h1>"),
        "comments" -> JArray(List(
          JObject(
            "_id" -> JString("5"),
            "commentId" -> JString("8"),
            "userName" -> JString("Susan"),
            "content" -> JString("<h2>Reply</h2>"),
            "comments" -> JNothing
          )
        ))
      ),
      JObject(
        "_id" -> JString("5"),
        "commentId" -> JString("9"),
        "userName" -> JString("George"),
        "content" -> JString("<h1>Test comment 2</h1>"),
        "comments" -> JNothing
      )
    ))
  )

  val ranges = for {
    size <- Gen.range("size")(200, 1000, 200)
  } yield 0 until size

  performance of "Ickenham" in {
    measure method "compileAndRender" config (
      exec.benchRuns -> 5,
      exec.minWarmupRuns -> 2,
      exec.maxWarmupRuns -> 5
    ) in {
      using(ranges) in {
        _.map(i => Ickenham.apply("comment")(discussion))
      }
    }
  }

  performance of "Ickenham" in {
    val templates = Ickenham.compile("comment")

    measure method "render" config (
      exec.benchRuns -> 5,
      exec.minWarmupRuns -> 2,
      exec.maxWarmupRuns -> 5
    ) in {
      using(ranges) in {
        _.map { i =>
          templates(discussion)
        }
      }
    }
  }

  performance of "Handlebars" in {
    measure method "compileAndRender" config (
      exec.benchRuns -> 5,
      exec.minWarmupRuns -> 2,
      exec.maxWarmupRuns -> 5
    ) in {
      using(ranges) in {
        _.map { i =>
          val handlebars = new Handlebars().registerHelpers(Json4sHelpers)
          handlebars.setInfiniteLoops(true)
          def ctx(obj: Object) =
            Context.newBuilder(obj).resolver(Json4sResolver).build
          val render = (template: Template) => (obj: Object) => template(ctx(obj))

          val comment = handlebars.compile("comment")
          render(comment)(discussion)
        }
      }
    }
  }

  performance of "Handlebars" in {
    val handlebars = new Handlebars().registerHelpers(Json4sHelpers)
    handlebars.setInfiniteLoops(true)
    def ctx(obj: Object) =
      Context.newBuilder(obj).resolver(Json4sResolver).build
    val render = (template: Template) => (obj: Object) => template(ctx(obj))
    val comment = handlebars.compile("comment")

    measure method "render" config (
      exec.benchRuns -> 5,
      exec.minWarmupRuns -> 2,
      exec.maxWarmupRuns -> 5
    ) in {
      using(ranges) in {
        _.map { i =>
          render(comment)(discussion)
        }
      }
    }
  }

  //performance of "String concat" in {
    //val strings = (1 to 1000).map(str => s"String $str").toList

    //measure method "mkString" config (
      //exec.benchRuns -> 5,
      //exec.minWarmupRuns -> 2,
      //exec.maxWarmupRuns -> 5
    //) in {
      //using(ranges) in {
        //_.map { i =>
          //strings.mkString
        //}
      //}
    //}

    //measure method "StringBuilder" config (
      //exec.benchRuns -> 5,
      //exec.minWarmupRuns -> 2,
      //exec.maxWarmupRuns -> 5
    //) in {
      //using(ranges) in {
        //_.map { i =>
          //val length = strings.foldLeft(0)(_ + _.length)
          //val sb = new java.lang.StringBuilder(length)
          //strings.foreach { str =>
            //sb.append(str)
          //}
          //sb.toString
        //}
      //}
    //}
  //}
}
