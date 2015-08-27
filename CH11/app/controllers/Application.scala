package controllers

import javax.inject.Inject

import actors.RandomNumberFetcher
import actors.RandomNumberFetcher.{RandomNumber, FetchRandomNumber}
import akka.actor.ActorSystem
import akka.util.Timeout
import play.api._
import play.api.libs.ws.WSClient
import play.api.mvc._
import akka.pattern.ask

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

class Application @Inject() (ws: WSClient, system: ActorSystem, ec: ExecutionContext) extends Controller {

  implicit val executionContext = ec
  implicit val timeout = Timeout(2000.millis)

  val fetcher = system.actorOf(RandomNumberFetcher.props(ws))

  def index = Action { implicit request =>
    Ok(views.html.index())
  }

  def compute = Action.async { implicit request =>
    (fetcher ? FetchRandomNumber(10)).map {
      case RandomNumber(r) =>
        Redirect(routes.Application.index())
          .flashing("result" -> s"The result is $r")
      case other =>
        InternalServerError
    }
  }

}
