package services

import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification

import scala.concurrent.duration._

class AuthenticationServiceSpec extends Specification {

  "The AuthenticationService" should {

    val service: AuthenticationService = new DummyAuthenticationService

    "correctly authenticate Bob Marley" in { implicit ee: ExecutionEnv =>
      service.authenticateUser("bob@marley.org", "secret") must beEqualTo (AuthenticationSuccessful).await(1, 200.millis)
    }

    "not authenticate Ziggy Marley" in { implicit ee: ExecutionEnv =>
      service.authenticateUser("ziggy@marley.org", "secret") must beEqualTo (AuthenticationUnsuccessful).await(1, 200.millis)
    }

    "fail if it takes too long" in { implicit ee: ExecutionEnv =>
      service.authenticateUser("jimmy@hendrix.com", "secret") must throwA[RuntimeException].await(1, 600.millis)
    }

  }

}
