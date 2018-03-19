package com.github.enpassant.ickenham.adapter

class PlainAdapter extends Adapter[Any] {
  override def extractString(value: Any): String = value.toString

  override def isEmpty(value: Any): Boolean = value match {
    case Nil => true
    case "" => true
    case 0 => true
    case 0.0 => true
    case null => true
    case _ => false
  }

  override def getChildren(value: Any): List[Any] = value match {
    case ls: List[Any] => ls
    case any => List()
  }

  override def getChild(value: Any, name: String): Option[Any] = value match {
    case map: Map[Any, Any] => map.get(name)
    case _ => None
  }

  override def asValue(text: String): Any = text
}

