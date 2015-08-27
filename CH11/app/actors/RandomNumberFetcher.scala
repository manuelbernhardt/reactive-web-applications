package actors

import actors.RandomNumberFetcher.{RandomNumber, FetchRandomNumber}
import akka.actor.{Props, Actor}
import play.api.libs.json.{JsArray, Json}
import play.api.libs.ws.WSClient
import scala.concurrent.Future
import akka.pattern.pipe

class RandomNumberFetcher(ws: WSClient) extends Actor {
  implicit val ec = context.dispatcher

  def receive = {
    case FetchRandomNumber(max) =>
      fetchRandomNumber(max).map(RandomNumber) pipeTo sender()
  }


  def fetchRandomNumber(max: Int): Future[Int] =
    ws
      .url("https://api.random.org/json-rpc/1/invoke")
      .post(Json.obj(
        "jsonrpc" -> "2.0",
        "method" -> "generateIntegers",
        "params" -> Json.obj(
          "apiKey" -> "<your-key-here>",
          "n" -> 1,
          "min" -> 0,
          "max" -> max,
          "replacement" -> true,
          "base" -> 10
        ),
        "id" -> 42
      )).map { response =>
        (response.json \ "result" \ "random" \ "data").as[JsArray].value.head.as[Int]
      }
}

object RandomNumberFetcher {
  def props(ws: WSClient) = Props(classOf[RandomNumberFetcher], ws)
  case class FetchRandomNumber(max: Int)
  case class RandomNumber(n: Int)
}
