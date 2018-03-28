package com.github.enpassant.ickenham.springmvc

import com.github.enpassant.ickenham.Ickenham
import com.github.enpassant.ickenham.Ickenham._
import com.github.enpassant.ickenham.adapter.JavaAdapter
import com.github.enpassant.ickenham.stream.WriterStream

import java.io.Writer
import java.util.Locale

import javax.servlet.http.HttpServletResponse
import javax.servlet.ServletContext

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.context.ApplicationContext
import org.springframework.context.i18n.LocaleContextHolder

import scala.util.Try

class SpringIckenham(
  val url: String,
  val context: ServletContext,
  val applicationContext: ApplicationContext)
{
  val logger = LoggerFactory.getLogger(classOf[IckenhamView])

  val parts = url.split("/")
  val templateFile = parts.last
  val template = templateFile.split("\\.").head
  val prefix = parts.dropRight(1).mkString("/")

  val i18nHelper = (params: List[String]) => {
    val locale = LocaleContextHolder.getLocale()
    Try(applicationContext.getMessage(params.head, params.tail.toArray, locale))
      .toOption.orElse(Some(params))
  }

  val helpers: Helpers = Map("i18n" -> i18nHelper)

  val loadTemplate = (name: String) => {
    Ickenham.loadFromInputStream(
      context.getResourceAsStream(prefix + "/" + name + ".hbs"))
  }

  val ickenham = new Ickenham(new JavaAdapter(), helpers, loadTemplate)

  val assembledFn = ickenham.compileWithStream[Unit](template)

  def render(json: Any, writer: Writer, locale: Locale) = {
    val stream = new WriterStream(writer)
    assembledFn(stream)(List(json))
    stream.getResult
  }
}
