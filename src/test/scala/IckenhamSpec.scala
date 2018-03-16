package com.github.enpassant.ickenham

import org.json4s.JsonAST._
import org.scalatest._

class IckenhamSpec extends FunSpec with Matchers {
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
            "content" -> JString("<h2>Reply</h2>")
          )
        ))
      ),
      JObject(
        "_id" -> JString("5"),
        "commentId" -> JString("9"),
        "userName" -> JString("George"),
        "content" -> JString("<h1>Test comment 2</h1>")
      )
    ))
  )

  describe("apply") {
    it("should create the expected html") {
      val resultHtml = Ickenham.apply("comment")(discussion)
      val expectedCommentHtml = Ickenham.loadFile("expectedComment.html")
      resultHtml.replaceAll("\\s+", " ") shouldBe
        expectedCommentHtml.replaceAll("\\s+", " ")
    }
  }

  describe("compile") {
    it("should create the expected html") {
      val template = "comment"
      val resultHtml = Ickenham.compile(template)(discussion)
      val expectedCommentHtml = Ickenham.loadFile("expectedComment.html")
      resultHtml.replaceAll("\\s+", " ") shouldBe
        expectedCommentHtml.replaceAll("\\s+", " ")
    }
  }

  describe("parse") {
    it("should create the expected html for simple block") {
      val template = "\n  PREFIX  \n  {{#each comments}}  \n  BLOCK  \n  " +
        "{{#if value}}  \n  CONDITION  \n  {{/if}}  \n  {{/each}}  \n  " +
        "SUFFIX"
      val tags = Ickenham.parse(template)
      val expectedTags = Vector(
        TextTag("\n  PREFIX "),
        BlockTag("each", "comments", Vector(
          TextTag("  \n  BLOCK "),
          BlockTag("if", "value", Vector(
            TextTag("  \n  CONDITION ")
          )),
          TextTag(" ")
        )),
        TextTag(" SUFFIX"))
      tags shouldBe expectedTags
    }

    it("should create the expected html") {
      val template = Ickenham.loadFile("comment.hbs")
      val tags = Ickenham.parse(template)
      val expectedTags = Vector(
        BlockTag("each", "comments", Vector(
          TextTag("""
<div class="comment" id=""""),
          ValueTag("this.commentId"),
          TextTag("""">
    <div>
        <div class="comment-header">
            <div>
                <span class="name">"""),
          ValueTag("userName"),
          TextTag("""</span>
            </div>
            <div>
                <a href="/discussion/"""),
          ValueTag("_id"),
          TextTag("#"),
          ValueTag("this.commentId"),
          TextTag("""" class="button">permalink</a> """),
          BlockTag("if", "../commentId", Vector(
            TextTag("""
                <a href="/discussion/"""),
            ValueTag("_id"),
            TextTag("#"),
            ValueTag("../commentId"),
            TextTag("""" class="button">szülő</a> """)
          )),
          TextTag(""" <a href="/discussion/"""),
          ValueTag("_id"),
          TextTag("/comment/"),
          ValueTag("this.commentId"),
          TextTag("""/new" class="button">Válasz</a>
            </div>
        </div>
        <div>
            """),
          ValueTag("content"),
          TextTag("""
        </div>
    </div> """),
          BlockTag("if", "comments", Vector(
            TextTag(""" """),
            IncludeTag("comment"),
            TextTag(""" """))),
          TextTag(""" </div> """))),
        TextTag(""" """))
      tags shouldBe expectedTags
    }
  }

  describe("searchNextTag") {
    it("should find the next value tag") {
      val template = "PREFIX {{../commentId}} SUFFIX"
      val nextTag = Ickenham.searchNextTag(template)
      nextTag shouldBe
        Some(NextTag("PREFIX ", ValueTag("../commentId"), " SUFFIX"))
    }
    it("should find the next include tag") {
      val template = "PREFIX {{> comment}} SUFFIX"
      val nextTag = Ickenham.searchNextTag(template)
      nextTag shouldBe
        Some(NextTag("PREFIX ", IncludeTag("comment"), " SUFFIX"))
    }
    it("should find the next end tag") {
      val template = "PREFIX \n{{/each}}\n SUFFIX"
      val nextTag = Ickenham.searchNextTag(template)
      nextTag shouldBe Some(NextTag("PREFIX ", EndTag("each"), " SUFFIX"))
    }
    it("should find the next block tag") {
      val template = "PREFIX {{#each comments}} BLOCK {{/each}} SUFFIX"
      val nextTag = Ickenham.searchNextTag(template)
      nextTag shouldBe
        Some(NextTag("PREFIX ",
          BlockTag("each", "comments", Vector(TextTag(" BLOCK "))), " SUFFIX"))
    }
  }

  describe("loadFile") {
    it("should load the specified file") {
      val expectedCommentHtml = Ickenham.loadFile("expectedComment.html")
      expectedCommentHtml should startWith("\n<div class=\"comment\" id=\"7\">")
    }
  }

  describe("assemble") {
    it("should assemble the text tag") {
      val tag = TextTag("Sample Text")
      val templates = Map("test" -> Vector(tag))
      val assembled = Ickenham.assemble("test", templates)(discussion)
      assembled shouldBe "Sample Text"
    }
  }

  describe("assemble") {
    it("should assemble the value tag") {
      val tag = ValueTag("_id")
      val templates = Map("test" -> Vector(tag))
      val assembled = Ickenham.assemble("test", templates)(discussion)
      assembled shouldBe "5"
    }
  }

  describe("assemble") {
    it("should assemble the include tag") {
      val tag = IncludeTag("comment")
      val tagId = ValueTag("_id")
      val templates = Map("test" -> Vector(tag), "comment" -> Vector(tagId))
      val assembled = Ickenham.assemble("test", templates)(discussion)
      assembled shouldBe "5"
    }
  }

  describe("assemble") {
    it("should assemble the each include tag") {
      val test = Vector(BlockTag("each", "comments", Vector(
        IncludeTag("comment"))))
      val comment = Vector(ValueTag("commentId"))
      val templates = Map("test" -> test, "comment" -> comment)
      val assembled = Ickenham.assemble("test", templates)(discussion)
      assembled shouldBe "79"
    }
  }

  describe("assemble") {
    it("should assemble the if include tag") {
      val test = Vector(BlockTag("if", "comments", Vector(
        IncludeTag("comment"))))
      val comment = Vector(ValueTag("_id"))
      val templates = Map("test" -> test, "comment" -> comment)
      val assembled = Ickenham.assemble("test", templates)(discussion)
      assembled shouldBe "5"
    }
  }

  describe("getVariable") {
    it("should get the _id value") {
      val value = Ickenham.getVariable("_id", List(discussion))
      value shouldBe JString("5")
    }
  }

  describe("getVariable") {
    it("should get the name and age values") {
      val json = JObject("person" ->
        JObject("name" -> JString("Joe"), "age" -> JInt(50)))
      val name = Ickenham.getVariable("./person/this/name", List(json))
      val age = Ickenham.getVariable("./person/this/age", List(json))
      (name, age) shouldBe (JString("Joe"), JInt(50))
    }
  }

  describe("getVariable") {
    it("should get the parent name and age values") {
      val json = JObject("person" ->
        JObject("name" -> JString("Joe"), "age" -> JInt(50)))
      val name = Ickenham.getVariable("./person/../person/name", List(json))
      val age = Ickenham.getVariable("./person/../person/age", List(json))
      (name, age) shouldBe (JString("Joe"), JInt(50))
    }
  }
}