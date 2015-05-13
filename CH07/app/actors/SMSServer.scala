package actors

import akka.actor.{ActorLogging, Actor, Props}
import akka.io.{ IO, Tcp }
import java.net.InetSocketAddress
import Tcp._

class SMSService extends Actor with ActorLogging {

  override def preStart(): Unit = {
    context.actorOf(Props[SMSServer])
  }

  def receive = {
    case any =>
      log.info(s"Received $any")
  }
}

class SMSServer extends Actor with ActorLogging {

  import context.system

   IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 6666))

  def receive = {
    case Bound(localAddress) =>
      log.info(s"SMS server listening on $localAddress")

    case CommandFailed(_: Bind) =>
      context stop self
 
    case Connected(remote, local) =>
      val handler = context.actorOf(Props[SMSHandler])
      val connection = sender()
      connection ! Register(handler)
  }
}

class SMSHandler extends Actor with ActorLogging {

  def receive = {
    case Received(data) =>
      log.info(s"Received message: ${data.utf8String}")
      sender() ! Write(data)
    case PeerClosed =>
      context stop self
  }
}
