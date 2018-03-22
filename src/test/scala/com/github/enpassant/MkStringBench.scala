package com.github.enpassant.ickenham

import org.scalameter.api._

object MkStringBench extends Bench.LocalTime {
  val ranges = for {
    size <- Gen.range("size")(200, 1000, 200)
  } yield 0 until size

  performance of "String concat" in {
    val strings = (1 to 1000).map(str => s"String $str").toList

    measure method "mkString" config (
      exec.benchRuns -> 5,
      exec.minWarmupRuns -> 2,
      exec.maxWarmupRuns -> 5
    ) in {
      using(ranges) in {
        _.map { i =>
          strings.mkString
        }
      }
    }

    measure method "StringBuilder" config (
      exec.benchRuns -> 5,
      exec.minWarmupRuns -> 2,
      exec.maxWarmupRuns -> 5
    ) in {
      using(ranges) in {
        _.map { i =>
          val length = strings.foldLeft(0)(_ + _.length)
          val sb = new java.lang.StringBuilder(length)
          strings.foreach { str =>
            sb.append(str)
          }
          sb.toString
        }
      }
    }
  }
}
