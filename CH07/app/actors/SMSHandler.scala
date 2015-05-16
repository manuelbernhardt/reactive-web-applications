package actors

import akka.actor.{ActorRef, ActorLogging, Actor}
import akka.io.Tcp._
import akka.util.{Timeout, ByteString}
import scala.concurrent.duration._

class SMSHandler extends Actor with ActorLogging {

  implicit val timeout = Timeout(2.seconds)

  implicit val ec = context.dispatcher

  lazy val commandHandler = context.actorSelection(
    "akka://application/user/sms/commandHandler"
  )

  val MessagePattern = """[\+]([0-9]*) (.*)""".r
  val RegistrationPattern = """register (.*)""".r

  var connectionSender: ActorRef = _

  def receive = {
    case Received(data) =>
      connectionSender = sender()
      log.info(s"Received message: ${data.utf8String}")
      data.utf8String.trim match {
        case MessagePattern(number, message) =>
          handleMessage(number, message)
        case other =>
          log.warning(s"Invalid message $other")
          sender() ! Write(ByteString("Invalid message format\n"))
      }
    case registered: UserRegistered =>
      connectionSender ! Write(ByteString("Registration successful\n"))
    case subscribed: MentionsSubscribed =>
      connectionSender ! Write(ByteString("Mentions subscribed\n"))
    case InvalidCommand(reason) =>
      connectionSender ! Write(ByteString(reason + "\n"))
    case MentionReceived(id, created, from, text, _) =>
      connectionSender ! Write(ByteString(s"mentioned by @$from: $text\n\n"))
      sender() ! AcknowledgeMention(id)
    case PeerClosed =>
      context stop self
  }

  def handleMessage(number: String, message: String) = {
    message match {
      case RegistrationPattern(userName) =>
        commandHandler ! RegisterUser(number, userName)
      case "subscribe mentions" =>
        commandHandler ! SubscribeMentions(number)
      case "connect" =>
        commandHandler ! ConnectUser(number)
    }
  }

}
