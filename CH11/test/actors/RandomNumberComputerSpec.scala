package actors

import akka.actor.ActorSystem
import akka.testkit._
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import org.scalatest._
import actors.RandomNumberComputer._

class RandomNumberComputerSpec(_system: ActorSystem)
  extends TestKit(_system)
  with ImplicitSender
  with FlatSpecLike
  with ShouldMatchers
  with BeforeAndAfterAll {

  def this() = this(
    ActorSystem(
      "RandomNumberComputerSpec",
      ConfigFactory.parseString(
        s"akka.test.timefactor=" +
          sys.props.getOrElse("SCALING_FACTOR", default = "1.0")
      )
    )
  )

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A RandomNumberComputerSpec" should "send back a random number" in {
    val randomNumberComputer = system.actorOf(RandomNumberComputer.props)
    within(100.milliseconds.dilated) {
      randomNumberComputer ! ComputeRandomNumber
      expectMsgType[RandomNumber]
    }
  }
}
