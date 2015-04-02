import filters.ScoreFilter
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.filters.gzip.GzipFilter
import play.filters.headers.SecurityHeadersFilter
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

object Global extends WithFilters(new GzipFilter(), SecurityHeadersFilter(), new ScoreFilter) {

  override def onHandlerNotFound(request: RequestHeader) = {
    Future {
      NotFound("Could not find " + request)
    }
  }

  override def onBadRequest(request: RequestHeader, error: String): Future[Result] = {
    super.onBadRequest(request, error)
  }

  override def onError(rh: RequestHeader, ex: Throwable): Future[Result] = {
    Logger.error(s"Error while processing request to ${rh.uri}", ex)
    super.onError(rh, ex)
  }

}