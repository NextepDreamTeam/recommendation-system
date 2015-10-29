package recommendationsystem.controllers.manager

import play.api.mvc._
import recommendationsystem.models.storage.UsersOdb

//import reactivemongo.api._
import play.api.libs.concurrent.Execution.Implicits._
import recommendationsystem.models._

class UsersController extends Controller {
  
  def list(page: Int) = Action.async {
    val pageLength = 50
    val futureUsers = Users.all
      //.skip((page - 1) * pageLength).limit(pageLength).toList
    val futureCount = UsersOdb.count
    for {
      users <- futureUsers
      count <- futureCount
    } yield {
      /** Only advice with input saved */
      Ok(recommendationsystem.views.html.manager.users.list("test", users, count.toInt, page, pageLength))
    }
  }

  def detail(id: String) = Action.async {
    val futureUser = UsersOdb.find("Select from Users where uid="+id)
      //.find(Json.obj("id" -> id)).one
    for{
      user <- futureUser
    } yield {
      user match {
        case u :: xs => Ok(recommendationsystem.views.html.manager.users.detail("", u))
        case Nil => Redirect(routes.UsersController.list(1))
      }
    }
  }
}
