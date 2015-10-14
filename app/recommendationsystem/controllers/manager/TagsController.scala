/*package recommendationsystem.controllers.manager

import play.api._
import play.api.mvc._
import reactivemongo.api._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._
import recommendationsystem.models._

class TagsController extends Controller{

  def list(page: Int) = Action.async {
    val pageLength = 50	
    val futureTags = Tags.all.skip((page - 1) * pageLength).limit(pageLength).toList
    val futureCount = Tags.count
    for {
      tags <- futureTags
      count <- futureCount
    } yield {
      /** Only advice with input saved */
      Ok(recommendationsystem.views.html.manager.tags.list("test", tags, count, page, pageLength))
    }
  }
  
}
*/