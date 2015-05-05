package controllers

import play.api._
import play.api.Play.current
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.db._
import org.jooq.util.mysql.MySQLDSL._
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import generated.Tables._
import generated.tables.records._

import scala.concurrent.Future


object Application extends Controller {

  def index = Authenticated { request =>
    Ok(views.html.index(request.firstName))
  }

  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors =>
        BadRequest(views.html.login(formWithErrors)),
      login =>
        DB.withConnection { c =>
          val context = DSL.using(c, SQLDialect.MYSQL)
          val users = context
            .selectFrom[UserRecord](USER)
            .where(USER.EMAIL.equal(login._1))
            .and(USER.PASSWORD.equal(password(login._2)))
            .fetch()

          if (users.size() > 0) {
            Redirect(routes.Application.index()).withSession(
              Authenticated.USER_ID -> users.get(0).getId.toString,
              Authenticated.FIRST_NAME -> users.get(0).getFirstname,
              Authenticated.LAST_NAME -> users.get(0).getLastname
            )
          } else {
            BadRequest(
              views.html.login(loginForm.withGlobalError("Wrong username or password"))
            )
          }
        }
    )
  }

  val loginForm = Form(
    tuple(
      "email" -> email,
      "password" -> text
    )
  )

}

case class AuthenticatedRequest[A](userId: Long, firstName: String, lastName: String)

object Authenticated extends ActionBuilder[AuthenticatedRequest] with Results {

  val USER_ID = "userid"
  val FIRST_NAME = "firstname"
  val LAST_NAME = "lastname"

  override def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]): Future[Result] = {
    val authenticated = for {
      id <- request.session.get(USER_ID)
      firstName <- request.session.get(FIRST_NAME)
      lastName <- request.session.get(LAST_NAME)
    } yield {
      AuthenticatedRequest[A](id.toLong, firstName, lastName)
    }

    authenticated.map { authenticatedRequest =>
      block(authenticatedRequest)
    } getOrElse {
      Future.successful {
        Redirect(routes.Application.login()).withNewSession
      }
    }
  }
}
