package helpers

import org.jooq.{DSLContext, SQLDialect}
import org.jooq.impl.DSL
import play.api.Play.current
import play.api.db.DB
import scala.concurrent.Future

object Database {

  def query[A](block: DSLContext => A): Future[A] = Future {
    DB.withConnection { c =>
      val context = DSL.using(c, SQLDialect.POSTGRES_9_4)
      block(context)
    }
  }(Contexts.database)

  def withTransaction[A](block: DSLContext => A): Future[A] = Future {
    DB.withTransaction { tx =>
      val context = DSL.using(tx, SQLDialect.POSTGRES_9_4)
      block(context)
    }
  }(Contexts.database)

}
