package actors

import akka.actor.SupervisorStrategy.{Stop, Escalate, Restart}
import akka.actor._
import messages.ComputeReach
import reactivemongo.core.errors.ConnectionException
import scala.concurrent.duration._

class StatisticsProvider extends Actor with ActorLogging {

  var reachComputer: ActorRef = _
  var storage: ActorRef = _
  var followersCounter: ActorRef = _

  override def preStart(): Unit = {
    log.info("Starting StatisticsProvider")
    followersCounter = context.actorOf(Props[UserFollowersCounter], name = "userFollowersCounter")
    storage = context.actorOf(Props[Storage], name = "storage")
    reachComputer = context.actorOf(Props[TweetReachComputer], name = "tweetReachComputer")

    context.watch(storage)
  }

  override def supervisorStrategy: SupervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 2.minutes) {
      case _: ConnectionException =>
        Restart
      case t: Throwable =>
        super.supervisorStrategy.decider.applyOrElse(t, (_: Any) => Escalate)
    }

  def receive = {
    case reach @ ComputeReach(_) =>
      log.info("Forwarding ComputeReach message to the reach computer")
      reachComputer forward reach
    case Terminated(terminatedStorageRef) =>
      context.system.scheduler.scheduleOnce(1.minute, self, ReviveStorage)
      context.become({
        case reach @ ComputeReach(_) =>
          sender() ! ServiceUnavailable
        case ReviveStorage =>
          storage = context.actorOf(Props[Storage], name = "storage")
          context.unbecome()
      })
  }

}

case object ServiceUnavailable
case object ReviveStorage