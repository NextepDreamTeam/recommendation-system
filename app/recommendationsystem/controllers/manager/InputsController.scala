package recommendationsystem.controllers.manager

import play.api.mvc._
//import reactivemongo.api._
import play.api.libs.concurrent.Execution.Implicits._
import recommendationsystem.models._

class InputsController extends Controller{
  
  def list(page: Int) = Action.async {
    val pageLength = 50	
    val futureRequests = Requests.all
      //.sort(Json.obj("date" -> -1)).skip((page - 1) * pageLength).limit(pageLength).toList
    val futureCount = Requests.count
    for {
      requests <- futureRequests
      count <- futureCount
    } yield {
      /** Only advice with input saved */
      Ok(recommendationsystem.views.html.manager.inputs.list("test", requests, count.toInt, page, pageLength))
    }
  }

}
