package spark.jobserver.receiver.readers

import java.io.{FileNotFoundException, File}
import java.nio.file.FileAlreadyExistsException

import spark.jobserver.common.utils.JsonUtils
import spark.jobserver.receiver.fileSystems.FileSystemBase


class TextReader(fs: FileSystemBase, workDir: String, fileName: String) extends BaseReader {

  private lazy val fileFullPath = fs.getFileFullPath(workDir, fileName)

  if (!exists) {
    throw new FileNotFoundException("The file already exists, FilePath: " + fileFullPath)
  }

  private lazy val reader = fs.getReader(fileFullPath)

  def getFileFullPath: String = fileFullPath

  def exists: Boolean = fs.exists(getFileFullPath)


  def readLine: String = {
    val line = reader.readLine()
    close()
    line
  }

  def readLineAsObject[T: Manifest]: Option[T] = {
    val line = readLine
    JsonUtils.parseObj[T](line)
  }

  def close(): Unit = {
    reader.close()
  }
}

object TextReader {
  def apply(fs: FileSystemBase, workDir: String, fileName: String): TextReader = new TextReader(fs, workDir, fileName)
}
