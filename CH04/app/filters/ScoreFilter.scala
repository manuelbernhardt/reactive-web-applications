package filters

import play.api.libs.iteratee.Enumerator
import play.api.mvc.{ Result, RequestHeader, Filter }
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

class ScoreFilter extends Filter {
  override def apply(
    nextFilter: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {

    val result = nextFilter(rh)
    result.map { res =>

      if (res.header.status == 200 || res.header.status == 406) {
        val correct = res.session(rh).get("correct").getOrElse(0)
        val wrong = res.session(rh).get("wrong").getOrElse(0)
        val score = s"\n\nYour current score is: $correct correct answers and $wrong wrong answers"
        val newBody = res.body andThen Enumerator(score.getBytes("UTF-8"))
        res.copy(body = newBody)
      } else {
        res
      }

    }
  }
}
