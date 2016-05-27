package spark.jobserver.receiver

import akka.actor.ActorRef

class JobServerReceiver(actors: ActorRef*) {
  def start(): Unit = {
    for(actor <- actors){
      actor ! Start
    }
  }
}
