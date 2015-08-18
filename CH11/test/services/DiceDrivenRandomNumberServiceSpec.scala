package services

import org.scalatest.time.{Millis, Span}
import org.scalatest.{ShouldMatchers, FlatSpec}
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Future

class DiceDrivenRandomNumberServiceSpec
  extends FlatSpec
  with ScalaFutures
  with ShouldMatchers {

  "The DiceDrivenRandomNumberService" should
    "return a number provided by a dice" in {

  implicit val patienceConfig =
    PatienceConfig(
      timeout = scaled(Span(15, Millis)),
      interval = scaled(Span(15, Millis))
    )

    val diceService = new DiceService {
      override def throwDice: Future[Int] = Future.successful {
        4
      }
    }
    val randomNumberService =
      new DiceDrivenRandomNumberService(diceService)

    whenReady(randomNumberService.generateRandomNumber) { result =>
      result shouldBe(4)
    }

  }

}