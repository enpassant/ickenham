package com.github.enpassant.ickenham

import com.github.enpassant.ickenham.adapter.Adapter
import com.github.enpassant.ickenham.adapter.PlainAdapter
import com.github.enpassant.ickenham.stream._

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

import java.nio.charset.StandardCharsets._
import java.nio.file.{Files, FileSystems, Paths}

import java.util.LinkedHashMap

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.duration._

import scala.util.matching.Regex
import scala.util.Try

import Ickenham._

class Ickenham[T](
  val adapter: Adapter[T],
  val helpers: Helpers[T] = emptyHelpers,
  val loadTemplate: String => String = Ickenham.loadTemplate)
{
  def this(adapter: Adapter[T]) =
    this(adapter, emptyHelpers, Ickenham.loadTemplate)

  val templates = new LinkedHashMap[String, Option[Template[_,T]]]()

  def compile(template: String): T => String = {
    val compiledFn = compileWithStream[String](template)
    val sbs = new StringBuilderStream()
    json =>
      compiledFn(sbs)(List(json))
      sbs.getResult()
  }

  def compileWithStream[R](templateName: String): Stream[R] => List[T] => Unit = {
    if (!templates.containsKey(templateName)) {
      templates.synchronized {
        templates.put(templateName, None)
      }
      val template = loadTemplate(templateName)
      val tags = parse(template)
      val assembledFn = assemble[R](tags)
        .asInstanceOf[Stream[R] => List[T] => Unit]
      templates.synchronized {
        templates.put(templateName, Some(assembledFn))
      }
      assembledFn
    }
    (stream: Stream[R]) => (path: List[T]) =>
      val template = templates.get(templateName).get
        .asInstanceOf[Stream[R] => List[T] => R]
      template(stream)(path)
  }

  def parse(text: String): Vector[Tag] = {
    val collectedTags = collectTags(text, Vector.empty[Tag])
    if (collectedTags.suffix.isEmpty) {
      collectedTags.tags
    } else {
      collectedTags.tags :+ TextTag(collectedTags.suffix)
    }
  }

  def collectTags(text: String, tags: Vector[Tag]): CollectedTags = {
    val nextTag = searchNextTag(text)
    nextTag match {
      case None =>
        CollectedTags("", tags :+ TextTag(text))
      case Some(NextTag(prefix, ElseTag, suffix)) if !prefix.isEmpty =>
        val elseTags = collectTags(suffix, Vector.empty[Tag])
        CollectedTags(elseTags.suffix, tags :+ TextTag(prefix), elseTags.tags)
      case Some(NextTag(prefix, ElseTag, suffix)) =>
        val elseTags = collectTags(suffix, Vector.empty[Tag])
        CollectedTags(elseTags.suffix, tags, elseTags.tags)
      case Some(NextTag(prefix, EndTag(blockName), suffix)) if !prefix.isEmpty =>
        CollectedTags(suffix, tags :+ TextTag(prefix))
      case Some(NextTag(prefix, EndTag(blockName), suffix)) =>
        CollectedTags(suffix, tags)
      case Some(NextTag(prefix, tag, suffix)) if !prefix.isEmpty =>
        collectTags(suffix, tags :+ TextTag(prefix) :+ tag)
      case Some(NextTag(prefix, tag, suffix)) =>
        collectTags(suffix, tags :+ tag)
    }
  }

  def searchNextTag(text: String): Option[NextTag] = {
    val elseTagRegex = """\{\{(else)\}\}"""
    val endTagRegex = """\{\{\/(\w+)\}\}"""
    val valueTagUnescapedRegex = """\{\{\{([_a-zA-Z0-9\.\/]+)\}\}\}"""
    val valueTagRegex = """\{\{([_a-zA-Z0-9\.\/]+)\}\}"""
    val includeTagRegex = """\{\{>\s*([_a-zA-Z0-9\./]+)\}\}"""
    val blockTagRegex = """\{\{#(\w+)\s+([_a-zA-Z0-9\./]+)\}\}"""
    val helperTagRegex = """\{\{(\w+) ([^}]+)\}\}"""
    val regex = new Regex(
      elseTagRegex + "|" +
      endTagRegex + "|" +
      valueTagUnescapedRegex + "|" +
      valueTagRegex + "|" +
      includeTagRegex + "|" +
      blockTagRegex + "|" +
      helperTagRegex
    )
    val matchOpt = regex.findFirstMatchIn(text)
    matchOpt map { m =>
      if (Option(m.group(1)) != None && m.group(1) != "") {
        NextTag(csr(m.before), ElseTag, csl(m.after))
      } else if (Option(m.group(2)) != None && m.group(2) != "") {
        NextTag(csr(m.before), EndTag(m.group(2)), csl(m.after))
      } else if (Option(m.group(3)) != None && m.group(3) != "") {
        NextTag(m.before.toString, ValueTag(m.group(3), false), m.after.toString)
      } else if (Option(m.group(4)) != None && m.group(4) != "") {
        NextTag(m.before.toString, ValueTag(m.group(4)), m.after.toString)
      } else if (Option(m.group(5)) != None && m.group(5) != "") {
        NextTag(csr(m.before), IncludeTag(m.group(5)), csl(m.after))
      } else if (Option(m.group(6)) != None && m.group(6) != "") {
        val collected = collectTags(m.after.toString, Vector.empty[Tag])
        val blockTag =
          BlockTag(m.group(6), m.group(7), collected.tags, collected.elseTags)
        NextTag(csr(m.before), blockTag, collected.suffix)
      } else {
        NextTag(
          m.before.toString,
          HelperTag(m.group(8), parseParameters(m.group(9))),
          m.after.toString)
      }
    }
  }

  private def parseParameters(str: String) = {
    def loop(param: String, pos: Int, params: List[Param]): List[Param] = {
      if (pos >= str.length) {
        params
      } else if (param.length == 0 && str(pos) == ' ') {
        loop(param, pos+1, params)
      } else if (param.length == 0) {
        loop(str(pos).toString, pos+1, params)
      } else if (param(0) == '"' && str(pos) == ' ') {
        loop(param + ' ', pos+1, params)
      } else if (param(0) == '"' && str(pos) == '\\' && str(pos+1) == '"') {
        loop(param + '"', pos+2, params)
      } else if (param(0) == '"' && str(pos) == '"') {
        loop("", pos+1, params :+ TextParam(param.tail))
      } else if (param(0) == '"') {
        loop(param + str(pos), pos+1, params)
      } else if (str(pos) == ' ') {
        loop("", pos+1, params :+ VariableParam(param))
      } else loop(param + str(pos), pos+1, params)
    }

    loop("", 0, Nil)
  }

  private def csl(str: CharSequence) = str.toString.replaceAll("^\\s+", " ")
  private def csr(str: CharSequence) = str.toString.replaceAll("\\s+$", " ")

  def assemble[R](tags: Vector[Tag]): Stream[R] => List[T] => Unit = {
    val substitutedFn = substitute(tags)
    stream => path =>
      substitutedFn(stream)(path)
  }

  def substitute(tags: Vector[Tag]): Stream[_] => List[T] => Unit = {
    val substituted = tags.map { tag =>
      tag match {
        case TextTag(text) =>
          stream: Stream[_] => path: List[T] => stream.push(text)
        case HelperTag(helperName, parameters) =>
          val helper = helpers.get(helperName)
          val params = parameters.map {
            case VariableParam(name) =>
              val names = getVariableNameList(name)
              VariableNameListParam(names)
            case param => param
          }

          stream: Stream[_] => path: List[T] =>
            val resolvedParams = params.map {
              case VariableNameListParam(names) => getVariableLoop(names, path)
              case TextParam(value) => adapter.asValue(value)
              case param => adapter.asValue(param.toString)
            }

            val value = helper.flatMap(_(resolvedParams))
            value foreach { v: Any =>
              stream.push(v.toString)
            }
        case ValueTag(variableName, needEscape) =>
          val names = getVariableNameList(variableName)
          stream: Stream[_] => path: List[T] =>
            val value = getVariableLoop(names, path)
            val result = adapter.extractString(value)
            if (needEscape) {
              stream.push(escape(result))
            } else {
              stream.push(result)
            }
        case IncludeTag(templateName) =>
          compileWithStream(templateName)
            .asInstanceOf[Stream[_] => List[T] => Unit]
        case BlockTag("if", name, content, elseContent) =>
          val substitutedFn = substitute(content)
          val substitutedElseFn = substitute(elseContent)
          val names = getVariableNameList(name)

          stream: Stream[_] => path: List[T] =>
            if (adapter.isEmpty(getVariableLoop(names, path, true))) {
              substitutedElseFn(stream)(path)
            } else {
              substitutedFn(stream)(path)
            }
        case BlockTag("each", name, content, _) =>
          val substitutedFn = substitute(content)
          val names = getVariableNameList(name)

          stream: Stream[_] => path: List[T] =>
            val children = adapter.getChildren(getVariableLoop(names, path))
            children foreach { item =>
              substitutedFn(stream)(item :: path)
            }
        case BlockTag(blockName, name, content, _) =>
          stream: Stream[_] => path: List[T] => stream.push(s"Block: $blockName")
        case _ =>
          stream: Stream[_] => path: List[T] => stream.push("ERROR!")
      }
    }
    stream: Stream[_] => path: List[T] => substituted.foreach(_(stream)(path))
  }

  def escape(string: String) = {
    string
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
  }

  def getVariableNameList(variableName: String): List[String] = {
    variableName
      .replace("..", "_parent_")
      .replace("./", "this/")
      .replace(".", "/")
      .split("/")
      .toList
  }

  def getVariable(variableName: String, path: List[T]): T = {
    val names = getVariableNameList(variableName)
    getVariableLoop(names, path)
  }

  def getVariableLoop(
    names: List[String],
    path: List[T],
    inContext: Boolean = false): T =
  {
    names match {
      case "" :: tail => getVariableLoop(tail, path.last :: Nil, true)
      case "this" :: tail => getVariableLoop(tail, path, true)
      case "_parent_" :: tail => getVariableLoop(tail, path.tail, true)
      case name :: Nil =>
        adapter.getChild(path.head, name) match {
          case Some(value) => value
          case _ if path.tail == Nil || inContext => adapter.asValue("")
          case _ => getVariableLoop(names, path.tail, inContext)
        }
      case name :: tail =>
        adapter.getChild(path.head, name) match {
          case Some(child) => getVariableLoop(tail, child :: path, true)
          case _ => adapter.asValue("")
        }
      case _ => adapter.asValue("Error")
    }
  }
}

