package services

import scala.concurrent.{ExecutionContext, Future}

trait AuthenticationService {

  def authenticateUser(email: String, password: String)(implicit ec: ExecutionContext): Future[AuthenticationResult]

}

sealed trait AuthenticationResult
case object AuthenticationSuccessful extends AuthenticationResult
case object AuthenticationUnsuccessful extends AuthenticationResult

/**
 * Dummy implementation aimed at demonstrating the testing of Futures
 */
class DummyAuthenticationService extends AuthenticationService {

  override def authenticateUser(email: String, password: String)(implicit ec: ExecutionContext): Future[AuthenticationResult] = Future {
    email match {
      case "bob@marley.org" =>
        // never do this in reality. This is for testing purposes only!
        Thread.sleep(200)
        AuthenticationSuccessful
      case "jimmy@hendrix.com" =>
        // never do this in reality. This is for testing purposes only!
        Thread.sleep(500)
        throw new RuntimeException("Future timed out after 500ms")
      case _ =>
        AuthenticationUnsuccessful
    }
  }
}
