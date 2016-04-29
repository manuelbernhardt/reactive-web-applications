package actors

import java.util.Locale

import akka.actor.{Cancellable, ActorRef, ActorLogging}
import akka.persistence.{RecoveryCompleted, RecoveryFailure, PersistentActor}
import helpers.TwitterCredentials
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.JsArray
import play.api.libs.oauth.{ConsumerKey, RequestToken, OAuthCalculator}
import play.api.libs.ws.WS
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.pattern.pipe
import play.api.Play.current

import scala.util.control.NonFatal

class ClientCommandHandler(phoneNumber: String, userName: String) extends PersistentActor with ActorLogging with TwitterCredentials {

  implicit val ec = context.dispatcher

  override def postStop(): Unit = {
    subscriptionScheduler.foreach(_.cancel())
  }

  override def persistenceId: String = phoneNumber

  var subscribedSMSHandler: Option[ActorRef] = None
  var lastSeenMentionTime: Option[DateTime] = None
  var subscriptionScheduler: Option[Cancellable] = None
  var unacknowledgedMentions = List.empty[MentionReceived]

  override def receiveRecover = {
    case RecoveryFailure(cause) => log.error(cause, "Failed to recover!")
    case RecoveryCompleted => log.info("Recovery completed")
    case evt: Event => handleEvent(evt)
  }

  override def receiveCommand = {

    case SubscribeMentions(_) =>
      if (subscriptionScheduler.isDefined) {
        sender() ! InvalidCommand("Already subscribed to mentions")
      } else {
        persist(MentionsSubscribed())(handleEvent)
      }

    case CheckMentions =>
      val maybeMentions = for {
        (consumerKey, requestToken) <- credentials
        time <- lastSeenMentionTime
      } yield fetchMentions(consumerKey, requestToken, userName, time)

      maybeMentions.foreach { mentions =>
        mentions.map { m =>
          Mentions(m)
        } recover { case NonFatal(t) =>
          log.error(t, "Could not fetch mentions")
          Mentions(Seq.empty)
        } pipeTo self
      }

    case Mentions(mentions) =>
      log.info("Fetched {} mentions", mentions.length)
      val orderedMentions = mentions.sortBy(_._2.getMillis)
      orderedMentions.foreach { mention =>
        persist(MentionReceived(mention._1, mention._2, mention._3, mention._4))(handleEvent)
      }

    case AcknowledgeMention(id) =>
      persist(MentionAcknowledged(id))(handleEvent)

    case ConnectUser(_) =>
      log.info("{} connected, about to send it {} mentions", phoneNumber, unacknowledgedMentions.size)
      subscribedSMSHandler = Some(sender())
      unacknowledgedMentions.foreach { mention =>
        subscribedSMSHandler.foreach { handler =>
          handler ! mention
        }
      }

  }


  def handleEvent(event: Event): Unit = event match {
    case subscribed @ MentionsSubscribed(timestamp) =>
      subscribedSMSHandler = Some(sender())
      lastSeenMentionTime = Some(timestamp)
      subscriptionScheduler = Some(context.system.scheduler.schedule(
        initialDelay = 10.seconds,
        interval = 60.seconds,
        receiver = self,
        message = CheckMentions
      )(context.dispatcher))
      log.info("Subscribed {} to mentions", phoneNumber)
      if (recoveryFinished) {
        sender() ! subscribed
        context.system.eventStream.publish(
          ClientEvent(phoneNumber, userName, subscribed)
        )
      }

    case received @ MentionReceived(id, created_on, from, text, _) =>
      lastSeenMentionTime = Some(created_on)
      unacknowledgedMentions = received :: unacknowledgedMentions
      if (recoveryFinished) {
        subscribedSMSHandler.foreach { handler =>
          handler ! received
        }
        context.system.eventStream.publish(
          ClientEvent(phoneNumber, userName, received)
        )
      }

    case MentionAcknowledged(id, _) =>
      log.info("Acknowledged delivery of mention {}", id)
      unacknowledgedMentions = unacknowledgedMentions.filterNot(_.id == id)
  }

  def fetchMentions(consumerKey: ConsumerKey, requestToken: RequestToken, user: String, time: DateTime): Future[Seq[(String, DateTime, String, String)]] = {
    val df = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss Z yyyy").withLocale(Locale.ENGLISH)

    WS.url("https://api.twitter.com/1.1/search/tweets.json")
      .sign(OAuthCalculator(consumerKey, requestToken))
      .withQueryString("q" -> s"@$user")
      .get()
      .map { response =>
        val mentions = (response.json \ "statuses").as[JsArray].value.map { status =>
          val id = (status \ "id_str").as[String]
          val text = (status \ "text").as[String]
          val from = (status \ "user" \ "screen_name").as[String]
          val created_at = df.parseDateTime((status \ "created_at").as[String])

          (id, created_at, from, text)
        }

        mentions.filter(_._2.isAfter(time))
    }
  }


}

object CheckMentions
case class Mentions(mentions: Seq[(String, DateTime, String, String)])