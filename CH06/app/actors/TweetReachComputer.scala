package actors

import akka.actor._
import akka.pattern.pipe
import messages._
import play.api.libs.json.JsArray
import play.api.libs.oauth.OAuthCalculator
import play.api.libs.ws.WS

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.control.NonFatal

import play.api.Play.current

class TweetReachComputer(userFollowersCounter: ActorRef, storage: ActorRef) extends Actor with ActorLogging with TwitterCredentials {

  var followerCountsByRetweet = Map.empty[FetchedRetweet, List[FollowerCount]]

  implicit val executionContext = context.dispatcher

  var retryScheduler: Cancellable = context.system.scheduler.schedule(10.millis, 20.seconds, self, ResendUnacknowledged)

  override def postStop(): Unit = {
    retryScheduler.cancel()
  }

  def receive = {

    case ComputeReach(tweetId) =>
      log.info("Starting to compute tweet reach for tweet {}", tweetId)
      val originalSender = sender()
      fetchRetweets(tweetId, sender()).recover {
        case NonFatal(t) =>
          RetweetFetchingFailed(tweetId, t, originalSender)
      } pipeTo self

    case fetchedRetweets: FetchedRetweet =>
      log.info("Received retweets for tweet {}", fetchedRetweets.tweetId)
      followerCountsByRetweet =
        followerCountsByRetweet + (fetchedRetweets -> List.empty)
      fetchedRetweets.retweeters.foreach { rt =>
        userFollowersCounter ! FetchFollowerCount(fetchedRetweets.tweetId, rt)
      }


    case count @ FollowerCount(tweetId, _, _) =>
      log.info("Received followers count for tweet {}", tweetId)
      fetchedRetweetsFor(tweetId).foreach { fetchedRetweets =>
        updateFollowersCount(tweetId, fetchedRetweets, count)
      }

    case FollowerCountUnavailable(tweetId, user) =>
      followerCountsByRetweet.keys.find(_.tweetId == tweetId).foreach { fetchedRetweets =>
        fetchedRetweets.client ! TweetReachCouldNotBeComputed
      }

    case ReachStored(tweetId) =>
      log.info("Received confirmation of storage for {}", tweetId)
      followerCountsByRetweet.keys.find(_.tweetId == tweetId).foreach { key =>
        followerCountsByRetweet = followerCountsByRetweet.filterNot(_._1 == key)
      }

    case RetweetFetchingFailed(tweetId, cause, client) =>
      log.error(cause, "Could not fetch retweets for tweet {}", tweetId)

    case ResendUnacknowledged =>
      val unacknowledged = followerCountsByRetweet.filterNot { case (retweet, counts) =>
        retweet.retweeters.size != counts.size
      }
      unacknowledged.foreach { case (retweet, counts) =>
        storage ! StoreReach(retweet.tweetId, counts.map(_.followersCount).sum)
      }

  }

  case class FetchedRetweet(tweetId: BigInt, retweeters: List[BigInt], client: ActorRef)
  case class RetweetFetchingFailed(tweetId: BigInt, cause: Throwable, client: ActorRef)
  case object ResendUnacknowledged

  def fetchedRetweetsFor(tweetId: BigInt) =
    followerCountsByRetweet.keys.find(_.tweetId == tweetId)

  def updateFollowersCount(tweetId: BigInt, fetchedRetweets: FetchedRetweet, count: FollowerCount) = {
    val existingCounts = followerCountsByRetweet(fetchedRetweets)
    followerCountsByRetweet = followerCountsByRetweet.updated(fetchedRetweets, count :: existingCounts)
    val newCounts = followerCountsByRetweet(fetchedRetweets)
    if (newCounts.length == fetchedRetweets.retweeters.length) {
      log.info("Received all retweeters followers count for tweet {}, computing sum", tweetId)
      val score = newCounts.map(_.followersCount).sum
      fetchedRetweets.client ! TweetReach(tweetId, score)
      storage ! StoreReach(tweetId, score)
    }
  }

  def fetchRetweets(tweetId: BigInt, client: ActorRef): Future[FetchedRetweet] = {
    credentials.map {
      case (consumerKey, requestToken) =>
        WS.url("https://api.twitter.com/1.1/statuses/retweeters/ids.json")
          .sign(OAuthCalculator(consumerKey, requestToken))
          .withQueryString("id" -> tweetId.toString)
          .withQueryString("stringify_ids" -> "true")
          .get().map { response =>
            if (response.status == 200) {
              val ids = (response.json \ "ids").as[JsArray].value.map(v => BigInt(v.as[String])).toList
              FetchedRetweet(tweetId, ids, client)
            } else {
              throw new RuntimeException(s"Could not retrieve details for Tweet $tweetId")
            }
        }
    }.getOrElse {
      Future.failed(new RuntimeException("You did not correctly configure the Twitter credentials"))
    }
  }

}

object TweetReachComputer {
  def props(followersCounter: ActorRef, storage: ActorRef) =
    Props(classOf[TweetReachComputer], followersCounter, storage)
}
