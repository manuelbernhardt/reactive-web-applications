package controllers

import javax.inject._
import play.api._
import play.api.mvc._

class Application @Inject() (configuration: Configuration) extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.index("Hello"))
  }

  def text = Action {
    Ok(configuration.getString("text").getOrElse("Hello world"))
  }


}
