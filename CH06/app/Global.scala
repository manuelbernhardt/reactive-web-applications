import actors._
import akka.actor._
import play.api.libs.concurrent.Akka
import play.api._
import play.api.Play.current

object Global extends GlobalSettings {

  override def onStart(app: Application): Unit = {
    val providerReference: ActorRef =
      Akka.system.actorOf(
        props = StatisticsProvider.props.withDispatcher("control-aware-dispatcher"),
        name = "statisticsProvider"
      )
  }
}
