package spark.jobserver.receiver.fileSystems

/**
  * Created by XiaoChuan on 005 2016/5/5.
  */
object FsUtils {

  def getFs(basePath: String): FileSystemBase = {
    if(basePath.startsWith("hdfs:")) {
      new Hdfs(basePath)
    }
    else {
      new Local(basePath)
    }
  }
}
