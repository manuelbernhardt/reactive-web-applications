import play.api._
import play.api.mvc._
import play.api.libs.json._

import models._

object Application extends Controller {

  def updateFirstName(userId: Long) = Action { implicit request =>
    val update: Option[Result] = for {
      json <- request.body.asJson
      user <- User.findOneById(userId)
      newFirstName <- (json \ "firstName").asOpt[String]
      if !newFirstName.trim.isEmpty
    } yield {
      User.updateFirstName(user.id, newFirstName)
      Ok
    }

    update.getOrElse {
      BadRequest(Json.obj("error" -> "Could not update your first name, please make sure that it is not empty"))
    }
  }
}