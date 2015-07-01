package actors

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor._
import messages.ComputeReach
import org.joda.time.{Interval, DateTime}
import reactivemongo.core.errors.ConnectionException
import scala.concurrent.duration._
import StatisticsProvider._

class StatisticsProvider extends Actor with ActorLogging {

  var reachComputer: ActorRef = _
  var storage: ActorRef = _
  var followersCounter: ActorRef = _

  implicit val ec = context.dispatcher

  override def preStart(): Unit = {
    log.info("Starting StatisticsProvider")
    followersCounter = context.actorOf(Props[UserFollowersCounter], name = "userFollowersCounter")
    storage = context.actorOf(Props[Storage], name = "storage")
    reachComputer = context.actorOf(TweetReachComputer.props(followersCounter, storage), name = "tweetReachComputer")

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
    case reach: ComputeReach =>
      log.info("Forwarding ComputeReach message to the reach computer")
      reachComputer forward reach
    case Terminated(terminatedStorageRef) =>
      context.system.scheduler.scheduleOnce(1.minute, self, ReviveStorage)
      context.become(storageUnavailable)
    case TwitterRateLimitReached(reset) =>
      context.system.scheduler.scheduleOnce(
        new Interval(DateTime.now, reset).toDurationMillis.millis,
        self,
        ResumeService
      )
      context.become(serviceUnavailable)
    case UserFollowersCounterUnavailable =>
      context.become(followersCountUnavailable)
  }
  
  def storageUnavailable: Receive = {
    case reach @ ComputeReach(_) =>
      sender() ! ServiceUnavailable
    case ReviveStorage =>
      storage = context.actorOf(Props[Storage], name = "storage")
      context.unbecome()
  }

  def serviceUnavailable: Receive = {
    case reach: ComputeReach =>
      sender() ! ServiceUnavailable
    case ResumeService =>
      context.unbecome()
  }

  def followersCountUnavailable: Receive = {
      case UserFollowersCounterAvailable =>
        context.unbecome()
      case reach: ComputeReach =>
        sender() ! ServiceUnavailable
    }

}

object StatisticsProvider {
  def props = Props[StatisticsProvider]

  case object ServiceUnavailable
  case object ReviveStorage
  case object ResumeService
}

