package com.github.enpassant.ickenham

import com.github.enpassant.ickenham._

object Main {
  def main(args: Array[String]): Unit = {
    val path = if (args.length > 0) args(0) else "resources"

    val discussion = Map(
      "_id" -> 5,
      "escape" -> "5 < 6",
      "comments" -> List(
        Map(
          "commentId" -> "7",
          "userName" -> "John",
          "content" -> "<h1>Test comment 1</h1>",
          "comments" -> List(
            Map(
              "commentId" -> "8",
              "userName" -> "Susan",
              "content" -> "<h2>Reply</h2>"
            )
          )
        ),
        Map(
          "commentId" -> "9",
          "userName" -> "George",
          "content" -> "<h1>Test comment 2</h1>"
        )
      )
    )

    val ickenham = Ickenham.native[Any]()
    val template = ickenham.compile("comment")

    val count = 100000

    val start = System.nanoTime
    (1 to count) foreach { i =>
      template(discussion)
    }
    val end = System.nanoTime

    println("Html: " + template(discussion))
    println("Render / sec: " + (count.toLong * 1000000000L / (end - start)))
  }
}

