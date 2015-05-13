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
      case command: Command =>
        val phoneNumber = command.phoneNumber
        context.child(phoneNumber).map { reference =>
          reference forward command
        } getOrElse {
          persist(ClientHandlerCreated(phoneNumber)) { event =>
            handleEvent(event)
            context.child(phoneNumber).foreach { _ forward command }
          }
        }
    }

    def handleEvent(event: Event) = event match {
      case ClientHandlerCreated(phoneNumber, _) =>
        context.actorOf(
          props = Props(classOf[ClientCommandHandler], phoneNumber),
          name = phoneNumber
        )
    }
}