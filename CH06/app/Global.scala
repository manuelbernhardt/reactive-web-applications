import actors._
import akka.actor._
import play.api.libs.concurrent.Akka
import play.api._
import play.api.Play.current

object Global extends GlobalSettings {
  override def onStart(app: Application): Unit = {
    val providerReference: ActorRef =
      Akka.system.actorOf(Props[StatisticsProvider], name = "statisticsProvider")
  }
}
