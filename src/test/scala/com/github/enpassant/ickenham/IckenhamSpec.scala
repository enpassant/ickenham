package com.github.enpassant.ickenham

import com.github.enpassant.ickenham.adapter.Json4sAdapter
import com.github.enpassant.ickenham.adapter.PlainAdapter
import com.github.enpassant.ickenham.adapter.JavaAdapter
import com.github.enpassant.ickenham.stream._

import collection.JavaConverters._

import org.json4s.JsonAST._
import org.scalatest._

class IckenhamSpec extends FunSpec with Matchers {
  import DiscussonData._

  describe("apply with PlainAdapter") {
    it("should create the expected html") {
      val ickenham = new Ickenham(adapterPlain)
      val resultHtml = ickenham.compile("comment")(discussionPlain)
      val expectedCommentHtml = ickenham.loadFile("expectedComment.html")
      resultHtml.replaceAll("\\s+", " ") shouldBe
        expectedCommentHtml.replaceAll("\\s+", " ")
    }
  }

  describe("apply with PlainAdapter and Writer") {
    it("should create the expected html") {
      val ickenham = new Ickenham(adapterPlain)
      val writer = new java.io.StringWriter()
      val stream = new WriterStream(writer)
      ickenham.compileWithStream("comment")(stream)(discussionPlain)
      val resultHtml = writer.toString
      val expectedCommentHtml = ickenham.loadFile("expectedComment.html")
      resultHtml.replaceAll("\\s+", " ") shouldBe
        expectedCommentHtml.replaceAll("\\s+", " ")
    }
  }

  describe("apply with PlainAdapter and OutputStream") {
    it("should create the expected html") {
      val ickenham = new Ickenham(adapterPlain)
      val baos = new java.io.ByteArrayOutputStream()
      val stream = new OutputStreamStream(baos)
      ickenham.compileWithStream("comment")(stream)(discussionPlain)
      val resultHtml = baos.toString
      val expectedCommentHtml = ickenham.loadFile("expectedComment.html")
      resultHtml.replaceAll("\\s+", " ") shouldBe
        expectedCommentHtml.replaceAll("\\s+", " ")
    }
  }

  describe("apply with JavaAdapter") {
    it("should create the expected html") {
      val ickenham = new Ickenham(adapterJava)
      val resultHtml = ickenham.compile("comment")(discussionJava)
      val expectedCommentHtml = ickenham.loadFile("expectedComment.html")
      resultHtml.replaceAll("\\s+", " ") shouldBe
        expectedCommentHtml.replaceAll("\\s+", " ")
    }
  }

  describe("apply") {
    it("should create the expected html") {
      val ickenham = new Ickenham(adapter)
      val resultHtml = ickenham.compile("comment")(discussion)
      val expectedCommentHtml = ickenham.loadFile("expectedComment.html")
      resultHtml.replaceAll("\\s+", " ") shouldBe
        expectedCommentHtml.replaceAll("\\s+", " ")
    }
  }

  describe("compile") {
    it("should create the expected html") {
      val ickenham = new Ickenham(adapter)
      val template = "comment"
      val resultHtml = ickenham.compile(template)(discussion)
      val expectedCommentHtml = ickenham.loadFile("expectedComment.html")
      resultHtml.replaceAll("\\s+", " ") shouldBe
        expectedCommentHtml.replaceAll("\\s+", " ")
    }
  }

