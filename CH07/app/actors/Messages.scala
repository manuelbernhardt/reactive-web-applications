package actors

import org.joda.time.DateTime

trait Command {
  val phoneNumber: String
}

trait Event {
  val timestamp: DateTime
}


case class SubscribeMentions(phoneNumber: String) extends Command
case class UnsubscribeMentions(phoneNumber: String) extends Command

case class ClientHandlerCreated(phoneNumber: String, timestamp: DateTime = DateTime.now) extends Event