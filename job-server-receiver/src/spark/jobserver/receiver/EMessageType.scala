package spark.jobserver.receiver

object EMessageType extends Enumeration {
  type MessageTypeDataType = Int
  val QueryInfo = 1
  val Description = 2
  val Data = 3
  val QueryCache = 4
}
