package spark.jobserver.receiver

import akka.actor.ActorRef
import com.typesafe.config.Config
import ooyala.common.akka.InstrumentedActor

sealed class CacheDataRetriever(config: Config, cacheManager: ActorRef) extends InstrumentedActor {

  override def wrappedReceive: Receive = {
    case Retrieve(db: String, query: String) =>
  }
}
