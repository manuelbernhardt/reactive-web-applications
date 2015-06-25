package controllers

import akka.util.Timeout
import messages.{TweetReachCouldNotBeComputed, TweetReach, ComputeReach}
import play.api._
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._

import akka.pattern.ask
import scala.concurrent.duration._


object Application extends Controller {

  lazy val statisticsProvider = Akka.system.actorSelection("akka://application/user/statisticsProvider")

  def computeReach(tweet_id: String) = Action.async {
    implicit val timeout = Timeout(5.minutes)
    val eventuallyReach = statisticsProvider ? ComputeReach(BigInt(tweet_id))
    eventuallyReach.map {
      case tr: TweetReach =>
        Ok(tr.score.toString)
      case TweetReachCouldNotBeComputed =>
        ServiceUnavailable("Sorry")
    }
  }

}