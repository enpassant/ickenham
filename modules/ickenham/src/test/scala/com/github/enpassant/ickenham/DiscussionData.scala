package com.github.enpassant.ickenham

import com.github.enpassant.ickenham.adapter.PlainAdapter
import com.github.enpassant.ickenham.adapter.JavaAdapter

import collection.JavaConverters._

import org.scalatest._

object DiscussonData {
  val adapter = new PlainAdapter()
  val adapterPlain = new PlainAdapter()

  val discussionPlain = List(Map(
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
  ))
  val discussion = discussionPlain

  val adapterJava = new JavaAdapter()

  val discussionJava = List(Map(
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
          ).asJava
        ).asJava
      ).asJava,
      Map(
        "commentId" -> "9",
        "userName" -> "George",
        "content" -> "<h1>Test comment 2</h1>"
      ).asJava
    ).asJava
  ).asJava)

  class Person(val name: String, val age: Int) {
    def getName = name
    def getAge = age
  }
}
