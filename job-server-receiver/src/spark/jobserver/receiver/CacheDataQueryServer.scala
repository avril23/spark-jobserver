package spark.jobserver.receiver

import com.typesafe.config.Config
import ooyala.common.akka.InstrumentedActor
import org.zeromq.ZMQ
import spark.jobserver.common.utils.JsonUtils

sealed class CacheDataQueryServer(config: Config, cacheManager: SparkCacheManager) extends InstrumentedActor {

  def wrappedReceive: Receive = {
    case Start => startServer()
  }

  private val _port = config.getInt("spark.jobserver.receiver.serverPort")
  private val _address = "tcp://*:" + _port
  private val _context = ZMQ.context(1)
  private val _receiver = _context.socket(ZMQ.REP)

  private var _isReceiving = true

  def startServer(): Unit = {
    logger.info("Starting spark.jobserver.server listener.")
    _receiver.bind(_address)
    while (true) {
      val str = _receiver.recvStr()
      val msgOpt = Message.parse(str)
      if (msgOpt.isDefined) {
        val msg = msgOpt.get
        msg.msgType match {
          case EMessageType.QueryCache =>
            val body = msg.body
            val queryModelOpt = JsonUtils.parseObj[QueryModel](body)
            if (queryModelOpt.isDefined) {
              val queryModel = queryModelOpt.get
              val resultOpt = doQuery(queryModel)
              if(resultOpt.isEmpty) {
                sendNullMsg()
              } else {
                val result = resultOpt.get
                _receiver.send(JsonUtils.toStr(result))
              }
            } else {
              sendNullMsg()
            }
          case _ =>
            logger.warn(s"unknown message type, ${msg.msgType}.")
        }
      } else {
        logger.warn("received an invalid message.")
        sendNullMsg()
      }
      //      doQuery(str, str)
      //      println("received ", str)
      //      Thread.sleep(5000)
      //      _receiver.send(new java.util.Date().toString)

      //      val msgOpt = Message.parse(str)
      //      if (msgOpt.isDefined) {
      //        val msg = msgOpt.get
      //        val msgType = msg.msgType
      //        val uuid = msg.uuid
      //        val body = msg.body
      //        logger.debug(s"received message, type: $msgType, uuid: $uuid")
      //        msg.msgType match {
      //          case EMessageType.QueryCache =>
      //            val list = doQuery(body)
      //            _receiver.send(list.toString)
      //        }
      //      }
    }
  }

  def doQuery(queryModel: QueryModel): Option[ResultSet] = {
    val db = queryModel.db
    val query = queryModel.query
    val resultOpt = cacheManager.getCache(db, query)
    if(resultOpt.isEmpty) {
      return None
    }
    val (headers, rowArray) = resultOpt.get
    val columnHeaders = headers.map(c => new ColumnHeader(c._1, c._2)).toList
    val rows = rowArray.toList
    Option(new ResultSet(columnHeaders, rows))
  }

  private def sendNullMsg(): Unit = {
    _receiver.send("")
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
}
