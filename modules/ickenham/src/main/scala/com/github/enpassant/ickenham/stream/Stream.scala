package com.github.enpassant.ickenham.stream

import java.io.BufferedWriter
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Writer

trait Stream[R] {
  def push(text: String): Unit
  def getResult: R
}

class StringBuilderStream extends Stream[String] {
  val sb = new java.lang.StringBuilder()

  def push(text: String) = {
    sb.append(text)
  }

  override def getResult(): String = {
    val result = sb.toString
    sb.setLength(0)
    result
  }
}

class OutputStreamStream(val os: OutputStream) extends Stream[Unit] {
  val writer = new OutputStreamWriter(os)

  def push(text: String) = {
    writer.write(text)
  }

  override def getResult(): Unit = writer.close
}

class WriterStream(val writer: Writer) extends Stream[Unit] {
  def push(text: String) = {
    writer.write(text)
  }

  override def getResult(): Unit = writer.close
}
