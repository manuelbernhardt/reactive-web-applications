package actors

import actors.RandomNumberComputer.{RandomNumber, ComputeRandomNumber}
import akka.actor.{Props, Actor}
import scala.util.Random

class RandomNumberComputer extends Actor {
  def receive = {
    case ComputeRandomNumber =>
      sender() ! RandomNumber(Random.nextInt())
  }
}

object RandomNumberComputer {
  def props = Props[RandomNumberComputer]
  case object ComputeRandomNumber
  case class RandomNumber(n: Int)
}
