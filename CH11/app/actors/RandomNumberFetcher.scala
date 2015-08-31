package actors

import actors.RandomNumberFetcher.{RandomNumber, FetchRandomNumber}
import akka.actor.{Props, Actor}
import play.api.libs.json.{JsResultException, JsArray, Json}
import play.api.libs.ws.WSClient
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.pattern.{CircuitBreakerOpenException, CircuitBreaker, pipe}

import scala.util.Random
import scala.util.control.NonFatal

class RandomNumberFetcher(ws: WSClient) extends Actor {
  implicit val ec = context.dispatcher

  val breaker = CircuitBreaker(
    scheduler = context.system.scheduler,
    maxFailures = 5,
    callTimeout = 3.seconds,
    resetTimeout = 30.seconds
  )

  def receive = {
    case FetchRandomNumber(max) =>
      val safeCall = breaker.withCircuitBreaker(
        fetchRandomNumber(max).map(RandomNumber)
      )
      safeCall recover {
        case js: JsResultException => computeRandomNumber(max)
        case o: CircuitBreakerOpenException => computeRandomNumber(max)
      } pipeTo sender()
  }

  def computeRandomNumber(max: Int) = RandomNumber(Random.nextInt(max))

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
