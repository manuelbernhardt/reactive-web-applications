package actors

import akka.actor.{ActorLogging, Actor, Props}
import helpers.Database

class SMSService(database: Database) extends Actor with ActorLogging {

  override def preStart(): Unit = {
    context.actorOf(Props[SMSServer])
    context.actorOf(Props[CQRSCommandHandler], name = "commandHandler")
    context.actorOf(CQRSQueryHandler.props(database), name = "queryHandler")
    context.actorOf(CQRSEventHandler.props(database), name = "eventHandler")
  }

  def receive = {
    case message =>
      log.info("Received {}", message)
  }
}

object SMSService {
  def props(database: Database) = Props(classOf[SMSService], database)
}