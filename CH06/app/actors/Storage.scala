package actors

import actors.Storage._
import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.pattern.pipe
import messages.{ReachStored, StoreReach}
import org.joda.time.DateTime
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import reactivemongo.bson._
import reactivemongo.core.errors.ConnectionException

case class StoredReach(when: DateTime, tweetId: BigInt, score: Int)

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
      "tweetId" -> r.tweetId,
      "tweet_id" -> r.tweetId,
      "score" -> r.score
    )
  }

}

class Storage() extends Actor with ActorLogging {

  val Database = "twitterService"
  val ReachCollection = "ComputedReach"

  implicit val executionContext = context.dispatcher

  val driver: MongoDriver = new MongoDriver
  var connection: MongoConnection = _
  var db: DefaultDB = _
  var collection: BSONCollection = _
  obtainConnection()

  override def postRestart(reason: Throwable): Unit = {
    reason match {
      case ce: ConnectionException =>
        // try to obtain a brand new connection
        obtainConnection()
    }
    super.postRestart(reason)
  }

  override def postStop(): Unit = {
    connection.close()
    driver.close()
  }
  
  var currentWrites = Set.empty[BigInt]

  def receive = {
    case StoreReach(tweetId, score) =>
      log.info("Storing reach for tweet {}", tweetId)
      if (!currentWrites.contains(tweetId)) {
        currentWrites = currentWrites + tweetId
        val originalSender = sender()
        collection.insert(StoredReach(DateTime.now, tweetId, score)).map { lastError =>
          LastStorageError(lastError, tweetId, originalSender)
        }.recover {
          case _ =>
            currentWrites = currentWrites - tweetId
        } pipeTo self
      }
    case LastStorageError(error, tweetId, client) =>
      if(error.inError) {
        currentWrites = currentWrites - tweetId
      } else {
        client ! ReachStored(tweetId)
      }
    }

  private def obtainConnection(): Unit = {
    connection = driver.connection(List("localhost"))
    db = connection.db(Database)
    collection = db.collection[BSONCollection](ReachCollection)
  }
}

object Storage {
  case class LastStorageError(result: WriteResult, tweetId: BigInt, client: ActorRef)
}