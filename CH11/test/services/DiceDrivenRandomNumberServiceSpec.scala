package services

import java.util.concurrent.atomic.AtomicInteger

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
      override def throwDice: Future[Int] = Future.successful(4)
    }
    val randomNumberService =
      new DiceDrivenRandomNumberService(diceService)

    whenReady(randomNumberService.generateRandomNumber) { result =>
      result shouldBe(4)
    }

  }

  it should "be able to cope with problematic dice throws" in {
    val overzealousDiceThrowingService = new DiceService {
      val counter = new AtomicInteger()
      override def throwDice: Future[Int] = {
        val count = counter.incrementAndGet()
        if(count % 2 == 0) {
          Future.successful(4)
        } else {
          Future.failed(new RuntimeException(
            "Dice fell of the table and the cat won't give it back"
          ))
        }
      }
    }

    val randomNumberService =
      new DiceDrivenRandomNumberService(overzealousDiceThrowingService)

    whenReady(randomNumberService.generateRandomNumber) { result =>
      result shouldBe(4)
    }
  }

}