package recommendationsystem.controllers.manager

import play.api._
import play.api.mvc._
import reactivemongo.api._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._
import recommendationsystem.models._

class AdvicesController extends Controller {

  def list(page: Int) = Action.async {
    val pageLength = 50	
    val futureAdvices = Advices.all.sort(Json.obj("date" -> -1)).skip((page - 1) * pageLength).limit(pageLength).toList
    val futureCount = Advices.count
    for {
      advices <- futureAdvices
      count <- futureCount
    } yield {
      /** Only advice with input saved */
      Ok(recommendationsystem.views.html.manager.advices.list("Advices", "test", advices, count, page, pageLength))
    }
  }

}