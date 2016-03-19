package services

import javax.inject._

import akka.actor._
import org.reactivestreams.Publisher
import play.api._
import play.api.libs.iteratee._
import play.api.libs.json._
import play.api.libs.oauth._
import play.api.libs.streams.Streams
import play.api.libs.ws._
import play.extras.iteratees._
import akka.stream._
import akka.stream.scaladsl._

import scala.concurrent.ExecutionContext

@Singleton
class TwitterStreamService @Inject() (
  ws: WSAPI,
  system: ActorSystem,
  executionContext: ExecutionContext,
  configuration: Configuration
) {

  def stream(topicsAndDigestRate: Map[String, Int]): Enumerator[JsValue] = {

    import FanOutShape._

    class SplitByTopicShape[A <: JsObject](
      _init: Init[A] = Name[A]("SplitByTopic")
    ) extends FanOutShape[A](_init) {

      protected override def construct(i: Init[A]) = new SplitByTopicShape(i)

      val topicOutlets = topicsAndDigestRate.keys.map { topic =>
        topic -> newOutlet[A]("out-" + topic)
      }.toMap
    }

    class SplitByTopic[A <: JsObject]
      extends FlexiRoute[A, SplitByTopicShape[A]](
        new SplitByTopicShape, Attributes.name("SplitByTopic")
      ) {

      import FlexiRoute._

      override def createRouteLogic(p: PortT) = new RouteLogic[A] {
        def extractFirstHashTag(tweet: JsObject) =
          (tweet \ "entities" \ "hashtags")
            .asOpt[JsArray]
            .flatMap { hashtags =>
              hashtags.value.headOption.map { hashtag =>
                (hashtag \ "text").as[String]
              }
            }
        override def initialState =
          State[Any](DemandFromAny(p.topicOutlets.values.toSeq :_*)) {
            (ctx, _, element) =>
              extractFirstHashTag(element).foreach { topic =>
                p.topicOutlets.get(topic).foreach { port =>
                  ctx.emit(port)(element)
                }
              }
              SameState
          }
        override def initialCompletionHandling = eagerClose
      }
    }


    credentials.map { case (consumerKey, requestToken) =>

      implicit val fm = ActorMaterializer()(system)

      val enumerator = buildTwitterEnumerator(
        consumerKey, requestToken, topicsAndDigestRate.keys.toSeq
      )
      val sink = Sink.publisher[JsValue]
      val graph = FlowGraph.closed(sink) { implicit builder => out =>
        val in = builder.add(enumeratorToSource(enumerator))
        val splitter = builder.add(new SplitByTopic[JsObject])
        val groupers = topicsAndDigestRate.map { case (topic, rate) =>
          topic -> builder.add(Flow[JsObject].grouped(rate))
        }
        val taggers = topicsAndDigestRate.map { case (topic, _) =>
          topic -> {
            val t = Flow[Seq[JsObject]].map { tweets =>
              Json.obj("topic" -> topic, "tweets" -> tweets)
            }
            builder.add(t)
          }
        }
        val merger = builder.add(Merge[JsValue](topicsAndDigestRate.size))

        builder.addEdge(in, splitter.in)
        splitter.topicOutlets.zipWithIndex.foreach { case ((topic, port), index) =>
          val grouper = groupers(topic)
          val tagger = taggers(topic)
          builder.addEdge(port, grouper.inlet)
          builder.addEdge(grouper.outlet, tagger.inlet)
          builder.addEdge(tagger.outlet, merger.in(index))
        }
        builder.addEdge(merger.out, out.inlet)
      }

      val publisher = graph.run()
      Streams.publisherToEnumerator(publisher)

    } getOrElse {
      Logger.error("Twitter credentials are not configured")
      Enumerator.empty[JsValue]
    }

  }

  private def buildTwitterEnumerator(
    consumerKey: ConsumerKey,
    requestToken: RequestToken,
    topics: Seq[String]
  ): Enumerator[JsObject] = {

    val (iteratee, enumerator) = Concurrent.joined[Array[Byte]]

      val url =
        "https://stream.twitter.com/1.1/statuses/filter.json"

      implicit val ec = executionContext

      val formattedTopics = topics
        .map(t => "#" + t)
        .mkString(",")

      ws
        .url(url)
        .sign(OAuthCalculator(consumerKey, requestToken))
        .postAndRetrieveStream(
          Map("track" -> Seq(formattedTopics))
        ) { response =>
          Logger.info("Status: " + response.status)
          iteratee
        }.map { _ =>
          Logger.info("Twitter stream closed")
        }

      val jsonStream: Enumerator[JsObject] = enumerator &>
        Encoding.decode() &>
        Enumeratee.grouped(JsonIteratees.jsSimpleObject)

    jsonStream
  }

  private def enumeratorToSource[Out](
    enum: Enumerator[Out]
  ): Source[Out, Unit] = {
    val publisher: Publisher[Out] =
      Streams.enumeratorToPublisher(enum)
    Source(publisher)
  }


  private def credentials = for {
    apiKey <- configuration.getString("twitter.apiKey")
    apiSecret <- configuration.getString("twitter.apiSecret")
    token <- configuration.getString("twitter.accessToken")
    tokenSecret <- configuration.getString("twitter.accessTokenSecret")
  } yield
    (ConsumerKey(apiKey, apiSecret), RequestToken(token, tokenSecret))

}