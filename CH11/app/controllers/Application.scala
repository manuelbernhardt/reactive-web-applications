package controllers

import javax.inject.Inject

import actors.RandomNumberFetcher
import actors.RandomNumberFetcher.{FetchRandomNumber, RandomNumber}
import akka.actor.ActorSystem
import akka.pattern.{AskTimeoutException, ask}
import akka.util.Timeout
import play.api.libs.ws.WSClient
import play.api.mvc._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._


class Application @Inject() (ws: WSClient,
                             ec: ExecutionContext,
                             system: ActorSystem) extends Controller {

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
    } recover {
      case to: AskTimeoutException =>
        Ok("Sorry, we are overloaded")
    }
  }

}