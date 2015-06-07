package helpers

import org.jooq.{DSLContext, SQLDialect}
import org.jooq.impl.DSL
import play.api.Play.current
import play.api.db.DB
import scala.concurrent.Future

object Database {

  def query[A](block: DSLContext => A): Future[A] = Future {
    DB.withConnection { connection =>
      val sql = DSL.using(connection, SQLDialect.POSTGRES_9_4)
      block(sql)
    }
  }(Contexts.database)

  def withTransaction[A](block: DSLContext => A): Future[A] = Future {
    DB.withTransaction { connection =>
      val sql = DSL.using(connection, SQLDialect.POSTGRES_9_4)
      block(sql)
    }
  }(Contexts.database)

}
