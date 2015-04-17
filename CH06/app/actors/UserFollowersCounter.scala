package actors

import akka.actor.{ActorLogging, Actor}
import messages.{FollowerCount, FetchFollowerCount}
import play.api.libs.oauth.OAuthCalculator
import play.api.libs.ws.WS
import play.api.Play.current

import akka.pattern.pipe

import scala.concurrent.Future

class UserFollowersCounter extends Actor with ActorLogging with TwitterCredentials {

  implicit val ec = context.dispatcher

  def receive = {
    case FetchFollowerCount(tweet_id, user) =>
      val originalSender = sender()
      fetchFollowerCount(tweet_id, user) pipeTo originalSender
  }

  private def fetchFollowerCount(tweet_id: BigInt, userId: BigInt) = {
    credentials.map {
      case (consumerKey, requestToken) =>
        WS.url("https://api.twitter.com/1.1/users/show.json")
          .sign(OAuthCalculator(consumerKey, requestToken))
          .withQueryString("user_id" -> userId.toString)
          .get().map { response =>
            if (response.status == 200) {
              val count = (response.json \ "followers_count").as[Int]
              FollowerCount(tweet_id, userId, count)
            } else {
              throw new RuntimeException(s"Could not retrieve followers count for user $userId")
            }
        }
    }.getOrElse {
      Future.failed(new RuntimeException("You did not correctly configure the Twitter credentials"))
    }
  }

}