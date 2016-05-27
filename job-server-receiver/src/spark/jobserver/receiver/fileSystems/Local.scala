package spark.jobserver.receiver.fileSystems

import java.io._

class Local(basePath: String) extends FileSystemBase {

  private val baseFile: File = new File(basePath)

  def getBase: String = baseFile.getPath

  def getWorkDirPath(workDir: String): String = new File(baseFile, workDir).getPath

  def getFilePath(workDir: String, fileName: String): String = new File(workDir, fileName).getPath

  def getFileFullPath(workDir: String, fileName: String): String = new File(getWorkDirPath(workDir), fileName).getPath

  def mkDir(path: String): Unit = {
    val fileDir = new File(path)
    if(!fileDir.exists()){
      fileDir.mkdir()
    }
  }

  def exists(path: String): Boolean = {
    val file = new File(path)
    file.exists()
  }

  def getWriter(path: String, overwrite: Boolean): BufferedWriter = new BufferedWriter(new FileWriter(path))

  def getReader(path: String): BufferedReader = new BufferedReader(new FileReader(path))

  override def toString: String = "Local"
}

