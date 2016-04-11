package controllers

import javax.inject.Inject

import generated.Tables._
import generated.tables.records._
import helpers.Database
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import play.api.Play.current
import play.api.cache.Cache
import play.api.data.Forms._
import play.api.data._
import play.api.db._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.Crypto
import play.api.mvc._

import scala.concurrent.Future


class Application @Inject() (
  val crypto: Crypto,
  val db: Database,
  val messagesApi: MessagesApi
) extends Controller with I18nSupport {

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
        db.query { sql =>
          val user = Option(sql
            .selectFrom(USER)
            .where(USER.EMAIL.equal(login._1))
            .and(USER.PASSWORD.equal(crypto.sign(login._2)))
            .fetchOne())

          user.map { u =>
            Redirect(routes.Application.index()).withSession(
              USER.ID.getName -> u.getId.toString
            )
          } getOrElse {
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

  override def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]): Future[Result] = {
    val authenticated = for {
      id <- request.session.get(USER.ID.getName)
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
        val sql = DSL.using(connection, SQLDialect.POSTGRES_9_4)
        val user = Option(sql.selectFrom[UserRecord](USER).where(USER.ID.equal(id)).fetchOne())
        user.foreach { u =>
          Cache.set(u.getId.toString, u)
        }
        user
      }
    }
}