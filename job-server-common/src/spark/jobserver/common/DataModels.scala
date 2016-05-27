package spark.jobserver.common

import spark.jobserver.common.utils.JsonUtils

class ModelBase {
  def toJsonString: String = JsonUtils.toStr(this)
}

case class ColumnHeader(name: String, `type`: String)

case class ColumnCell(value: String)

case class DataRow(row: List[ColumnCell])

case class DataTable(headers: List[ColumnHeader],rows: List[DataRow]) extends ModelBase

object DataTable{
  def apply(dtJson: String): Option[DataTable] = JsonUtils.parseObj(dtJson)
}

case class TableInfo(jsonFile: List[String], count: Long, schema: List[ColumnHeader])
