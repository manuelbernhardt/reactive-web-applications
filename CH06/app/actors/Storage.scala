package actors

import akka.actor.{ActorLogging, Actor}
import messages.{ReachNotStored, ReachStored, StoreReach}
import org.joda.time.DateTime
import reactivemongo.api.{MongoConnection, MongoDriver}
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson._
import reactivemongo.core.errors.ConnectionException

import scala.collection.mutable

case class StoredReach(when: DateTime, tweet_id: BigInt, score: Int)

object StoredReach {

  implicit object BigIntHandler extends BSONDocumentReader[BigInt] with BSONDocumentWriter[BigInt] {
    def write(bigInt: BigInt): BSONDocument = BSONDocument(
      "signum" -> bigInt.signum,
      "value" -> BSONBinary(bigInt.toByteArray, Subtype.UserDefinedSubtype))

    def read(doc: BSONDocument): BigInt = BigInt(
    doc.getAs[Int]("signum").get, {
      val buf = doc.getAs[BSONBinary]("value").get.value
      buf.readArray(buf.readable())
    })
  }

  implicit object StoredReachHandler extends BSONDocumentReader[StoredReach] with BSONDocumentWriter[StoredReach] {
    override def read(bson: BSONDocument): StoredReach = {
      val when = bson.getAs[BSONDateTime]("when").map(t => new DateTime(t.value)).get
      val tweetId = bson.getAs[BigInt]("tweet_id").get
      val score = bson.getAs[Int]("score").get
      StoredReach(when, tweetId, score)
    }

    override def write(r: StoredReach): BSONDocument = BSONDocument(
      "when" -> BSONDateTime(r.when.getMillis),
      "tweetId" -> r.tweet_id,
      "tweet_id" -> r.tweet_id,
      "score" -> r.score
    )
  }

}

class Storage extends Actor with ActorLogging {

  val Database = "twitterService"
  val ReachCollection = "ComputedReach"

  var driver: MongoDriver = _
  var connection: MongoConnection = _
  var collection: BSONCollection = _

  implicit val executionContext = context.dispatcher

  override def preStart(): Unit = {
    driver = new MongoDriver(context.system)
    connection = driver.connection(List("localhost"))
    val db = connection.db(Database)
    collection = db.collection[BSONCollection](ReachCollection)
  }

  override def postRestart(reason: Throwable): Unit = {
    reason match {
      case ce: ConnectionException =>
        // try to obtain a brand new connection
        connection = driver.connection(List("localhost"))
        val db = connection.db(Database)
        collection = db.collection[BSONCollection](ReachCollection)
    }
    super.postRestart(reason)
  }

  override def postStop(): Unit = {
    connection.close()
    driver.close()
  }
  
  val currentWrites = new mutable.HashSet[BigInt]

  def receive = {
    case StoreReach(tweet_id, score) =>
      log.info(s"Storing reach for tweet $tweet_id")
      val originalSender = sender()
      if (!currentWrites.contains(tweet_id)) {
        currentWrites += tweet_id
        collection.save(StoredReach(DateTime.now, tweet_id, score)).map { lastError =>
          if(lastError.inError) {
            currentWrites.remove(tweet_id)
            originalSender ! ReachNotStored(tweet_id)
          } else {
            originalSender ! ReachStored(tweet_id)
          }
        } recover {
          case _ =>
            currentWrites.remove(tweet_id)
            originalSender ! ReachNotStored(tweet_id)
        }
      }
  }

}