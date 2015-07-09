package services

import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import org.specs2.specification.mutable.ExecutionEnvironment
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.WithApplication
import play.modules.reactivemongo._

import scala.concurrent.duration._

class StatisticsServiceSpec() extends Specification with ExecutionEnvironment {

  def is(implicit ee: ExecutionEnv) = {
    "The StatisticsService" should {

      "compute and publish statistics" in new WithApplication() {
        val repository = new MongoStatisticsRepository(configuredAppBuilder.injector.instanceOf[ReactiveMongoApi])
        val wsTwitterService = new WSTwitterService
        val service = new DefaultStatisticsService(repository, wsTwitterService)

        val f = service.createUserStatistics("elmanu")

        f must beEqualTo(()).await(retries = 0, timeout = 5.seconds)
      }

    }

    def configuredAppBuilder = {
      import scala.collection.JavaConversions.iterableAsScalaIterable

      val env = play.api.Environment.simple(mode = play.api.Mode.Test)
      val config = play.api.Configuration.load(env)
      val modules = config.getStringList("play.modules.enabled").fold(
        List.empty[String])(l => iterableAsScalaIterable(l).toList)

      new GuiceApplicationBuilder().
        configure("play.modules.enabled" -> (modules :+
          "play.modules.reactivemongo.ReactiveMongoModule")).build
    }
  }

}
