package actors

import akka.actor.SupervisorStrategy.Restart
import akka.actor._
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
      randomNumberComputer ! ComputeRandomNumber(100)
      expectMsgType[RandomNumber]
    }
  }

  it should "fail when the maximum is a negative number" in {

    class StepParent(target: ActorRef) extends Actor {
      override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
        case t: Throwable =>
          target ! t
          Restart
      }
      def receive = {
        case props: Props =>
          sender ! context.actorOf(props)
      }
    }

    val parent = system.actorOf(Props(new StepParent(testActor)), name = "stepParent")
    parent ! RandomNumberComputer.props
    val actorUnderTest = expectMsgType[ActorRef]
    actorUnderTest ! ComputeRandomNumber(-1)
    expectMsgType[IllegalArgumentException]
  }
}
