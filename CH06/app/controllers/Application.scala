package controllers

import javax.inject._

import actors.StatisticsProvider
import akka.actor.ActorSystem
import akka.util.Timeout
import messages.{TweetReachCouldNotBeComputed, TweetReach, ComputeReach}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._

import akka.pattern.ask
import scala.concurrent.duration._

class Application @Inject() (system: ActorSystem) extends Controller {

  lazy val statisticsProvider = system.actorSelection("akka://application/user/statisticsProvider")

  def computeReach(tweetId: String) = Action.async {
    implicit val timeout = Timeout(5.minutes)
    val eventuallyReach = statisticsProvider ? ComputeReach(BigInt(tweetId))
    eventuallyReach.map {
      case tr: TweetReach =>
        Ok(tr.score.toString)
      case StatisticsProvider.ServiceUnavailable =>
        ServiceUnavailable("Sorry")
      case TweetReachCouldNotBeComputed =>
        ServiceUnavailable("Sorry")
    }
  }

}