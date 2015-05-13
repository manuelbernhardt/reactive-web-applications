package actors

import akka.actor.{ActorLogging, Actor}
import akka.io.Tcp._

class SMSHandler extends Actor with ActorLogging {

  def receive = {
    case Received(data) =>
      log.info(s"Received message: ${data.utf8String}")
      sender() ! Write(data)
    case PeerClosed =>
      context stop self
  }
}
