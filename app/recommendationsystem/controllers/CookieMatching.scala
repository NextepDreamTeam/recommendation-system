package recommendationsystem.controllers

import play.api._
import play.api.Play.current
import play.api.mvc._
import play.api.cache.Cache
import scala.concurrent.duration._


object MatchingCookie {
  def apply(key: String)(orElse: => String) = {
    Cache.getOrElse(key){orElse}
  }
}


object CookieMatching extends Controller {

  //controllare redirectTo
  def synchCookie(cookie: String, redirectTo: String) = Action { request =>
    val dreUserId = request.session.get("userId").getOrElse(java.util.UUID.randomUUID.toString)
    Cache.set(cookie, dreUserId, 365 days)
    Redirect(redirectTo).withSession("userId" -> dreUserId)
  }

}
