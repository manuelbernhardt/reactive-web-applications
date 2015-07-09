package services

import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import org.specs2.specification.mutable.ExecutionEnvironment
import play.api.test.WithApplication
import play.modules.reactivemongo._

import scala.concurrent.duration._

class StatisticsServiceSpec() extends Specification with ExecutionEnvironment {

  def is(implicit ee: ExecutionEnv) = {
    "The StatisticsService" should {

      "compute and publish statistics" in new WithApplication() {
        val repository = new MongoStatisticsRepository(app.injector.instanceOf[DefaultReactiveMongoApi])
        val wsTwitterService = new WSTwitterService
        val service = new DefaultStatisticsService(repository, wsTwitterService)

        val f = service.createUserStatistics("elmanu")

        f must beEqualTo(Unit).await(retries = 0, timeout = 5.seconds)

      }
    }
  }

}
