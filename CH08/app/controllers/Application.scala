package controllers

import java.sql.Timestamp

import akka.actor.{Actor, ActorRef, Props}
import dashboard.GraphType
import generated.Tables._
import org.joda.time.format.DateTimeFormat
import org.joda.time.LocalDate
import org.jooq.impl.DSL._
import org.jooq.types.YearToMonth
import org.jooq.{Converter, DatePart, SQLDialect}
import play.api.Play.current
import play.api.db._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def graphs = WebSocket.acceptWithActor[String, JsValue] {
    request => out => DashboardClient.props(out)
  }

}

class DashboardClient(out: ActorRef) extends Actor {
  def graphType(s: String) = GraphType.withName(s)
  def receive = {
    case t: String => graphType(t) match {
      case GraphType.MonthlySubscriptions =>
        val mentionsCount = DB.withConnection { connection =>
          val sql = using(connection, SQLDialect.POSTGRES_9_4)


          // TODO do everything in the query once jOOQ supports timestamps in generate_series
          /*
            SELECT *
            FROM (SELECT generate_series(now() - '1 month'::interval, now(), '1 day'::interval)::date) AS d(day)
            LEFT JOIN (
               SELECT date_trunc('day', created_on)::date AS day, count(*) AS mention_count
               FROM   mentions
               WHERE  created_on > now() - interval '1 month'
               GROUP  BY 1
               ) t USING (day)
            ORDER  BY 1;
          */

          sql.select(trunc(MENTIONS.CREATED_ON, DatePart.DAY).as("day"), count())
             .from(MENTIONS)
             .where(MENTIONS.CREATED_ON.greaterThan(currentTimestamp().sub(new YearToMonth(0, 1))))
             .groupBy(field("day"))
             .orderBy(field("day"))
             .fetch()
        }

        val allDates = (1 to 30).map { day =>
          LocalDate.now.minusDays(day)
        }

        import scala.collection.JavaConverters._

        val counts: Map[LocalDate, Int] = mentionsCount.iterator().asScala.map { record =>
          record.getValue(0, new LocalDateConverter) -> record.getValue(1, classOf[Int])
        }.toMap

        val monthlyCounts: Map[String, Int] = allDates.map { day =>
          DateTimeFormat.forPattern("dd/MM").print(day) -> counts.get(day).getOrElse(0)
        }.sortBy(_._1).toMap


        out ! Json.obj(
          "graph_type" -> GraphType.MonthlySubscriptions,
          "labels" -> Json.toJson(monthlyCounts.keys),
          "series" -> Json.arr("Subscriptions"),
          "data" -> Json.arr(Json.toJson(monthlyCounts.values))
        )
    }
  }
}
object DashboardClient {
  def props(out: ActorRef) = Props(classOf[DashboardClient], out)
}

class LocalDateConverter extends Converter[Timestamp, LocalDate] {
  override def from(t: Timestamp): LocalDate = new LocalDate(t)
  override def to(u: LocalDate): Timestamp = new Timestamp(u.toDateTimeAtStartOfDay.getMillis)
  override def fromType(): Class[Timestamp] = classOf[Timestamp]
  override def toType: Class[LocalDate] = classOf[LocalDate]
}
