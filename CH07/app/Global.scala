import actors.SMSService
import akka.actor.Props
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import play.api.db.DB
import play.api.libs.Crypto
import play.api.libs.concurrent.Akka
import play.api.{Application, GlobalSettings}
import generated.Tables._
import play.api.Play.current

object Global extends GlobalSettings {
  override def onStart(app: Application): Unit = {
    Akka.system.actorOf(Props[SMSService], name = "sms")
    DB.withTransaction { connection =>
      val sql = DSL.using(connection, SQLDialect.POSTGRES_9_4)
      if (sql.fetchCount(USER) == 0) {
        sql
          .insertInto(USER)
          .columns(USER.EMAIL, USER.FIRSTNAME, USER.LASTNAME, USER.PASSWORD)
          .values("bob@marley.org", "Bob", "Marley", Crypto.encryptAES("secret"))
          .execute()
      }

    }
  }
}
