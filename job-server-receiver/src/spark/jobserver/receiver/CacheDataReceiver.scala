package spark.jobserver.receiver

import akka.actor.ActorRef
import com.typesafe.config.Config
import ooyala.common.akka.InstrumentedActor
import org.zeromq.ZMQ

import spark.jobserver.receiver.CacheDataWriter._

sealed class CacheDataReceiver(config: Config, writerActor: ActorRef) extends InstrumentedActor {

  private val _port = config.getInt("spark.jobserver.receiver.port")
  private val _address = "tcp://*:" + _port
  private val _context = ZMQ.context(1)
  private val _receiver = _context.socket(ZMQ.PULL)

  private var _isReceiving = true

  def start(): Unit = {
    logger.info("Starting spark.jobserver.receiver listener.")
    _receiver.bind(_address)
    while (true) {
      val str = _receiver.recvStr()
      val msgOpt = Message.parse(str)
      if (msgOpt.isDefined) {
        val msg = msgOpt.get
        val msgType = msg.msgType
        val uuid = msg.uuid
        val body = msg.body
        logger.debug(s"received message, type: $msgType, uuid: $uuid")
        msg.msgType match {
          case EMessageType.Data => writerActor ! NewRows(uuid, body)
          case EMessageType.Description => writerActor ! NewDescription(uuid, body)
          case EMessageType.QueryInfo => writerActor ! NewQueryInfo(uuid, body)
        }
      }
    }
  }


  override def postStop(): Unit = {
    try {
      logger.info("shutting down CacheDataReceiver.")
      _isReceiving = false
      _receiver.close()
    } catch {
      case ex: Throwable => logger.error(ex.getMessage, ex)
    }
  }

  override def wrappedReceive: Receive = {
    case Start => start()
  }
}
