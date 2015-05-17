package actors

import akka.actor.{Props, ActorLogging}
import akka.persistence.{RecoveryFailure, RecoveryCompleted, PersistentActor}

class CQRSCommandHandler extends PersistentActor with ActorLogging {

  override def persistenceId: String = "CQRSCommandHandler"

  override def receiveRecover: Receive = {
    case RecoveryFailure(cause) => log.error(cause, "Failed to recover!")
    case RecoveryCompleted => log.info("Recovery completed")
    case evt: Event => handleEvent(evt)
  }

  override def receiveCommand: Receive = {
    case RegisterUser(phoneNumber, username) =>
      persist(UserRegistered(phoneNumber, username))(handleEvent)
    case command: Command =>
      context.child(command.phoneNumber).map { reference =>
        reference forward command
      } getOrElse {
        sender() ! "User unknown"
      }
  }

  def handleEvent(event: Event): Unit = event match {
    case registered @ UserRegistered(phoneNumber, userName, _) =>
      context.actorOf(
        props = Props(classOf[ClientCommandHandler], phoneNumber, userName),
        name = phoneNumber
      )
      if (recoveryFinished) {
        sender() ! registered
        context.system.eventStream.publish(registered)
      }
    }
}