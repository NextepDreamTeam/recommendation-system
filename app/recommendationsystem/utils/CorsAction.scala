/*package recommendationsystem.utils

import play.api.mvc._
import play.api.http.HeaderNames._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import play.api.Logger


/**
 * User: Alessandro Ferlin
 * Date: 30-01-2014
 */


object CorsAction extends ActionBuilder[Request] {

  def withCors[A](action: Action[A]) = Action.async(action.parser) { request =>
    val origin = request.headers.get(ORIGIN).getOrElse("*")
    Logger.debug("Adding CORS header on response")
    action(request).map(
      _.withHeaders(ACCESS_CONTROL_ALLOW_ORIGIN -> origin,
        ACCESS_CONTROL_ALLOW_METHODS -> "GET, PUT, POST, DELETE, OPTIONS",
        ACCESS_CONTROL_ALLOW_HEADERS -> "Origin, Content-Type, X-Requested-With, Accept,  X-Csrftoken",
        ACCESS_CONTROL_ALLOW_CREDENTIALS -> "true"))
  }
  
  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[SimpleResult]) = {
    block(request)
  }
  
  override def composeAction[A](action: Action[A]) = this.withCors(action)
}
*/