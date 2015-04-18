package actors

import akka.actor._
import akka.pattern.pipe
import messages._
import play.api.libs.json.JsArray
import play.api.libs.oauth.OAuthCalculator
import play.api.libs.ws.WS

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.control.NonFatal

import play.api.Play.current



class TweetReachComputer extends Actor with ActorLogging with TwitterCredentials {

  lazy val userFollowersCounter = context.actorSelection("../userFollowersCounter")
  lazy val storage = context.actorSelection("../storage")

  val followerCountsByRetweet = new mutable.HashMap[FetchedRetweet, List[FollowerCount]]

  implicit val executionContext = context.dispatcher

  var retryScheduler: Cancellable = _

  override def preStart(): Unit = {
    retryScheduler = context.system.scheduler.schedule(10.millis, 20.seconds, self, ResendUnacknowledged)
  }

  override def postStop(): Unit = {
    retryScheduler.cancel()
  }

  def receive = {

    case ComputeReach(tweet_id) =>
      log.info(s"Starting to compute tweet reach for tweet $tweet_id")
      val originalSender = sender()
      fetchRetweets(tweet_id, sender()).recover {
        case NonFatal(t) =>
          RetweetFetchingFailed(tweet_id, t, originalSender)
      } pipeTo self

    case fetchedRetweets: FetchedRetweet =>
      log.info(s"Received retweets for tweet ${fetchedRetweets.tweet_id}")
      followerCountsByRetweet += fetchedRetweets -> List.empty
      fetchedRetweets.retweeters.foreach { rt =>
        userFollowersCounter ! FetchFollowerCount(fetchedRetweets.tweet_id, rt)
      }

    case count @ FollowerCount(tweet_id, user, followersCount) =>
      log.info(s"Received followers count for tweet $tweet_id")
      followerCountsByRetweet.keys.find(_.tweet_id == tweet_id).foreach { fetchedRetweets =>
        val existingCounts = followerCountsByRetweet(fetchedRetweets)
        followerCountsByRetweet.update(fetchedRetweets, count :: existingCounts)
        val newCounts = followerCountsByRetweet(fetchedRetweets)
        if (newCounts.length == fetchedRetweets.retweeters.length) {
          log.info(s"Received all retweeters followers count for tweet $tweet_id, computing sum")
          val score = newCounts.map(_.followersCount).sum
          fetchedRetweets.client ! TweetReach(tweet_id, score)
          storage ! StoreReach(tweet_id, score)
        }
      }

    case ReachStored(tweet_id) =>
      log.info(s"Received confirmation of storage for $tweet_id")
      followerCountsByRetweet.keys.find(_.tweet_id == tweet_id).foreach { key =>
        followerCountsByRetweet.remove(key)
      }

    case RetweetFetchingFailed(tweet_id, cause, client) =>
      log.error(cause, s"Could not fetch retweets for tweet $tweet_id")

    case ResendUnacknowledged =>
      val unacknowledged = followerCountsByRetweet.filterNot { case (retweet, counts) =>
        retweet.retweeters.size != counts.size
      }
      unacknowledged.foreach { case (retweet, counts) =>
        storage ! StoreReach(retweet.tweet_id, counts.map(_.followersCount).sum)
      }

  }

  case class FetchedRetweet(tweet_id: BigInt, retweeters: List[BigInt], client: ActorRef)
  case class RetweetFetchingFailed(tweet_id: BigInt, cause: Throwable, client: ActorRef)
  case object ResendUnacknowledged

  def fetchRetweets(tweet_id: BigInt, client: ActorRef): Future[FetchedRetweet] = {
    credentials.map {
      case (consumerKey, requestToken) =>
        WS.url("https://api.twitter.com/1.1/statuses/retweeters/ids.json")
          .sign(OAuthCalculator(consumerKey, requestToken))
          .withQueryString("id" -> tweet_id.toString)
          .withQueryString("stringify_ids" -> "true")
          .get().map { response =>
            if (response.status == 200) {
              val ids = (response.json \ "ids").as[JsArray].value.map(v => BigInt(v.as[String])).toList
              FetchedRetweet(tweet_id, ids, client)
            } else {
              throw new RuntimeException(s"Could not retrieve details for Tweet $tweet_id")
            }
        }
    }.getOrElse {
      Future.failed(new RuntimeException("You did not correctly configure the Twitter credentials"))
    }
  }

}