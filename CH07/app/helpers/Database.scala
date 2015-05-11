package helpers

import java.sql.Connection
import play.api.Play.current
import play.api.db.DB
import scala.concurrent.Future

object Database {

  def query[A](block: Connection => A): Future[A] = Future {
    DB.withConnection(block)
  }(Contexts.database)

}
