package com.github.enpassant.ickenham.stream

trait Stream[R] {
  def push(text: String): Unit
  def getResult: R
}

class StringBuilderStream extends Stream[String] {
  val sb = new java.lang.StringBuilder()

  def push(text: String) = {
    sb.append(text)
  }

  override def getResult(): String = sb.toString
}

