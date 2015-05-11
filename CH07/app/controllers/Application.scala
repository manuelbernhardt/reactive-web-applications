package controllers

import helpers.Database
import play.api._
import play.api.Play.current
import play.api.cache.Cache
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
    Ok(views.html.index(request.user.getFirstname))
  }

  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  def logout = Action { implicit request =>
    Redirect(routes.Application.index()).withNewSession
  }

  def authenticate = Action.async { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors =>
        Future.successful {
          BadRequest(views.html.login(formWithErrors))
        },
      login =>
        Database.query { c =>
          val context = DSL.using(c, SQLDialect.MYSQL)
          val users = context
            .selectFrom[UserRecord](USER)
            .where(USER.EMAIL.equal(login._1))
            .and(USER.PASSWORD.equal(password(login._2)))
            .fetch()

          if (users.size() > 0) {
            Redirect(routes.Application.index()).withSession(
              Authenticated.USER_ID -> users.get(0).getId.toString
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

case class AuthenticatedRequest[A](userId: Long, user: UserRecord)

object Authenticated extends ActionBuilder[AuthenticatedRequest] with Results {

  val USER_ID = "userid"

  override def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]): Future[Result] = {
    val authenticated = for {
      id <- request.session.get(USER_ID)
      user <- fetchUser(id.toLong)
    } yield {
      AuthenticatedRequest[A](id.toLong, user)
    }

    authenticated.map { authenticatedRequest =>
      block(authenticatedRequest)
    } getOrElse {
      Future.successful {
        Redirect(routes.Application.login()).withNewSession
      }
    }
  }

  def fetchUser(id: Long): Option[UserRecord] =
    Cache.getAs[UserRecord](id.toString).map { user =>
      Some(user)
    } getOrElse {
      DB.withConnection { connection =>
        val context = DSL.using(connection, SQLDialect.MYSQL)
        val user = Option(context.selectFrom[UserRecord](USER).where(USER.ID.equal(id)).fetchOne())
        user.map { u =>
          Cache.set(u.getId.toString, u)
        }
        user
      }
    }
}
