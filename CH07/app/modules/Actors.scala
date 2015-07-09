package modules

import javax.inject._

import actors.SMSService
import akka.actor.ActorSystem
import com.google.inject.AbstractModule
import helpers.Database

class Actors @Inject()(val system: ActorSystem, val database: Database) extends ApplicationActors {
  system.actorOf(SMSService.props(database), name = "sms")
}

trait ApplicationActors

class ActorsModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ApplicationActors]).to(classOf[Actors]).asEagerSingleton
  }
}

