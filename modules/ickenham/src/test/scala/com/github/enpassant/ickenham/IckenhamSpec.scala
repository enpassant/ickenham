package com.github.enpassant.ickenham

import com.github.enpassant.ickenham.adapter.PlainAdapter
import com.github.enpassant.ickenham.adapter.JavaAdapter
import com.github.enpassant.ickenham.stream._

import collection.JavaConverters._

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers._

class IckenhamSpec extends AnyFunSpec with should.Matchers {
  import DiscussonData._

  describe("compile with PlainAdapter") {
    it("should create the expected html") {
      val ickenham = new Ickenham(adapterPlain)
      val resultHtml = ickenham.compile("comment")(discussionPlain.head)
      val expectedCommentHtml = Ickenham.loadFile("expectedComment.html")
      resultHtml.replaceAll("\\s+", " ") shouldBe
        expectedCommentHtml.replaceAll("\\s+", " ")
    }
  }

  describe("compile with PlainAdapter and Writer") {
    it("should create the expected html") {
      val ickenham = new Ickenham(adapterPlain)
      val writer = new java.io.StringWriter()
      val stream = new WriterStream(writer)
      ickenham.compileWithStream("comment")(stream)(discussionPlain)
      val resultHtml = writer.toString
      val expectedCommentHtml = Ickenham.loadFile("expectedComment.html")
      resultHtml.replaceAll("\\s+", " ") shouldBe
        expectedCommentHtml.replaceAll("\\s+", " ")
      stream.getResult
    }
  }

  describe("compile with PlainAdapter and OutputStream") {
    it("should create the expected html") {
      val ickenham = new Ickenham(adapterPlain)
      val baos = new java.io.ByteArrayOutputStream()
      val stream = new OutputStreamStream(baos)
      ickenham.compileWithStream("comment")(stream)(discussionPlain)
      stream.getResult
      val resultHtml = baos.toString
      val expectedCommentHtml = Ickenham.loadFile("expectedComment.html")
      resultHtml.replaceAll("\\s+", " ") shouldBe
        expectedCommentHtml.replaceAll("\\s+", " ")
    }
  }

  describe("compile with JavaAdapter") {
    it("should create the expected html") {
      val ickenham = new Ickenham(adapterJava)
      val resultHtml = ickenham.compile("comment")(discussionJava.head)
      val expectedCommentHtml = Ickenham.loadFile("expectedComment.html")
      resultHtml.replaceAll("\\s+", " ") shouldBe
        expectedCommentHtml.replaceAll("\\s+", " ")
    }
  }

