package spark.jobserver.receiver.readers

abstract class BaseReader {

  def getFileFullPath: String

  def exists: Boolean

  def readLine: String

  def readLineAsObject[T: Manifest]: Option[T]

  def close(): Unit
}
