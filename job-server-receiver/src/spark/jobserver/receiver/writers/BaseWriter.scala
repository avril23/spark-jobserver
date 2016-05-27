package spark.jobserver.receiver.writers

abstract class BaseWriter {

  def getFileFullPath: String

  def exists: Boolean

  def writeRow(rows: List[String]): BaseWriter

  def writeRow(row: String): BaseWriter

  def writeRow(bytes: Array[Byte]): BaseWriter

  def close(): Unit
}
