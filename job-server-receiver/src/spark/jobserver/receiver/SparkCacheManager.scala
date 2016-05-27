package spark.jobserver.receiver

import java.util.Date

import akka.actor.ActorRef
import akka.pattern.ask
import com.typesafe.config.{ConfigFactory, Config}
import org.slf4j.LoggerFactory
import scala.concurrent.Await
import scala.concurrent.duration._

import spark.jobserver.common.JobManagerActor._
import spark.jobserver.common.supervisor.ContextSupervisor._
import spark.jobserver.common.utils.SparkJobUtils
import spark.jobserver.receiver.fileSystems.FileSystemBase
import spark.jobserver.receiver.readers.TextReader

sealed class SparkCacheManager(config: Config, supervisor: ActorRef) {

  private val logger = LoggerFactory.getLogger(getClass)
  private val contextTimeout = SparkJobUtils.getContextTimeout(config)
  private val queryTimeout = 1.hours
  private val defaultContextName = "DefaultContext"
  private val defContextConfig = ConfigFactory.parseString(
    s"""
          context-factory = spark.jobserver.context.SQLContextFactory
     """)


  private var cacheMapping = Map[String, Date]()

  def loadJson(name: String, uuid: String, contextConfig: Config = defContextConfig): Unit = {
    addContext(name, contextConfig)
    val contextActorOpt = getContext(name)
    if (contextActorOpt.isEmpty) {
      logger.warn("Can`t get specific context.")
    }
    else {
      println("in load json.")
      val contextActor = contextActorOpt.get
      println(contextActor.toString(), contextActor)
      //      contextActor ! LoadData("hdfs://123.56.1.80:8020/jobServer/6b07b0dfee383050838a07b2561ae0c5/data.json")
      //contextActor ! LoadData("hdfs://192.168.1.129:9000/jobServer/6b07b0dfee383050838a07b2561ae0c5/data.json")
      //      contextActor ! LoadJson("file:/D:/jobServer/6b07b0dfee383050838a07b2561ae0c5/data.json")
      //      contextActor ! LoadJson("home/shixiaochaun/Data/jobServer/6b07b0dfee383050838a07b2561ae0c5/data.json")
    }
  }

  private def addContext(name: String, contextConfig: Config): Unit = {
    val future = (supervisor ? AddContext(name, contextConfig)) (contextTimeout.seconds)
    Await.result(future, contextTimeout.seconds) match {
      case ContextInitialized => logger.info(s"created a new sql context, token: $name, " +
        s"config: ${contextConfig.toString}")
      case ContextAlreadyExists => logger.info(s"The context already exists.")
      case ContextInitError(e) => logger.error("Context init error", e)
    }
  }

  private def getContext(name: String): Option[ActorRef] = {
    val future = (supervisor ? GetContext(name)) (contextTimeout.seconds)
    Await.result(future, contextTimeout.seconds) match {
      case (manager: ActorRef, resultActor: ActorRef) => Some(manager)
      case NoSuchContext => None
      case ContextInitError(err) => None
    }
  }

  private def createContext(name: String, uuid: String, contextConfig: Config = defContextConfig): Option[ActorRef] = {
    addContext(name, contextConfig)
    getContext(name)
  }

  def makeCache(fs: FileSystemBase, uuid: String): Unit = {
    val workDir = fs.getWorkDirPath(uuid)
    if (!fs.exists(workDir)) {
      logger.warn(s"Can`t find specific path, we can`t make cache. WorkDir: $workDir.")
      return
    }

    val isValid = validateCacheFile(fs, uuid)
    if (!isValid) {
      logger.warn(s"The cache is not valid.")
      return
    }

    val contextOpt = createContext(defaultContextName, uuid)
    if (contextOpt.isEmpty) {
      logger.warn("Create or get context failed.")
      return
    }

    val qiOpt = getQueryInfo(fs, uuid)
    if (qiOpt.isEmpty) {
      logger.warn("Can`t parse query info.")
      return
    }

    val qi = qiOpt.get
    val tableName = qi._1
    val format = qi._2
    val fileName = format match {
      case "json" => fs.JSON_FILE
      case _ => fs.PARQUET_FILE
    }

    val dataFilePath = fs.getFileFullPath(uuid, fileName)
    val headers = getColumnHeaders(fs, uuid)
    val contextActor = contextOpt.get
    val future = (contextActor ? CreateCache(tableName, dataFilePath, format, headers)) (queryTimeout)
    Await.result(future, queryTimeout) match {
      case Cached => cacheMapping = cacheMapping.updated(uuid, new Date())
        logger.debug(s"cache has been created. uuid: $uuid")
        logger.debug(s"cache list: ${cacheMapping.map(c => s"uuid: ${c._1}, last access time: ${c._2}").mkString(", ")}")
      case CacheError(t: Throwable) =>
        logger.error(s"create cache failed, FileSystem: ${fs.toString}, uuid: $uuid", t)
    }
  }

  def getCache(db: String, query: String): Option[(Map[String, String], Array[String])] = {
    val contextOpt = getContext(defaultContextName)
    if (contextOpt.isEmpty) {
      logger.error(s"Can`t find specific context by name, ContextName: $defaultContextName")
      None
    } else {
      val context = contextOpt.get
      val future = (context ? RetrieveCache(db, query)) (queryTimeout)
      Await.result(future, queryTimeout) match {
        case Retrieved(listOpt: Option[(Map[String, String], Array[String])]) => listOpt
        case RetrieveError(t: Throwable) =>
          logger.error(s"retrieve cache failed. Db: $db, Query: $query", t)
          None
      }
    }
  }

  private def getColumnHeaders(fs: FileSystemBase, uuid: String): Map[String, String] = {
    val descOpt = TextReader(fs, uuid, fs.DESCRIPTION_FILE).readLineAsObject[Description]
    if (descOpt.isEmpty) {
      return Map.empty
    }

    val desc = descOpt.get
    desc.headers.map(h => h.name -> h.`type`).toMap
  }

  // tableName -> format
  private def getQueryInfo(fs: FileSystemBase, uuid: String): Option[(String, String)] = {
    val queryInfoOpt = TextReader(fs, uuid, fs.QUERY_INFO).readLineAsObject[QueryInfo]
    if (queryInfoOpt.isEmpty) {
      return None
    }
    val queryInfo = queryInfoOpt.get
    Option(queryInfo.tableName -> queryInfo.format)
  }

  // TODO: Add validation logic
  private def validateCacheFile(fs: FileSystemBase, uuid: String): Boolean = {
    true
  }
}
