package spark.jobserver.receiver.writers

import spark.jobserver.receiver.fileSystems.FileSystemBase

// TODO: Implement parquet writer.
class ParquetWriter (fs: FileSystemBase, workDir: String, fileName: String, overwrite: Boolean = true) extends BaseWriter {

  private lazy val fileFullPath = fs.getFileFullPath(workDir, fileName)

  override def getFileFullPath: String = fileFullPath

  override def writeRow(rows: List[String]): BaseWriter = this

  override def writeRow(row: String): BaseWriter = this

  override def writeRow(bytes: Array[Byte]): BaseWriter = this

  override def exists: Boolean = fs.exists(fileFullPath)

  override def close(): Unit = {}
}

object ParquetWriter {
  def apply(fs: FileSystemBase, workDir: String, fileName: String): BaseWriter = new ParquetWriter(fs, workDir, fileName)
}