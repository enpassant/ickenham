package com.github.enpassant.ickenham.adapter

import org.json4s._

class Json4sAdapter extends Adapter[JValue] {
  implicit val formats = DefaultFormats

  override def extractString(value: JValue): String = value.toOption match {
    case Some(v) => v.extract[String]
    case _ => ""
  }

  override def isEmpty(value: JValue): Boolean = !value.toOption.isDefined

  override def getChildren(value: JValue): List[JValue] = value match {
    case array: JArray => array.children
    case any => List()
  }

  override def getChild(value: JValue, name: String): JValue = value \ name
  override def asValue(text: String): JValue = JString(text)
}

