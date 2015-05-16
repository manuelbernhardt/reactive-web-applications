package actors

import akka.actor.{ActorLogging, Actor}
import akka.io.Tcp._

class SMSHandler extends Actor with ActorLogging {

  lazy val commandHandler = context.actorSelection(
    "akka://application/user/sms/commandHandler"
  )

  val MessagePattern = """[\+]([0-9]*) (.*)""".r
  val SubscriptionPattern = """subscribe mentions (.*)""".r

  def receive = {
    case Received(data) =>
      log.info(s"Received message: ${data.utf8String}")
      data.utf8String match {
        case MessagePattern(number, message) =>
          message match {
            case SubscriptionPattern(userName) =>
              commandHandler ! SubscribeMentions(number, userName)
          }
        case other =>
          log.warning(s"Invalid message $other")
          sender() ! "Invalid message format"
      }
    case PeerClosed =>
      context stop self
  }
}
