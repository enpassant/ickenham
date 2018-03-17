package com.github.enpassant.ickenham

import com.github.enpassant.ickenham.adapter.Adapter

import java.nio.charset.StandardCharsets._
import java.nio.file.{Files, Paths}
import scala.util.matching.Regex
import scala.util.Try

class Ickenham[T](adapter: Adapter[T]) {
  type Templates = Map[String, Vector[Tag]]

  def apply(fileName: String): T => String = {
    compile(fileName)
  }

  def compiles(templateNames: String*): Templates = {
    (templateNames map { templateName =>
      val template = loadFile(templateName + ".hbs")
      val tags = parse(template)
      (templateName -> tags)
    }).toMap
  }

  def compile(template: String): T => String = {
    val templates = compiles(template)
    val assembledFn = assemble(template, templates)
    json => assembledFn(json)
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
    val endTagRegex = """\{\{\/(\w+)\}\}"""
    val valueTagRegex = """\{\{([_a-zA-Z0-9\.\/]+)\}\}"""
    val includeTagRegex = """\{\{>\s+([_a-zA-Z0-9\./]+)\}\}"""
    val blockTagRegex = """\{\{#(\w+)\s+([_a-zA-Z0-9\./]+)\}\}"""
    val regex = new Regex(
      endTagRegex + "|" +
      valueTagRegex + "|" +
      includeTagRegex + "|" +
      blockTagRegex
    )
    val matchOpt = regex.findFirstMatchIn(text)
    matchOpt map { m =>
      if (Option(m.group(1)) != None) {
        NextTag(csr(m.before), EndTag(m.group(1)), csl(m.after))
      } else if (Option(m.group(2)) != None) {
        NextTag(m.before.toString, ValueTag(m.group(2)), m.after.toString)
      } else if (Option(m.group(3)) != None) {
        NextTag(csr(m.before), IncludeTag(m.group(3)), csl(m.after))
      } else {
        val collectedTags = collectTags(m.after.toString, Vector.empty[Tag])
        val blockTag = BlockTag(m.group(4), m.group(5), collectedTags.tags)
        NextTag(csr(m.before), blockTag, collectedTags.suffix)
      }
    }
  }

  private def csl(str: CharSequence) = str.toString.replaceAll("^\\s+", " ")
  private def csr(str: CharSequence) = str.toString.replaceAll("\\s+$", " ")

  def assemble(template: String, templates: Templates): T => String = {
    val tags = templates(template)
    val substitutedFn = substitute(tags, templates)
    json => substitutedFn(List(json))
  }

  def substitute(tags: Vector[Tag], templates: Templates):
    List[T] => String =
  {
    val substituted = tags.map { tag =>
      tag match {
        case TextTag(text) =>
          path: List[T] => text
        case ValueTag(variableName) =>
          val names = getVariableNameList(variableName)
          path: List[T] =>
            val value = getVariableLoop(names, path)
            adapter.extractString(value)
        case IncludeTag(templateName) =>
          path => substitute(templates(templateName), templates)(path)
        case BlockTag("if", name, content) =>
          val substitutedFn = substitute(content, templates)
          val names = getVariableNameList(name)

          path: List[T] =>
            if (adapter.isEmpty(getVariableLoop(names, path))) {
              ""
            } else {
              substitutedFn(path)
            }
        case BlockTag("each", name, content) =>
          val substitutedFn = substitute(content, templates)
          val names = getVariableNameList(name)

          path: List[T] =>
            val children = adapter.getChildren(getVariableLoop(names, path))
            val substitutedItems = children map { item =>
              substitutedFn(item :: path)
            }
            mkString(substitutedItems)
        case BlockTag(blockName, name, content) =>
          path: List[T] => s"Block: $blockName"
        case EndTag(blockName) =>
          path: List[T] => "ERROR!"
      }
    }
    path: List[T] => mkString(substituted.map(_(path)))
  }

  def mkString(strings: Seq[String]) = {
    val length = strings.foldLeft(0)(_ + _.length)
    val sb = new java.lang.StringBuilder(length)
    strings.foreach { str =>
      sb.append(str)
    }
    sb.toString
  }

  def getVariableNameList(variableName: String): List[String] = {
    variableName
      .replace("..", "_parent_")
      .replace(".", "/")
      .split("/")
      .toList
  }

  def getVariable(variableName: String, path: List[T]): T = {
    val names = getVariableNameList(variableName)
    getVariableLoop(names, path)
  }

  def getVariableLoop(names: List[String], path: List[T]): T = {
    names match {
      case "" :: tail => getVariableLoop(tail, path)
      case "this" :: tail => getVariableLoop(tail, path)
      case "_parent_" :: tail => getVariableLoop(tail, path.tail)
      case name :: Nil => adapter.getChild(path.head, name)
      case name :: tail =>
        val child = adapter.getChild(path.head, name)
        getVariableLoop(tail, child :: path)
      case _ => adapter.asValue("Error")
    }
  }

  def loadFile(fileName: String): String = {
    val resultTry = Try {
      new String(Files.readAllBytes(
        Paths.get(getClass.getResource("/" + fileName).getFile)), UTF_8)
    }
    resultTry.getOrElse("")
  }
}

sealed trait Tag
case class TextTag(text: String) extends Tag
case class ValueTag(variableName: String) extends Tag
case class IncludeTag(templateName: String) extends Tag
case class BlockTag(
  blockName: String,
  name: String,
  content: Vector[Tag] = Vector.empty[Tag]
) extends Tag
case class EndTag(blockName: String) extends Tag

case class NextTag(prefix: String, tag: Tag, suffix: String)
case class CollectedTags(suffix: String, tags: Vector[Tag] = Vector.empty[Tag])
