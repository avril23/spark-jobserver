package spark.jobserver.receiver

import spark.jobserver.common.utils.JsonUtils

case class Message(msgType: Int, uuid: String, body: String)

object Message {
  def parse(str: String): Option[Message] = {
    JsonUtils.parseObj[Message](str)
  }
}
