package actors

import akka.actor.{ActorLogging, Actor, Props}

class SMSService extends Actor with ActorLogging {

  override def preStart(): Unit = {
    context.actorOf(Props[SMSServer])
    context.actorOf(Props[CQRSCommandHandler], name = "commandHandler")
  }

  def receive = {
    case any =>
      log.info(s"Received $any")
  }
}