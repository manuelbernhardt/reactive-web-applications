package controllers

import play.api._
import play.api.Play.current
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.db._
import org.jooq.util.mysql.MySQLDSL._
import org.jooq._
import org.jooq.impl.DSL
import generated.Tables._
import generated.tables.records._


object Application extends Controller {

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
            Ok(s"Hello ${users.get(0).getFirstname}")
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
