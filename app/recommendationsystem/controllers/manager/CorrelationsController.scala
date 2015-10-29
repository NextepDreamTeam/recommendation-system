package recommendationsystem.controllers.manager

import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import recommendationsystem.models._

/**
 * Class that represents the controller for the view of the Correlations data.
 * @author Alberto Adami
 */
class CorrelationsController extends Controller {
  /**
   * Method that show a page of the Correlations value.
   * @param page - the page to show on the browser.
   * @return a web page containing the date of the page.
   */
  def list(page: Int) = Action.async {
    val pageLength = 50
    val futureCorrelations = Correlations.all
      //.skip((page - 1) * pageLength).limit(pageLength).toList
    val futureCount = Correlations.count
    for{
      correlations <- futureCorrelations
      count <- futureCount
    } yield {
      Ok(recommendationsystem.views.html.manager.correlations.list("test", correlations, count.toInt, page, pageLength))
    }
  }

  }
