package services

import scala.concurrent.Future

trait DiceService {
  def throwDice: Future[Int]
}
class RollingDiceService extends DiceService {
  override def throwDice: Future[Int] =
    Future.successful {
      4 // chosen by fair dice roll.
        // guaranteed to be random.
    }
}