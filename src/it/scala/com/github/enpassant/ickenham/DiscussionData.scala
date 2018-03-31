package com.github.enpassant.ickenham

import com.github.enpassant.ickenham.adapter.Json4sAdapter
import com.github.enpassant.ickenham.adapter.PlainAdapter
import com.github.enpassant.ickenham.adapter.JavaAdapter

import collection.JavaConverters._

import org.json4s.JsonAST._
import org.scalatest._

object DiscussonData {
  val adapter = new Json4sAdapter()

  val discussion = List(JObject(
    "_id" -> JString("5"),
    "escape" -> JString("5 < 6"),
    "comments" -> JArray(List(
      JObject(
        "commentId" -> JString("7"),
        "userName" -> JString("John"),
        "content" -> JString("<h1>Test comment 1</h1>"),
        "comments" -> JArray(List(
          JObject(
            "commentId" -> JString("8"),
            "userName" -> JString("Susan"),
            "content" -> JString("<h2>Reply</h2>"),
            "comments" -> JNothing
          )
        ))
      ),
      JObject(
        "commentId" -> JString("9"),
        "userName" -> JString("George"),
        "content" -> JString("<h1>Test comment 2</h1>"),
        "comments" -> JNothing
      )
    ))
  ))

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

  case class Discussion(_id: Int, escape: String, comments: List[Comment])

  case class Comment(
    commentId: Int,
    userName: String,
    content: String,
    comments: List[Comment] = List())

  val discussionCaseClass = Discussion(5, "5 < 6", List(
    Comment(7, "John", "<h1>Test comment 1</h1>",List(
      Comment(8, "Susan", "<h2>Reply</h2>")
    )),
    Comment(9, "George", "<h1>Test comment 2</h1>")
  ))

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
