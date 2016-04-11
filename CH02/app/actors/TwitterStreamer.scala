package actors

import akka.actor._
import play.api._
import play.api.Play.current
import play.api.libs.iteratee._
import play.api.libs.iteratee.Concurrent.Broadcaster
import play.api.libs.json._
import play.api.libs.oauth._
import play.api.libs.ws.WS
import play.extras.iteratees._
import play.api.libs.concurrent.Execution.Implicits._

import scala.collection.mutable.ArrayBuffer

class TwitterStreamer(out: ActorRef) extends Actor {
  def receive = {
    case "subscribe" =>
      Logger.info("Received subscription from a client")
      TwitterStreamer.subscribe(out)
  }

  override def postStop() {
    Logger.info("Client unsubscribing from stream")
    TwitterStreamer.unsubscribe(out)
  }
}

object TwitterStreamer {

  private var broadcastEnumerator: Option[Enumerator[JsObject]] = None

  private var broadcaster: Option[Broadcaster] = None

  private val subscribers = new ArrayBuffer[ActorRef]()

  def props(out: ActorRef) = Props(new TwitterStreamer(out))

  def subscribe(out: ActorRef): Unit = {

    if (broadcastEnumerator.isEmpty) {
      init()
    }

    def twitterClient: Iteratee[JsObject, Unit] = Cont {
      case in@Input.EOF => Done(None)
      case in@Input.El(o) =>
        if (subscribers.contains(out)) {
          out ! o
          twitterClient
        } else {
          Done(None)
        }
      case in@Input.Empty =>
        twitterClient
    }

    broadcastEnumerator.foreach { enumerator =>
      enumerator run twitterClient
    }
    subscribers += out
  }

  def unsubscribe(subscriber: ActorRef): Unit = {
      val index = subscribers.indexWhere(_ == subscriber)
      if (index > 0) {
        subscribers.remove(index)
        Logger.info("Unsubscribed client from stream")
      }
  }

  def subscribeNode: Enumerator[JsObject] = {
    if (broadcastEnumerator.isEmpty) {
      TwitterStreamer.init()
    }

    broadcastEnumerator.getOrElse {
      Enumerator.empty[JsObject]
    }
  }

  def init(): Unit = {

    credentials.map { case (consumerKey, requestToken) =>

      val (iteratee, enumerator) = Concurrent.joined[Array[Byte]]

      val jsonStream: Enumerator[JsObject] = enumerator &>
        Encoding.decode() &>
        Enumeratee.grouped(JsonIteratees.jsSimpleObject)

      val (e, b) = Concurrent.broadcast(jsonStream)

      broadcastEnumerator = Some(e)
      broadcaster = Some(b)

      val maybeMasterNodeUrl = Option(System.getProperty("masterNodeUrl"))
      val url = maybeMasterNodeUrl.getOrElse {
        "https://stream.twitter.com/1.1/statuses/filter.json"
      }

      WS
        .url(url)
        .sign(OAuthCalculator(consumerKey, requestToken))
        .withQueryString("track" -> "cat")
        .get { response =>
        Logger.info("Status: " + response.status)
        iteratee
      }.map { _ =>
        Logger.info("Twitter stream closed")
      }

    } getOrElse {
      Logger.error("Twitter credentials are not configured")
    }

  }

  private def credentials = for {
    apiKey <- Play.configuration.getString("twitter.apiKey")
    apiSecret <- Play.configuration.getString("twitter.apiSecret")
    token <- Play.configuration.getString("twitter.token")
    tokenSecret <- Play.configuration.getString("twitter.tokenSecret")
  } yield (ConsumerKey(apiKey, apiSecret), RequestToken(token, tokenSecret))


}
