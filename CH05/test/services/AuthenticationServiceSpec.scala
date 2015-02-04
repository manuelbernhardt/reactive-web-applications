package services

import org.specs2.matcher.Matchers
import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import scala.concurrent.duration._

class AuthenticationServiceSpec extends Specification with Matchers with NoTimeConversions {

  "The AuthenticationService" should {

    val service: AuthenticationService = new DummyAuthenticationService

    "correctly authenticate Bob Marley" in {
      service.authenticateUser("bob@marley.org", "secret") must beEqualTo (AuthenticationSuccessful).await(1, 200.millis)
    }

    "not authenticate Ziggy Marley" in {
      service.authenticateUser("ziggy@marley.org", "secret") must beEqualTo (AuthenticationUnsuccessful).await(1, 200.millis)
    }

    "fail if it takes too long" in {
      service.authenticateUser("jimmy@hendrix.com", "secret") must throwA[RuntimeException].await(1, 600.millis)
    }

  }

}
