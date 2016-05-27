package spark.jobserver.receiver

import com.typesafe.config.Config
import ooyala.common.akka.InstrumentedActor
import spark.jobserver.common.utils.JsonUtils

import spark.jobserver.receiver.exceptions.CacheFormatNotSupported
import spark.jobserver.receiver.fileSystems.FsUtils
import spark.jobserver.receiver.writers._
import spark.jobserver.receiver.CacheDataWriter._

object CacheDataWriter {

  case class NewRows(uuid: String, rowsStr: String)

  case class NewDescription(uuid: String, description: String)

  case class NewQueryInfo(uuid: String, queryInfo: String)

}

sealed class CacheDataWriter(config: Config, cacheManager: SparkCacheManager) extends InstrumentedActor {

  private val _cacheFolder = config.getString("spark.jobserver.receiver.cache-folder")
  private val _cacheFormat = config.getString("spark.jobserver.receiver.cache-format")
  private val _fileSystem = FsUtils.getFs(_cacheFolder)

  private var _writerMap: Map[String, BaseWriter] = Map.empty

  init()

  private def init(): Unit = {
    if (!_fileSystem.exists(_cacheFolder)) {
      _fileSystem.mkDir(_cacheFolder)
    }
  }

  override def wrappedReceive: Receive = {
    case NewRows(uuid, rowsStr) =>
      rowsHandler(uuid, rowsStr)
    case NewDescription(uuid, description) =>
      // Close data file first.
      closeDataWriter(uuid)
      descriptionHandler(uuid, description)
    case NewQueryInfo(uuid, queryInfo) =>
      queryInfoHandler(uuid, queryInfo)
    case _ => logger.warn("can`t find pattern.")
  }

  private def queryInfoHandler(uuid: String, queryInfoStr: String): Unit = {
    val writer = getTextWriter(uuid, _fileSystem.QUERY_INFO)
    val queryInfoOpt = JsonUtils.parseObj[QueryInfo](queryInfoStr)
    val str = if (queryInfoOpt.isEmpty) {
      queryInfoStr
    } else {
      val queryInfo = queryInfoOpt.get
      val newQueryInfo = new QueryInfo(
        queryInfo.token,
        queryInfo.rawQuery,
        queryInfo.queryId,
        queryInfo.baseQuery,
        queryInfo.count,
        queryInfo.tableName,
        _cacheFormat
      )
      JsonUtils.toStr(newQueryInfo)
    }

    writer.writeRow(str)
    writer.close()
    cacheManager.makeCache(_fileSystem, uuid)
  }

  private def closeDataWriter(uuid: String) = {
    val writer = getDataWriter(uuid)
    this.synchronized {
      _writerMap = _writerMap.-(uuid)
    }
    writer.close()
  }

  private def descriptionHandler(uuid: String, description: String): Unit = {
    val writer = getTextWriter(uuid, _fileSystem.DESCRIPTION_FILE)
    writer.writeRow(description)
    writer.close()
  }

  private def getWriter(workDir: String, fileName: String): BaseWriter = {
    _cacheFormat match {
      case "json" => TextWriter(_fileSystem, workDir, fileName)
      case "parquet" => ParquetWriter(_fileSystem, workDir, fileName)
      case _ => throw new CacheFormatNotSupported("We do not support the cache format. " + _cacheFormat)
    }
  }

  private def getTextWriter(workDir: String, fileName: String): TextWriter =
    TextWriter(_fileSystem, workDir, fileName)

  private def getDataWriter(uuid: String): BaseWriter = {
    val writerOpt = _writerMap.get(uuid)
    if (writerOpt.isDefined) {
      writerOpt.get
    } else {
      val myWriter = getWriter(uuid, _fileSystem.JSON_FILE)
      this.synchronized {
        _writerMap = _writerMap ++ Map(uuid -> myWriter)
      }

      myWriter
    }
  }

  private def rowsHandler(uuid: String, rowsStr: String): Unit = {
    val writer = getDataWriter(uuid)
    writer.writeRow(rowsStr)
  }
}