object Ickenham {
  type Templates = Map[String, Vector[Tag]]
  type Template[R, T] = Stream[R] => List[T] => Unit
  type Helpers[T] = Map[String, List[T] => Option[Any]]

  def emptyHelpers[T] = Map.empty[String, List[T] => Option[Any]]

  def loadTemplate(name: String): String = loadFile(name + ".hbs")

  def loadFile(fileName: String): String = {
    loadFromInputStream(getClass.getResourceAsStream("/" + fileName))
  }

  def loadTemplateAtCurrentDir(relPath: String)(name: String): String =
    loadFileAtCurrentDir(relPath, name + ".hbs")

  def loadFileAtCurrentDir(relPath: String, fileName: String): String = {
    val path = FileSystems.getDefault.getPath(relPath, fileName)
    new String(Files.readAllBytes(path), UTF_8)
  }

  def loadFromInputStream(is: InputStream) = {
    val reader = new BufferedReader(new InputStreamReader(is))
    val sb = new StringBuilder()
    try {
      var notExit = true
      do {
        val line = reader.readLine()
        notExit = Option(line) != None
        if (notExit) {
          sb.append(line).append("\n")
        }
      } while (notExit)
    } finally {
      reader.close()
    }
    sb.toString()
  }

  def native[T](
    helpers: Helpers[T] = emptyHelpers,
    loadTemplate: String => String =
      Ickenham.loadTemplateAtCurrentDir("templates"),
    adapter: Adapter[T] = new PlainAdapter()) =
  {
    new Ickenham[T](adapter, helpers, loadTemplate)
  }
}

sealed trait Tag
case class TextTag(text: String) extends Tag
case class ValueTag(variableName: String, escape: Boolean = true) extends Tag
case class IncludeTag(templateName: String) extends Tag
case class HelperTag(helperName: String, parameters: List[Param]) extends Tag
case class BlockTag(
  blockName: String,
  name: String,
  content: Vector[Tag] = Vector.empty[Tag],
  elseContent: Vector[Tag] = Vector.empty[Tag]
) extends Tag
case object ElseTag extends Tag
case class EndTag(blockName: String) extends Tag

case class NextTag(prefix: String, tag: Tag, suffix: String)
case class CollectedTags(
  suffix: String,
  tags: Vector[Tag] = Vector.empty[Tag],
  elseTags: Vector[Tag] = Vector.empty[Tag]
)

sealed trait Param
case class TextParam(value: String) extends Param
case class VariableParam(name: String) extends Param
case class VariableNameListParam(names: List[String]) extends Param
