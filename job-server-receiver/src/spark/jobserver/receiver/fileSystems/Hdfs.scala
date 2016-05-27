package spark.jobserver.receiver.fileSystems

import java.io.{InputStreamReader, OutputStreamWriter, BufferedWriter, BufferedReader}

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

class Hdfs(basePath: String) extends FileSystemBase {

  private[this] val splitter = "/"

  System.setProperty("HADOOP_USER_NAME", "hdfs")
  val conf = new Configuration()
  conf.set("fs.defaultFS", basePath)
  val fs = FileSystem.get(conf)


  def getBase: String = if (basePath.endsWith(splitter)) basePath else basePath + splitter

  def getWorkDirPath(workDir: String): String = getBase + workDir + splitter

  def getFilePath(workDir: String, fileName: String): String = workDir + splitter + fileName

  def getFileFullPath(workDir: String, fileName: String): String = getWorkDirPath(workDir) + fileName

  def mkDir(path: String): Unit = {}

  def exists(path: String): Boolean = {
    fs.exists(new Path(path))
  }

  def getWriter(path: String, overwrite: Boolean): BufferedWriter = {
    val filePath = new Path(path)
    val outputStream = fs.create(filePath, overwrite)
    new BufferedWriter(new OutputStreamWriter(outputStream.getWrappedStream))
  }

  def getReader(path: String): BufferedReader = {
    val filePath = new Path(path)
    val inputStream = fs.open(filePath)
    new BufferedReader(new InputStreamReader(inputStream.getWrappedStream))
  }


  override def toString: String = "HDFS"

}