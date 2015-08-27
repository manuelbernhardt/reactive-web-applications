package actors

import actors.RandomNumberComputer.{RandomNumber, ComputeRandomNumber}
import akka.actor.{Props, Actor}
import scala.util.Random

class RandomNumberComputer extends Actor {
  def receive = {
    case ComputeRandomNumber(max) =>
      sender() ! RandomNumber(Random.nextInt(max))
  }
}

object RandomNumberComputer {
  def props = Props[RandomNumberComputer]
  case class ComputeRandomNumber(max: Int)
  case class RandomNumber(n: Int)
}
