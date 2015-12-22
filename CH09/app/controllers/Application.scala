package controllers

import javax.inject.Inject

import play.api.libs.iteratee._
import play.api.libs.json._
import play.api.mvc._
import services.TwitterStreamService

class Application @Inject() (twitterStream: TwitterStreamService)
  extends Controller {

  def index = Action { implicit request =>
    val parsedTopics = parseTopicsAndDigestRate(request.queryString)
    Ok(views.html.index(parsedTopics, request.rawQueryString))
  }

  def stream = WebSocket.using[JsValue] { request =>
    val parsedTopics = parseTopicsAndDigestRate(request.queryString)
    val out = twitterStream.stream(parsedTopics)
    val in: Iteratee[JsValue, Unit] = Iteratee.ignore[JsValue]
    (in, out)
  }

  private def parseTopicsAndDigestRate(
    queryString: Map[String, Seq[String]]
  ): Map[String, Int] = {
    val topics = queryString.getOrElse("topic", Seq.empty)
    topics.map { topicAndRate =>
      val Array(topic, digestRate) = topicAndRate.split(':')
      (topic, digestRate.toInt)
    }.toMap[String, Int]
  }

}