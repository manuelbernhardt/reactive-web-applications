package services

import play.api.Play
import play.api.Play.current
import play.api.libs.oauth.{OAuthCalculator, RequestToken, ConsumerKey}
import play.api.libs.ws.WS

import scala.concurrent.{ExecutionContext, Future}

case class TwitterCounts(followersCount: Long, friendsCount: Long)

trait TwitterService {
  
  def fetchRelationshipCounts(userName: String)(implicit ec: ExecutionContext): Future[TwitterCounts]

  def postTweet(message: String)(implicit ec: ExecutionContext): Future[Unit]

}

class WSTwitterService extends TwitterService {

  override def fetchRelationshipCounts(userName: String)(implicit ec: ExecutionContext): Future[TwitterCounts] = {

    credentials.map {
      case (consumerKey, requestToken) =>
        WS.url("https://api.twitter.com/1.1/users/show.json")
          .sign(OAuthCalculator(consumerKey, requestToken))
          .withQueryString("screen_name" -> userName)
          .get().map { response =>
            if (response.status == 200) {
                TwitterCounts(
                  (response.json \ "followers_count").as[Long],
                  (response.json \ "friends_count").as[Long]
                )
            } else {
              throw new TwitterServiceException(s"Could not retrieve counts for Twitter user $userName")
            }
        }
    }.getOrElse {
      Future.failed(new TwitterServiceException("You did not correctly configure the Twitter credentials"))
    }

  }

  override def postTweet(message: String)(implicit ec: ExecutionContext): Future[Unit] = Future.successful {
    println("TWITTER: " + message)
  }

  private def credentials = for {
    apiKey <- Play.configuration.getString("twitter.apiKey")
    apiSecret <- Play.configuration.getString("twitter.apiSecret")
    token <- Play.configuration.getString("twitter.accessToken")
    tokenSecret <- Play.configuration.getString("twitter.accessTokenSecret")
  } yield (ConsumerKey(apiKey, apiSecret), RequestToken(token, tokenSecret))

}

case class TwitterServiceException(message: String) extends RuntimeException(message)