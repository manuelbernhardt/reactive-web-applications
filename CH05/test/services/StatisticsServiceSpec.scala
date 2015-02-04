package services

import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import play.api.test.WithApplication
import scala.concurrent.duration._

class StatisticsServiceSpec extends Specification with NoTimeConversions{

  "The StatisticsService" should {

    "compute and publish statistics" in new WithApplication() {
      val service = new DefaultStatisticsService(new MongoStatisticsRepository, new WSTwitterService)

      val f = service.createUserStatistics("elmanu")

      f must haveClass[Unit].await(retries = 0, timeout = 5.seconds)

    }
  }

}
