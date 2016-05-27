package spark.jobserver.receiver

import spark.jobserver.receiver.fileSystems.FileSystemBase

// Start receiver and transfer server.
case object Start

// retrieve data from transfer server via query.
case class Retrieve(db: String, query: String)

case class MakeCache(fs: FileSystemBase, uuid: String)

case class GetCache(query: String)