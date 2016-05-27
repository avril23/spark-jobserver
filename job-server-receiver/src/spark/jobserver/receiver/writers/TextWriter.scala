package spark.jobserver.receiver.writers

import java.nio.file._

import spark.jobserver.common.utils.EncodingUtils
import spark.jobserver.receiver.fileSystems.FileSystemBase

class TextWriter(fs: FileSystemBase, workDir: String, fileName: String,  overwrite: Boolean = true) extends BaseWriter {

  private val filePath = fs.getFilePath(workDir, fileName)
  private val fileFullPath = fs.getFileFullPath(workDir, fileName)

  if (exists && !overwrite) {
    throw new FileAlreadyExistsException("The file already exists, FilePath: " + fileFullPath)
  }

  private val workDirPath = fs.getWorkDirPath(workDir)
  if(!fs.exists(workDirPath)) {
    fs.mkDir(workDirPath)
  }

  private lazy val writer = fs.getWriter(fileFullPath, overwrite)

  def getFileFullPath: String = fileFullPath

  def exists: Boolean = fs.exists(fileFullPath)

  def writeRow(lines: List[String]): TextWriter = {
    lines.foreach(writer.append)
    this
  }

  def writeRow(line: String): TextWriter = {
    writer.write(line)
    writer.newLine()
    this
  }

  def writeRow(bytes: Array[Byte]): TextWriter = {
    val line = EncodingUtils.decode(bytes)
    writeRow(line)
  }

  def close(): Unit = {
    writer.flush()
    writer.close()
  }
}

object TextWriter {
  //  def apply(fs: FileSystemBase, file: File, overwrite: Boolean = true): TextWriter = {
  //    val filePath = file.getPath
  //    new TextWriter(fs, filePath, overwrite)
  //  }

  def apply(fs: FileSystemBase, workDir: String, fileName: String, overwrite: Boolean = true): TextWriter = {
    new TextWriter(fs, workDir, fileName, overwrite)
  }
}
