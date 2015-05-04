package controllers

import play.api._
import play.api.Play.current
import play.api.mvc._
import play.api.db._

import org.jooq._
import org.jooq.impl.DSL
import generated.Tables._
import generated.tables.records._


object Application extends Controller {

  def authenticate = Action { implicit request =>
    DB.withConnection { c =>
      val context = DSL.using(c, SQLDialect.MYSQL)
      val users = context.selectFrom[UserRecord](USER).fetch()
      Ok(users.toString)
    }
  }

}
