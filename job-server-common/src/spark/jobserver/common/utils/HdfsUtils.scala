package spark.jobserver.common.utils

import java.io.{OutputStreamWriter, BufferedWriter}

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

class HdfsUtils {

  def write(uri: String, filePath: String, data: Array[Byte]): Unit = {
    System.setProperty("HADOOP_USER_NAME", "Mariusz")
    val path = new Path(filePath)
    val conf = new Configuration()
    conf.set("fs.defaultFS", uri)
    val fs = FileSystem.get(conf)
    val os = fs.create(path)
    val w = new BufferedWriter(new OutputStreamWriter(os.getWrappedStream))

    //os.getWrappedStream
    fs.close()
  }
}
