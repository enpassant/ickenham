package com.github.enpassant.ickenham

import com.github.enpassant.ickenham.adapter.Json4sAdapter
import com.github.enpassant.ickenham.adapter.PlainAdapter
import com.github.enpassant.ickenham.adapter.JavaAdapter

import collection.JavaConverters._

import com.github.jknack.handlebars.{ Context, Handlebars, Template }
import fixiegrips.{ Json4sHelpers, Json4sResolver }
import org.json4s.JsonAST._
import org.scalameter.api._

object IckenhamBench extends Bench.LocalTime {
  import DiscussonData._

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
        _.map(i => new Ickenham(adapter).compile("comment")(discussion.head))
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
          render(comment)(discussion.head)
        }
      }
    }
  }
}