  describe("compile") {
    it("should create the expected html") {
      val ickenham = new Ickenham(adapter)
      val resultHtml = ickenham.compile("comment")(discussion.head)
      val expectedCommentHtml = Ickenham.loadFile("expectedComment.html")
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
      val template = Ickenham.loadFile("comment.hbs")
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
        TextTag(" "))
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
      val template =
        "PREFIX {{i18n \"../commentId\" _id \"third parameter\"}} SUFFIX"
      val nextTag = ickenham.searchNextTag(template)
      nextTag shouldBe
        Some(NextTag("PREFIX ", HelperTag(
          "i18n",
          List(
            TextParam("../commentId"),
            VariableParam("_id"),
            TextParam("third parameter"))),
        " SUFFIX"))
    }
  }

  describe("loadFile") {
    it("should load the specified file") {
      val ickenham = new Ickenham(adapter)
      val expectedCommentHtml = Ickenham.loadFile("expectedComment.html")
      expectedCommentHtml should startWith("\n<div class=\"comment\" id=\"7\">")
    }
  }

  describe("assemble") {
    it("should assemble the unknown tag") {
      val ickenham = new Ickenham(adapter)
      val tag = ValueTag("content")
      val tags = Vector(tag)
      val sbs = new StringBuilderStream()
      ickenham.assemble(tags)(sbs)(discussion)
      sbs.getResult shouldBe ""
    }
    it("should assemble the text tag") {
      val ickenham = new Ickenham(adapter)
      val tag = TextTag("Sample Text")
      val tags = Vector(tag)
      val sbs = new StringBuilderStream()
      ickenham.assemble(tags)(sbs)(discussion)
      sbs.getResult shouldBe "Sample Text"
    }
    it("should assemble the helper tag") {
      val i18nMap = Map("example.title" -> "Example title %1s")
      val i18nHelper = (ls: List[Any]) => i18nMap.get(ls.head.toString).map {
        _.format(ls.tail :_*)
      }
      val ickenham = new Ickenham(adapterPlain, Map("i18n" -> i18nHelper))
      val textTag = TextTag("-")
      val tag = HelperTag("i18n", List(
        TextParam("example.title"),
        VariableParam("_id")))
      val tagMissing = HelperTag("i18n", List(TextParam("example.value")))
      val helperMissing =
        HelperTag("capitalize", List(TextParam("example.title")))
      val tags = Vector(tag, textTag, tagMissing, textTag, helperMissing)
      val sbs = new StringBuilderStream()
      ickenham.assemble(tags)(sbs)(discussionPlain)
      sbs.getResult shouldBe ("Example title 5--")
    }
    it("should assemble the escaped value tag") {
      val ickenham = new Ickenham(adapter)
      val tag = ValueTag("escape")
      val tags = Vector(tag)
      val sbs = new StringBuilderStream()
      ickenham.assemble(tags)(sbs)(discussion)
      sbs.getResult shouldBe "5 &lt; 6"
    }
    it("should assemble the unescaped value tag") {
      val ickenham = new Ickenham(adapter)
      val tag = ValueTag("escape", false)
      val tags = Vector(tag)
      val sbs = new StringBuilderStream()
      ickenham.assemble(tags)(sbs)(discussion)
      sbs.getResult shouldBe "5 < 6"
    }

    val loadTemplate = (name: String) => name match {
      case "test" => "{{> comment}}"
      case "comment" => "{{_id}}"
    }

    it("should assemble the include tag") {
      val ickenham = new Ickenham(adapter, loadTemplate=loadTemplate)
      val tag = IncludeTag("comment")
      val tagId = ValueTag("_id")
      val tags = Vector(tag)
      val sbs = new StringBuilderStream()
      ickenham.assemble(tags)(sbs)(discussion)
      sbs.getResult shouldBe "5"
    }

    it("should assemble the each include tag") {
      val loadTemplateEach = (name: String) => name match {
        case "test" => "{{>comment}}"
        case "comment" => "{{commentId}}"
      }

      val ickenham = new Ickenham(adapter, loadTemplate=loadTemplateEach)
      val test = Vector(BlockTag("each", "comments", Vector(
        IncludeTag("comment"))))
      val comment = Vector(ValueTag("commentId"))
      val sbs = new StringBuilderStream()
      ickenham.assemble(test)(sbs)(discussion)
      sbs.getResult shouldBe "79"
    }

    it("should assemble the root value tag") {
      val ickenham = new Ickenham(adapter, loadTemplate=loadTemplate)
      val test = Vector(BlockTag("each", "comments", Vector(
        IncludeTag("comment"))))
      val comment = Vector(ValueTag("._id"))
      val sbs = new StringBuilderStream()
      ickenham.assemble(test)(sbs)(discussion)
      sbs.getResult shouldBe "55"
    }

    it("should assemble the if include tag") {
      val ickenham = new Ickenham(adapter, loadTemplate=loadTemplate)
      val test = Vector(BlockTag("if", "comments", Vector(
        IncludeTag("comment"))))
      val comment = Vector(ValueTag("_id"))
      val sbs = new StringBuilderStream()
      ickenham.assemble(test)(sbs)(discussion)
      sbs.getResult shouldBe "5"
    }

    it("should assemble the if else tag") {
      val ickenham = new Ickenham(adapter, loadTemplate=loadTemplate)
      val test = Vector(BlockTag("if", "missing", Vector(
        IncludeTag("comment")), Vector(TextTag("Missing"))))
      val comment = Vector(ValueTag("_id"))
      val sbs = new StringBuilderStream()
      ickenham.assemble(test)(sbs)(discussion)
      sbs.getResult shouldBe "Missing"
    }

    it("should assemble the if tag with 0") {
      val ickenham = new Ickenham(adapterPlain)
      val test = Vector(BlockTag("if", "int", Vector(
        TextTag("found")), Vector(TextTag("not found"))))
      val sbs = new StringBuilderStream()
      ickenham.assemble(test)(sbs)(List(Map("int" -> 0)))
      sbs.getResult shouldBe "not found"
    }

    it("should assemble the if tag with 0.0") {
      val ickenham = new Ickenham(adapterPlain)
      val test = Vector(BlockTag("if", "double", Vector(
        TextTag("found")), Vector(TextTag("not found"))))
      val sbs = new StringBuilderStream()
      ickenham.assemble(test)(sbs)(List(Map("double" -> 0.0)))
      sbs.getResult shouldBe "not found"
    }

    it("should assemble the if tag with Nil") {
      val ickenham = new Ickenham(adapterPlain)
      val test = Vector(BlockTag("if", "ls", Vector(
        TextTag("found")), Vector(TextTag("not found"))))
      val sbs = new StringBuilderStream()
      ickenham.assemble(test)(sbs)(List(Map("ls" -> Nil)))
      sbs.getResult shouldBe "not found"
    }

    it("should assemble the if tag with null") {
      val ickenham = new Ickenham(adapterPlain)
      val test = Vector(BlockTag("if", "obj", Vector(
        TextTag("found")), Vector(TextTag("not found"))))
      val sbs = new StringBuilderStream()
      ickenham.assemble(test)(sbs)(List(Map("obj" -> null)))
      sbs.getResult shouldBe "not found"
    }

    it("should assemble the if tag with 0 Java") {
      val ickenham = new Ickenham(adapterJava)
      val test = Vector(BlockTag("if", "int", Vector(
        TextTag("found")), Vector(TextTag("not found"))))
      val sbs = new StringBuilderStream()
      val value: java.lang.Integer = new java.lang.Integer(0)
      ickenham.assemble(test)(sbs)(List(Map("int" -> value)))
      sbs.getResult shouldBe "not found"
    }

    it("should assemble the if tag with 0.0 Java") {
      val ickenham = new Ickenham(adapterJava)
      val test = Vector(BlockTag("if", "double", Vector(
        TextTag("found")), Vector(TextTag("not found"))))
      val sbs = new StringBuilderStream()
      val value: java.lang.Double = new java.lang.Double(0.0)
      ickenham.assemble(test)(sbs)(List(Map("double" -> value)))
      sbs.getResult shouldBe "not found"
    }

    it("should assemble the if tag with empty Map Java") {
      val ickenham = new Ickenham(adapterJava)
      val test = Vector(BlockTag("if", "ls", Vector(
        TextTag("found")), Vector(TextTag("not found"))))
      val sbs = new StringBuilderStream()
      val value: java.util.Map[String, String] = Map.empty[String, String].asJava
      ickenham.assemble(test)(sbs)(List(Map("ls" -> value)))
      sbs.getResult shouldBe "not found"
    }

    it("should assemble the if tag with Nil Java") {
      val ickenham = new Ickenham(adapterJava)
      val test = Vector(BlockTag("if", "ls", Vector(
        TextTag("found")), Vector(TextTag("not found"))))
      val sbs = new StringBuilderStream()
      ickenham.assemble(test)(sbs)(List(Map("ls" -> Nil.asJava)))
      sbs.getResult shouldBe "not found"
    }

    it("should assemble the if tag with null Java") {
      val ickenham = new Ickenham(adapterJava)
      val test = Vector(BlockTag("if", "obj", Vector(
        TextTag("found")), Vector(TextTag("not found"))))
      val sbs = new StringBuilderStream()
      val value: java.lang.Double = null
      ickenham.assemble(test)(sbs)(List(Map("obj" -> value)))
      sbs.getResult shouldBe "not found"
    }
  }

  //describe("getVariable") {
    //it("should get the nothing value") {
      //val ickenham = new Ickenham(adapter)
      //val value = ickenham.getVariable("content", discussion)
      //value shouldBe JString("")
    //}
    //it("should get the _id value") {
      //val ickenham = new Ickenham(adapter)
      //val value = ickenham.getVariable("_id", discussion)
      //value shouldBe JString("5")
    //}
    //it("should get the name and age values") {
      //val ickenham = new Ickenham(adapter)
      //val json = JObject("person" ->
        //JObject("name" -> JString("Joe"), "age" -> JInt(50)))
      //val name = ickenham.getVariable("./person/this/name", List(json))
      //val age = ickenham.getVariable("./person/this/age", List(json))
      //(name, age) shouldBe (JString("Joe"), JInt(50))
    //}
    //it("should get the parent name and age values") {
      //val ickenham = new Ickenham(adapter)
      //val json = JObject("person" ->
        //JObject("name" -> JString("Joe"), "age" -> JInt(50)))
      //val name = ickenham.getVariable("./person/../person/name", List(json))
      //val age = ickenham.getVariable("./person/../person/age", List(json))
      //(name, age) shouldBe (JString("Joe"), JInt(50))
    //}
  //}

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
}
