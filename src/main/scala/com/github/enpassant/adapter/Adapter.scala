package com.github.enpassant.ickenham.adapter

trait Adapter[T] {
  def extractString(value: T): String
  def isEmpty(value: T): Boolean
  def getChildren(value: T): List[T]
  def getChild(value: T, name: String): T
  def asValue(text: String): T
}
