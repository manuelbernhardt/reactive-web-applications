package actors

import java.net.InetSocketAddress

import akka.actor.{Props, ActorLogging, Actor}
import akka.io.{Tcp, IO}
import akka.io.Tcp._

class SMSServer extends Actor with ActorLogging {

  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 6666))

  def receive = {
    case Bound(localAddress) =>
      log.info(s"SMS server listening on $localAddress")

    case CommandFailed(_: Bind) =>
      context stop self

    case Connected(remote, local) =>
      val connection = sender()
      val handler = context.actorOf(Props(classOf[SMSHandler], connection))
      connection ! Register(handler)
  }
}