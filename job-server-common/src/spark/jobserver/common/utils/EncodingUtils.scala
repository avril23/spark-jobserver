package spark.jobserver.common.utils

import java.nio.ByteBuffer

object EncodingUtils {

  private val UTF8_ENCODING = "utf-8"

  def decode(data: Array[Byte]): String = decode(data, 0, data.length)

  def decode(data: Array[Byte], offset: Int, length: Int): String = {
    new String(data, offset, length, UTF8_ENCODING)
  }

  def toInt(data: Array[Byte]): Int = toInt(data, 0, data.length)

  def toInt(data: Array[Byte], offset: Int, length: Int): Int = {
    ByteBuffer.wrap(data, offset, length).getInt
  }
}
