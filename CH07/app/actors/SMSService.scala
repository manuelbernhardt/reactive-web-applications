package actors

import javax.inject.Inject

import akka.actor.{ActorLogging, Actor, Props}
import com.google.inject.AbstractModule
import helpers.Database
import play.api.libs.concurrent.AkkaGuiceSupport

class SMSService @Inject() (database: Database) extends Actor with ActorLogging {

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

class SMSServiceModule extends AbstractModule with AkkaGuiceSupport {
  def configure(): Unit =
    bindActor[SMSService]("sms")
}