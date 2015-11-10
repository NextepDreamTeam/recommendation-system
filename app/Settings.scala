import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.http.Status
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._
import play.libs.Akka
import scala.concurrent.duration._
import scala.concurrent.Future
import java.lang.reflect.Constructor
import recommendationsystem.models.Error

//import recommendationsystem.algorithms.Pearson

import recommendationsystem.formatters.json.ErrorFormatters.generalFormatter

object Settings {


  def onStart(app: Application) {
    Logger.info("Application has started")

    val nowH = ("%1$tH".format(System.currentTimeMillis)).toInt
    val howMuchToRuns = (24 - nowH + 3)

    //Akka.system.scheduler.schedule(0 hours, 24 hours)(Pearson.calculate)
  }

  /**
   * When an exception occurs in your application, the onError operation 
   * will be called. The default is to use the internal framework error page:
   */
  def onError(request: RequestHeader, ex: Throwable) = {
    Future.successful {
      InternalServerError(Json.toJson(
        Error(
          status = Status.INTERNAL_SERVER_ERROR,
          message = "Internal server error",
          developerMessage = ex.getMessage())))
    }
  }

  /**
   * The onBadRequest operation will be called if a route was found, 
   * but it was not possible to bind the request parameters:
   */
  def onBadRequest(request: RequestHeader, error: String) = {
    Future.successful {
      BadRequest(Json.toJson(
        Error(status = Status.BAD_REQUEST, message = error)))
    }
  }
  
  /**
   * If the framework doesn���������t find an Action for a request, the 
   * onHandlerNotFound operation will be called:
   */
  def onHandlerNotFound(request: RequestHeader) = {
    Future.successful(NotFound(
      Json.toJson(
        Error(status = Status.NOT_FOUND, message = "Not Found"))
    ))
  }

}
