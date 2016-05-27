package spark.jobserver.receiver

case class QueryInfo(
                      token: String,
                      rawQuery: String,
                      queryId: String,
                      baseQuery: String,
                      count: Int,
                      tableName: String,
                      format: String = "")

case class ColumnHeader(name: String, `type`: String)

case class Description(headers: List[ColumnHeader])

case class Rows[T](rows: List[T])

case class QueryModel(db: String, query: String)

case class ResultSet(headers: List[ColumnHeader], rows: List[String])