  describe("parse") {
    it("should create the expected html for simple block") {
      val ickenham = new Ickenham(adapter)
      val template = "\n  PREFIX  \n  {{#each comments}}  \n  BLOCK  \n  " +
        "{{#if value}}  \n  CONTENT  \n  {{else}}  \n  ELSE  \n  {{/if}}" +
        "  \n  {{/each}}  \n  " +
        "SUFFIX"
      val tags = ickenham.parse(template)
      val expectedTags = Vector(
        TextTag("\n  PREFIX "),
        BlockTag("each", "comments", Vector(
          TextTag("  \n  BLOCK "),
          BlockTag("if", "value", Vector(
            TextTag("  \n  CONTENT ")
          ), Vector(
            TextTag(" ELSE ")
          )),
          TextTag(" ")
        )),
        TextTag(" SUFFIX"))
      tags shouldBe expectedTags
    }

    it("should create the expected html") {
      val ickenham = new Ickenham(adapter)
      val template = ickenham.loadFile("comment.hbs")
      val tags = ickenham.parse(template)
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
          ValueTag("content", false),
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
    it("should find the next escaped value tag") {
      val ickenham = new Ickenham(adapter)
      val template = "PREFIX {{../commentId}} SUFFIX"
      val nextTag = ickenham.searchNextTag(template)
      nextTag shouldBe
        Some(NextTag("PREFIX ", ValueTag("../commentId"), " SUFFIX"))
    }
    it("should find the next unescaped value tag") {
      val ickenham = new Ickenham(adapter)
      val template = "PREFIX {{{../commentId}}} SUFFIX"
      val nextTag = ickenham.searchNextTag(template)
      nextTag shouldBe
        Some(NextTag("PREFIX ", ValueTag("../commentId", false), " SUFFIX"))
    }
    it("should find the next include tag") {
      val ickenham = new Ickenham(adapter)
      val template = "PREFIX {{> comment}} SUFFIX"
      val nextTag = ickenham.searchNextTag(template)
      nextTag shouldBe
        Some(NextTag("PREFIX ", IncludeTag("comment"), " SUFFIX"))
    }
    it("should find the next end tag") {
      val ickenham = new Ickenham(adapter)
      val template = "PREFIX \n{{/each}}\n SUFFIX"
      val nextTag = ickenham.searchNextTag(template)
      nextTag shouldBe Some(NextTag("PREFIX ", EndTag("each"), " SUFFIX"))
    }
    it("should find the next block tag") {
      val ickenham = new Ickenham(adapter)
      val template = "PREFIX {{#each comments}} BLOCK {{/each}} SUFFIX"
      val nextTag = ickenham.searchNextTag(template)
      nextTag shouldBe
        Some(NextTag("PREFIX ",
          BlockTag("each", "comments", Vector(TextTag(" BLOCK "))), " SUFFIX"))
    }
    it("should find the next else tag") {
      val ickenham = new Ickenham(adapter)
      val template = "PREFIX \n{{else}}\n SUFFIX"
      val nextTag = ickenham.searchNextTag(template)
      nextTag shouldBe Some(NextTag("PREFIX ", ElseTag, " SUFFIX"))
    }
    it("should find the next custom helper tag") {
      val ickenham = new Ickenham(adapter)
      val template = "PREFIX {{i18n ../commentId}} SUFFIX"
      val nextTag = ickenham.searchNextTag(template)
      nextTag shouldBe
        Some(NextTag("PREFIX ", HelperTag("i18n", "../commentId"), " SUFFIX"))
    }
  }

  describe("loadFile") {
    it("should load the specified file") {
      val ickenham = new Ickenham(adapter)
      val expectedCommentHtml = ickenham.loadFile("expectedComment.html")
      expectedCommentHtml should startWith("\n<div class=\"comment\" id=\"7\">")
    }
  }

  describe("assemble") {
    it("should assemble the unknown tag") {
      val ickenham = new Ickenham(adapter)
      val tag = ValueTag("content")
      val templates = Map("test" -> Vector(tag))
      val sbs = new StringBuilderStream()
      val assembled = ickenham.assemble("test", templates)(sbs)(discussion)
      assembled shouldBe ""
    }
  }

  describe("assemble") {
    it("should assemble the text tag") {
      val ickenham = new Ickenham(adapter)
      val tag = TextTag("Sample Text")
      val templates = Map("test" -> Vector(tag))
      val sbs = new StringBuilderStream()
      val assembled = ickenham.assemble("test", templates)(sbs)(discussion)
      assembled shouldBe "Sample Text"
    }
  }

  describe("assemble") {
    it("should assemble the escaped value tag") {
      val ickenham = new Ickenham(adapter)
      val tag = ValueTag("escape")
      val templates = Map("test" -> Vector(tag))
      val sbs = new StringBuilderStream()
      val assembled = ickenham.assemble("test", templates)(sbs)(discussion)
      assembled shouldBe "5 &lt; 6"
    }
  }

  describe("assemble") {
    it("should assemble the unescaped value tag") {
      val ickenham = new Ickenham(adapter)
      val tag = ValueTag("escape", false)
      val templates = Map("test" -> Vector(tag))
      val sbs = new StringBuilderStream()
      val assembled = ickenham.assemble("test", templates)(sbs)(discussion)
      assembled shouldBe "5 < 6"
    }
  }

  describe("assemble") {
    it("should assemble the include tag") {
      val ickenham = new Ickenham(adapter)
      val tag = IncludeTag("comment")
      val tagId = ValueTag("_id")
      val templates = Map("test" -> Vector(tag), "comment" -> Vector(tagId))
      val sbs = new StringBuilderStream()
      val assembled = ickenham.assemble("test", templates)(sbs)(discussion)
      assembled shouldBe "5"
    }
  }

  describe("assemble") {
    it("should assemble the each include tag") {
      val ickenham = new Ickenham(adapter)
      val test = Vector(BlockTag("each", "comments", Vector(
        IncludeTag("comment"))))
      val comment = Vector(ValueTag("commentId"))
      val templates = Map("test" -> test, "comment" -> comment)
      val sbs = new StringBuilderStream()
      val assembled = ickenham.assemble("test", templates)(sbs)(discussion)
      assembled shouldBe "79"
    }
  }

  describe("assemble") {
    it("should assemble the root value tag") {
      val ickenham = new Ickenham(adapter)
      val test = Vector(BlockTag("each", "comments", Vector(
        IncludeTag("comment"))))
      val comment = Vector(ValueTag("._id"))
      val templates = Map("test" -> test, "comment" -> comment)
      val sbs = new StringBuilderStream()
      val assembled = ickenham.assemble("test", templates)(sbs)(discussion)
      assembled shouldBe "55"
    }
  }

  describe("assemble") {
    it("should assemble the if include tag") {
      val ickenham = new Ickenham(adapter)
      val test = Vector(BlockTag("if", "comments", Vector(
        IncludeTag("comment"))))
      val comment = Vector(ValueTag("_id"))
      val templates = Map("test" -> test, "comment" -> comment)
      val sbs = new StringBuilderStream()
      val assembled = ickenham.assemble("test", templates)(sbs)(discussion)
      assembled shouldBe "5"
    }
  }

  describe("assemble") {
    it("should assemble the if else tag") {
      val ickenham = new Ickenham(adapter)
      val test = Vector(BlockTag("if", "missing", Vector(
        IncludeTag("comment")), Vector(TextTag("Missing"))))
      val comment = Vector(ValueTag("_id"))
      val templates = Map("test" -> test)
      val sbs = new StringBuilderStream()
      val assembled = ickenham.assemble("test", templates)(sbs)(discussion)
      assembled shouldBe "Missing"
    }
  }

  describe("getVariable") {
    it("should get the nothing value") {
      val ickenham = new Ickenham(adapter)
      val value = ickenham.getVariable("content", List(discussion))
      value shouldBe JString("")
    }
  }

  describe("getVariable") {
    it("should get the _id value") {
      val ickenham = new Ickenham(adapter)
      val value = ickenham.getVariable("_id", List(discussion))
      value shouldBe JString("5")
    }
  }

  describe("getVariable") {
    it("should get the name and age values") {
      val ickenham = new Ickenham(adapter)
      val json = JObject("person" ->
        JObject("name" -> JString("Joe"), "age" -> JInt(50)))
      val name = ickenham.getVariable("./person/this/name", List(json))
      val age = ickenham.getVariable("./person/this/age", List(json))
      (name, age) shouldBe (JString("Joe"), JInt(50))
    }
  }

  describe("getVariable with PlainAdapter") {
    it("should get the name and age values") {
      val ickenham = new Ickenham(adapterPlain)
      val json = Map("person" -> new Person("Joe", 50))
      val name = ickenham.getVariable("./person/this/name", List(json))
      val age = ickenham.getVariable("person/age", List(json))
      (name, age) shouldBe ("Joe", 50)
    }
  }

  describe("getVariable with JavaAdapter") {
    it("should get the name and age values") {
      val ickenham = new Ickenham(adapterJava)
      val json = new Person("Joe", 50)
      val name = ickenham.getVariable("this/name", List(json))
      val age = ickenham.getVariable("this/age", List(json))
      (name, age) shouldBe ("Joe", 50)
    }
  }

  describe("getVariable") {
    it("should get the parent name and age values") {
      val ickenham = new Ickenham(adapter)
      val json = JObject("person" ->
        JObject("name" -> JString("Joe"), "age" -> JInt(50)))
      val name = ickenham.getVariable("./person/../person/name", List(json))
      val age = ickenham.getVariable("./person/../person/age", List(json))
      (name, age) shouldBe (JString("Joe"), JInt(50))
    }
  }
}
