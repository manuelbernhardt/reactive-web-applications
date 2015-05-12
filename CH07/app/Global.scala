import org.jooq.SQLDialect
import org.jooq.impl.DSL
import play.api.db.DB
import play.api.libs.Crypto
import play.api.{Application, GlobalSettings}
import generated.Tables._
import play.api.Play.current

object Global extends GlobalSettings {
  override def onStart(app: Application): Unit = {
    DB.withTransaction { tx =>
      val context = DSL.using(tx, SQLDialect.POSTGRES_9_4)
      if (context.fetchCount(USER) == 0) {
        context
          .insertInto(USER)
          .columns(USER.EMAIL, USER.FIRSTNAME, USER.LASTNAME, USER.PASSWORD)
          .values("bob@marley.org", "Bob", "Marley", Crypto.encryptAES("secret"))
          .execute()
      }

    }
  }
}
