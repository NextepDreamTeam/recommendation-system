package recommendationsystem.controllers.manager

//import recommendationsystem.controllers.manager.routes;
import play.api._
import play.api.mvc._
import reactivemongo.api._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Iteratee
import recommendationsystem.models._

class UsersController extends Controller {
  
  def list(page: Int) = Action.async {
    val pageLength = 50	
    val futureUsers = Users.all.skip((page - 1) * pageLength).limit(pageLength).toList
    val futureCount = Users.count
    for {
      users <- futureUsers
      count <- futureCount
    } yield {
      /** Only advice with input saved */
      Ok(recommendationsystem.views.html.manager.users.list("test", users, count, page, pageLength))
    }
  }
  
  def detail(id: String) = Action.async {
    val futureUser = Users.find(Json.obj("id" -> id)).one
    for{
      user <- futureUser
    } yield {
      user match {
        case Some(u) => Ok(recommendationsystem.views.html.manager.users.detail("", u))
        case None => Redirect(routes.UsersController.list(1))
      }
      
    }
  }

}