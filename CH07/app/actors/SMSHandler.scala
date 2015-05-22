package actors

import akka.actor.{ActorRef, ActorLogging, Actor}
import akka.io.Tcp._
import akka.util.{ByteString, Timeout}
import scala.concurrent.duration._

class SMSHandler extends Actor with ActorLogging {

  implicit val timeout = Timeout(2.seconds)

  implicit val ec = context.dispatcher

  lazy val commandHandler = context.actorSelection(
    "akka://application/user/sms/commandHandler"
  )
  lazy val queryHandler = context.actorSelection(
    "akka://application/user/sms/queryHandler"
  )

  val MessagePattern = """[\+]([0-9]*) (.*)""".r
  val RegistrationPattern = """register (.*)""".r

  var connectionReference: ActorRef = _

  def receive = {
    case Received(data) =>
      connectionReference = sender()
      log.info(s"Received message: ${data.utf8String}")
      data.utf8String.trim match {
        case MessagePattern(number, message) =>
          handleMessage(number, message)
        case other =>
          log.warning(s"Invalid message $other")
          sender() ! Write(ByteString("Invalid message format\n"))
      }
    case registered: UserRegistered =>
      connectionReference ! Write(ByteString("Registration successful\n"))
    case subscribed: MentionsSubscribed =>
      connectionReference ! Write(ByteString("Mentions subscribed\n"))
    case InvalidCommand(reason) =>
      connectionReference ! Write(ByteString(reason + "\n"))
    case MentionReceived(id, created, from, text, _) =>
      connectionReference ! Write(ByteString(s"mentioned by @$from: $text\n"))
      sender() ! AcknowledgeMention(id)
    case DailyMentionsCount(count) =>
      connectionReference ! Write(ByteString(s"$count mentions today\n"))
    case WeeklyMentionsCount(count) =>
      connectionReference ! Write(ByteString(s"$count mentions this week\n"))
    case QueryFailed =>
      connectionReference ! Write(ByteString("Sorry, we couldn't run your query\n"))
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
      case "mentions today" =>
        queryHandler ! MentionsToday(number)
      case "mentions past week" =>
        queryHandler ! MentionsPastWeek(number)
        
    }
  }

}
