package spark.jobserver.common.utils

import javax.servlet.FilterRegistration

import org.zeromq.ZMQ

object ZeroMQUtils {
  def main(args: Array[String]) {
    val context = ZMQ.context(1)
    val receiver = context.socket(ZMQ.PULL)
    receiver.bind("tcp://*:9876")
    println("listening.")
    while (true) {
      println("receiving.")
      val msgByte = receiver.recv(5000)
      val msg = new String(msgByte, 0, msgByte.length, "UTF-8")
    }
  }
}
