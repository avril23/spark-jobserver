package spark.jobserver.receiver.exceptions

case class CacheFormatNotSupported(message: String) extends Exception(message)
