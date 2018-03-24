package com.github.enpassant.ickenham.adapter

import scala.collection.JavaConverters._
import scala.util.Try

class JavaAdapter extends Adapter[Any] {
  override def extractString(value: Any): String = value.toString

  override def isEmpty(value: Any): Boolean = value match {
    case ls: java.util.List[_] if ls.isEmpty => true
    case map: java.util.Map[_, _] if map.isEmpty => true
    case "" => true
    case 0 => true
    case 0.0 => true
    case null => true
    case _ => false
  }

  override def getChildren(value: Any): List[Any] = value match {
    case ls: java.util.List[_] => ls.asScala.toList
    case any => List()
  }

  override def getChild(value: Any, name: String): Option[Any] = value match {
    case map: java.util.Map[String, _] => map.asScala.get(name)
    case obj: java.lang.Object =>
      val methodOpt = Try(
        obj.getClass.getMethod("get" + name.capitalize)
      ).toOption
      methodOpt map { method =>
        method.invoke(obj)
      }
    case _ => None
  }

  override def asValue(text: String): Any = text
}

