package com.github.enpassant.ickenham

import com.github.enpassant.ickenham.adapter.Json4sAdapter
import com.github.enpassant.ickenham.adapter.PlainAdapter
import com.github.enpassant.ickenham.adapter.JavaAdapter

import collection.JavaConverters._

import com.github.jknack.handlebars.{ Context, Handlebars, Template }
import fixiegrips.{ Json4sHelpers, Json4sResolver }
import org.json4s.JsonAST._
import org.scalameter.api._

object IckenhamRenderSimpleBench extends Bench.LocalTime {
  import DiscussonData._

  def templateName = "commentSimple"

  val ranges = for {
    size <- Gen.range("size")(2000, 10000, 2000)
  } yield 0 until size

  performance of "Ickenham" in {
    val templates = new Ickenham(adapter).compile(templateName)

    measure method "render" config (
      exec.benchRuns -> 5,
      exec.minWarmupRuns -> 2,
      exec.maxWarmupRuns -> 5
    ) in {
      using(ranges) in {
        _.map { i =>
          templates(discussion.head)
        }
      }
    }
  }

  performance of "Ickenham with PlainAdapter" in {
    val templates = new Ickenham(adapterPlain).compile(templateName)

    measure method "render" config (
      exec.benchRuns -> 5,
      exec.minWarmupRuns -> 2,
      exec.maxWarmupRuns -> 5
    ) in {
      using(ranges) in {
        _.map { i =>
          templates(discussionPlain.head)
        }
      }
    }
  }

  performance of "Ickenham with JavaAdapter" in {
    val templates = new Ickenham(adapterJava).compile(templateName)

    measure method "render" config (
      exec.benchRuns -> 5,
      exec.minWarmupRuns -> 2,
      exec.maxWarmupRuns -> 5
    ) in {
      using(ranges) in {
        _.map { i =>
          templates(discussionJava.head)
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
    val comment = handlebars.compile(templateName)

    measure method "render" config (
      exec.benchRuns -> 5,
      exec.minWarmupRuns -> 2,
      exec.maxWarmupRuns -> 5
    ) in {
      using(ranges) in {
        _.map { i =>
          render(comment)(discussion.head)
        }
      }
    }
  }
}
