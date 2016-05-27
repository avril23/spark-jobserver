package spark.jobserver.common.utils

import org.json4s.DefaultFormats
import org.json4s.ext.JodaTimeSerializers
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization.write

object JsonUtils {

  implicit val formats = DefaultFormats ++ JodaTimeSerializers.all

  def toStr[T <: AnyRef](t: T): String = write(t)(formats)

  def parseObj[A: Manifest](jsonStr: String): Option[A] = {
    if (jsonStr != null && jsonStr.nonEmpty) {
      try {
        val json = parse(jsonStr)
        json.extractOpt[A]
      }
      catch {
        case ex: Throwable =>
          println(ex.getMessage, ex.getStackTrace)
          None
      }
    } else {
      None
    }
  }

  def main(args: Array[String]) {
    //    val headers = List(new ColumnHeader("id", "Int"), new ColumnHeader("name", "String"))
    //    val rows = List(
    //      new DataRow(List(new ColumnCell("6"), new ColumnCell("damowang"))),
    //      new DataRow(List(new ColumnCell("10"), new ColumnCell("shixiaochuan"))),
    //      new DataRow(List(new ColumnCell("99"), new ColumnCell("!!##$$"))),
    //      new DataRow(List(new ColumnCell("22"), new ColumnCell("tt")))
    //    )
    //
    //    val table: DataTable = DataTable(headers, rows)
    //    println("table: " + table)
    //    val js = table.toJsonString
    //    println("js: " + js)
    //    val obj = parseObj[DataTable](js)
    //    println("obj: " + obj.getOrElse("None"))

    case class Count(count: Int)
    case class Message(msgType: Int, uuid: String, body: String)
    val str = "{\"msgType\":1," +
      "\"uuid\":\"d867ea2eac853008b4e1aceb45c7fd35\"," +
      "\"body\":\"{\\\"count\":10}\"}"
    val str1 = "{\"msgType\":3,\"uuid\":\"d867ea2eac853008b4e1aceb45c7fd35\"," +
      "\"body\":\"[\\\"56\\\",\\\"1\\\",\\\"1\\\",\\\"1\\\",\\\"2014-11-19 19:55:08.394329+08\\\"," +
      "\\\"2014-11-19 19:55:18.475746+08\\\",\\\"8\\\"," +
      "\\\"{\\\\\\\"File2\\\\\\\":\\\\\\\"io_5\\\\\\\",\\\\\\\"File1\\\\\\\":\\\\\\\"io_6\\\\\\\"}\\\"," +
      "\\\"{\\\\\\\"Result\\\\\\\":\\\\\\\"io_7\\\\\\\"}\\\",\\\"{}\\\",null,\\\"10\\\",\\\"0\\\",null," +
      "\\\"55f257d0b44e12a63b597e7a87360ddd\\\",null,null]\"}"
    println(parseObj[Message](str1))
  }
}