package spark.jobserver.receiver.fileSystems

import java.io.{BufferedReader, BufferedWriter}

abstract class FileSystemBase {

  val QUERY_INFO = "queryInfo.info"
  val DESCRIPTION_FILE = "description.info"
  val JSON_FILE = "data.json"
  val PARQUET_FILE = "data.parquet"

  def getBase: String
  def getWorkDirPath(workDir: String): String
  def mkDir(path: String): Unit
  def exists(path: String): Boolean
  def getFilePath(workDir: String, fileName: String): String
  def getFileFullPath(workDir: String, fileName: String): String
  def getWriter(path: String, overwrite: Boolean): BufferedWriter
  def getReader(path: String): BufferedReader
}
