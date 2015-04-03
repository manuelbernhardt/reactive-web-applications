package actors

import akka.actor.{ActorRef, Props, Actor, ActorLogging}
import messages._

import scala.collection.mutable
import scala.concurrent.Future

class StatisticsProvider extends Actor with ActorLogging {

  var reachComputer: ActorRef = _
  var storage: ActorRef = _
  var followersCounter: ActorRef = _

  override def preStart(): Unit = {
    log.info("Starting StatisticsProvider")
    followersCounter = context.actorOf(Props[UserFollowersCounter], name = "userFollowersCounter")
    storage = context.actorOf(Props[Storage], name = "storage")
    reachComputer = context.actorOf(Props[TweetReachComputer], name = "tweetReachComputer")
  }


  def receive = {
    case reach @ ComputeReach =>
      reachComputer forward reach
  }

}


class TweetReachComputer extends Actor with ActorLogging {

  lazy val userFollowersCounter = context.actorSelection("../userFollowersCounter")
  lazy val storage = context.actorSelection("../storage")

  var followerCountsByRetweet = new mutable.HashMap[FetchedRetweets, List[FollowerCount]]

  def receive = {
    case ComputeReach(tweet_id) =>
      fetchRetweets(tweet_id, sender()).map { fetchedRetweets =>
        followerCountsByRetweet += fetchedRetweets -> List.empty
        fetchedRetweets.retweets.foreach { rt =>
          userFollowersCounter ! FetchFollowerCount(tweet_id, rt.user)
        }
      }
    case count @ FollowerCount(tweet_id, user, followersCount) =>
      followerCountsByRetweet.keys.find(_.tweet_id == tweet_id).foreach { fetchedRetweets =>
        val existingCounts = followerCountsByRetweet(fetchedRetweets)
        followerCountsByRetweet.update(fetchedRetweets, count :: existingCounts)
        val newCounts = followerCountsByRetweet(fetchedRetweets)
        if (newCounts.length == fetchedRetweets.retweets.length) {
          val score = newCounts.map(_.followersCount).sum
          fetchedRetweets.client ! TweetReach(tweet_id, score)
          storage ! StoreReach(tweet_id, score)
        }
      }
    case ReachStored(tweet_id) =>
      followerCountsByRetweet.keys.find(_.tweet_id == tweet_id).foreach { key =>
        followerCountsByRetweet.remove(key)
      }

  }


  case class FetchedRetweets(tweet_id: BigInt, retweets: List[Retweet], client: ActorRef)
  case class Retweet(tweet_id: BigInt, user: String)

  def fetchRetweets(tweet_id: BigInt, client: ActorRef): Future[FetchedRetweets] = ???

}

class UserFollowersCounter extends Actor with ActorLogging {
  def receive = {
    case _ =>
  }
}
class Storage extends Actor with ActorLogging {
    def receive = {
    case _ =>
  }
}