package com.github.enpassant.ickenham.adapter

import org.json4s._

class Json4sAdapter extends Adapter[JValue] {
  implicit val formats = DefaultFormats

  override def extractString(value: JValue): String = value.toOption match {
    case Some(v) => v.extract[String]
    case _ => ""
  }

  override def isEmpty(value: JValue): Boolean = value match {
    case JArray(Nil) => true
    case JString("") => true
    case JBool(false) => true
    case JLong(0L) => true
    case JInt(value) if value == BigInt(0) => true
    case JDecimal(value) if value == BigDecimal(0) => true
    case JBool(false) => true
    case JNothing => true
    case JNull => true
    case _ => false
  }

  override def getChildren(value: JValue): List[JValue] = value match {
    case array: JArray => array.children
    case any => List()
  }

  override def getChild(value: JValue, name: String): Option[JValue] = {
    value \ name match {
      case JNothing => None
      case child => Some(child)
    }
  }

  override def asValue(text: String): JValue = JString(text)
}

