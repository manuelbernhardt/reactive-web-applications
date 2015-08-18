package services

import scala.concurrent.Future

trait RandomNumberService {
  def generateRandomNumber: Future[Int]
}

class DiceDrivenRandomNumberService(dice: DiceService)
  extends RandomNumberService {
  override def generateRandomNumber: Future[Int] = dice.throwDice
}