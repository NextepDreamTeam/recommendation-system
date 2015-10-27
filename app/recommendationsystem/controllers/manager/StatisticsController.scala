/*package recommendationsystem.controllers.manager

import play.api.libs.json
import play.api.libs.json._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import recommendationsystem.models._
import recommendationsystem.models.storage.AdvicesOdb
import scala.concurrent._
import ExecutionContext.Implicits.global

/**
 * Class that represents the controller that has the goal the handle all the statistics requests.
 */
class StatisticsController extends Controller{

  /**
   * Represents the form for a Range object.
   */
  val rangeForm: Form[Range] = Form (
    mapping(
      "startDate" -> longNumber(min = 0, max = System.currentTimeMillis()),
      "finishDate" -> longNumber(min = 0, max = System.currentTimeMillis())
    )(Range.apply)(Range.unapply)

  )

  /**
   * Method that show the page to select the statistic action.
   * @return an HTML page.
   */
  def index = Action {
    Ok(recommendationsystem.views.html.manager.statistics.options())
  }

  /**
   * Method that search the most used tag.
   * @return a Future[Option[(String, Int)]
   * If doesn't exists any tag in the db will be returned None.
   * Otherwise will be return a (String, Int), where the String represents the tag name and the Int the number of
   * occurrences of the tag.
   */
  private def calculateMax: Future[Option[(String, Int)]] = {
    //get all the Tags presents in the db.
    Tags.all flatMap { case tags =>
      val xs = tags map { case tag =>
        val tagName = tag.category  + ":" + tag.attr //create the String representation of the Tag.
      //Search all the document where tags.tag == tagName and for each document return a tuple (tag, number).
      val query =
        "select from Requests " +
          "where outE(\"RequestsHoldsTag\").inV(\"Tag\") IN " +
          "(select from Tags where tag in "+Json.toJson(tagName).toString()+" )"
        Requests.find(query).map (requests => (tagName, requests.size) )
      }
      val f = Future.sequence(xs)//transforms a List[Future[(String, Int)]] to Future[List[(String, Int)]]
      f.map { case ys =>
        //get the most used tag
        val res = ys.foldLeft(Option.empty[(String, Int)]){ (z, i) =>
          i match {
            case (name, number) if (number > i._2 ) => Some((name, number))
            case(n, nu) => Some(n, nu)
          }

        }
        res //return the result
      }

    }
  }


  /**
   * Method that search the most used tag.
   * The loading of the page can be slow.
   * @return an html page containing the result.
   */
  def maxHtml = Action.async {
    calculateMax.flatMap(result => Future{Ok(recommendationsystem.views.html.manager.statistics.max(result))})

  }

  /**
   * Method that search the most used tag in the db.
   * The method do the same job of the maxHtml method, but returns a Json as results.
   * @return a Future[Response] representing the result of the computation.
   * If doesn't exists any tag in the db will be return the empty Json({}).
   * Otherwise will be returned a json like: {"tag": "c1:a1", "occurrences": 2}, where:
   * tag : represent the tag name
   * occurrences : represent the number of times that the tag appears in the db.
   */
  def maxJson = Action.async {
    calculateMax.flatMap(result => result match {
      case Some(x) => Future{Ok(Json.obj("tag" -> x._1, "occurrences" -> x._2))}//exists a tag
      case None => Future{Ok(Json.obj())}//no tag exists
    })
  }

  /**
   * Method that find all the clicked advices.
   * @return A Future containing a List[Advice] object.
   */
  private def clickedAdvices: Future[List[Advice]] = {
    val query : String = "Select from Advices where clicked='true'"
    //val query = Json.obj("clicked" -> true)3
    AdvicesOdb.find(query)
  }

  /**
   * Method that display a view containing all the clicked advices.
   * @return a
   */
  def clickedAdvicesHtml = Action.async {
    clickedAdvices flatMap(results =>
      Future{Ok(recommendationsystem.views.html.manager.statistics.clickedadvises(results))}
      )
  }

  /**
   * Method that returns a json containing the clicked tags.
   * @return a Future[Response] containing the json result.
   */
  def clickedAdvicesJson = Action.async {
    clickedAdvices flatMap { results =>
      val jsonArray = for(el <- results) yield  Json.toJson(el)
      Future{Ok{Json.obj("elements" -> jsonArray)}}
    }
  }

  /**
   * Method the number and the average of Advices in some period.
   * @param range represents the period on which calculate the (average, number).
   * @return a Future of a Option[(Double, Int)] value.
   * If no clicked advices exists in the specified range will be return None.
   * Otherwise will be returned a Some(av, c), where:
   * av - represent the % of click in the range.
   * c - is the number of click in the range.
   */
  private def calculateStatisticsOnRange(range: Range): Future[(Double, Int)] = {
    val query : String = "select from Requests where date>="+range.startDate+" and date<="+range.finishDate
    /*
    val gte = Json.obj("date" -> Json.obj("$gte" -> range.startDate))//check all the dates that are >= startDate
    val lte = Json.obj("date" -> Json.obj("$lte" -> range.finishDate))//check all the dates that are <= finishDate
    val query = Json.obj("$and" -> Json.arr(gte, lte))//the query object in json
    */
    Requests.find(query).toList flatMap {results => //callback on the find on the db
      if(results.size > 0) { //at least one document match the query
      val checkCondition = Json.obj("clicked" -> true) //condition on the clicked advice
      val query1 = Json.obj("$and" -> Json.arr(gte, lte, checkCondition))
        Advices.find(query1).toList flatMap { elements => //finds all the Advices and set a callback on the query
          if(elements.size > 0) { //exists documents
          val percentage = elements.size.toDouble/ results.size
            val number = elements.size
            Future{(percentage, number)}//return the percentage and the number
          } else Future{(0.0, 0)} //no one match the query
        }
      } else Future{(0.0, 0)} //no one document match the query, return an empty result
    }
  }

  /**
   * Method that calculate the average and the number of advices clicked in a range period.
   * The data are passed in a json like: {"startDate": xxxxxxxxx, "finishDate": yyyyyyyyy}.
   * The method return the data in a json like: {"average": 3.2, "occurrences": 4}
   * @return a Future[Response] containing the json.
   */
  def calculateGoodAdvicesJson = Action.async{request =>
    implicit val restFormatter = recommendationsystem.formatters.json.RangeFormatters.readerFormatter
    val jsonData = request.body.asJson//get json value
    jsonData match {
      case Some(x) => x.validate(restFormatter) match {
        case JsSuccess(input, _) => calculateStatisticsOnRange(input) flatMap{result =>
          Future{Ok(Json.obj("average" -> result._1, "occurrences" -> result._2))}
        }
        case e: JsError => Future{BadRequest("Json bad formed")}
      }
      case None => Future{BadRequest("Need a json value")}
    }
  }

  /**
   * Method that display the form for select a range for the the search.
   * @return an html view that display the form.
   */
   def getCalculateStatisticsOnRangeForm = Action {
     Ok(recommendationsystem.views.html.manager.statistics.forms.rangeform(rangeForm)(recommendationsystem.controllers.manager.routes.StatisticsController.calculateGoodAdvicesHtml()))
   }

  /**
   * Method that show the number and average of click in a Range.
   * @return an http response containing the status of the operation.
   */
  def calculateGoodAdvicesHtml = Action.async { implicit request =>

    rangeForm.bindFromRequest.fold(
      errors => Future{BadRequest(recommendationsystem.views.html.manager.statistics.forms.rangeform(errors)(recommendationsystem.controllers.manager.routes.StatisticsController.calculateGoodAdvicesHtml()))},
      range =>  {calculateStatisticsOnRange(range) flatMap {result => Future{Redirect(routes.StatisticsController.displayStatistics(result._1, result._2))}}}
    )

  }

  /**
   * Method that display a view containing the statistic of a computation.
   * @param average - represents the average to display
   * @param count - represents the count to display
   * @return a Result containing the HTML view.
   */
  def displayStatistics(average: Double, count:Int) = Action{
    Ok(recommendationsystem.views.html.manager.statistics.goodadvise(average, count)("Good Advise"))
  }



  /**
       * Method that returns all the tags associated to a user.
       * @return a json containing the response.
       * The json has the following form: {"tags": [{"tag": "tag1}, {"tag": "tag2"]}
       * It the user doesn't exists, or has not any tag, will be return the empty json object ({}).
       */
  def userTag = Action.async { request =>
       
       /**
       	  * Method that return all the tags associate with a user.
       	  * user - the user email.
       	  * @return a Future[Result]
       	  */
       	 def elaborate(user: String): Future[Result] = {
         //obtain all the users saved in the db.
    	 val allUser : Future[Option[User]] = Users.find(Json.obj("email" -> user)).one
    	 val futureComputation = allUser map {
    	   								(user: Option[User]) =>
    	   								  user match {
    	   								    case Some(x) => x.tags  match {
    	   								      case Some(userTags) => val tags =  userTags.map{tag: (Tag, Double, Long) => val t = tag._1; t.category + ":" + t.attr} //obtain all the Tag objects
    	   								      										val arrayTags = for(tag <- tags) yield{Json.obj("tag" -> tag)} //crete the array json
    	   								      										Ok(Json.obj("tags" -> arrayTags)) //return the json corresponding to the user.   								      						
    	   								      case None => Ok(Json.obj()) //return an empty json.
    	   								    }
    	   								    case None => Ok(Json.obj()) //return an empty json
    	   								  }
     
    	 }
    	 //return the Future computation JSON response.
    	 futureComputation
    	 
     }
       	 //get the json object
       	 val json = request.body.asJson
       	 //parse the json object
       	 json match {
       	   case Some(x) => val y = x \ "user"; // get the "user" key value.
       	   					y match {
				       	     case user: JsString => elaborate(x.as[String]) //elaborate the request.
				       	     case _ => Future{BadRequest}   
				       	   }
       	   case None => Future{BadRequest("Need a json")}
       	 }
       	 
 }

  /**
   * Method that search the most used tag in some collection in a Range.
   * @param range - the range on which search the documents (startDate, finishDate).
   * @return a Future[Option[List[(String, Int)]]] object.
   */
  private def calculateRequests(range: Range): Future[List[recommendationsystem.models.Request]] = {
    val gte = Json.obj("date" -> Json.obj("$gte" -> range.startDate))
    val lte = Json.obj("date" -> Json.obj("$lte" -> range.finishDate))
    val query = Json.obj("$and" -> Json.arr(gte, lte))
    Requests.find(query).toList flatMap {result => Future{result}
    }
  }

  /**
   * Method that search the most used tags (in a Request) in a range period.
   * The input data are passed in a json like the following: {"startDate": xxxxxxx, "finishDate": yyyyyy}
   * startDate - is an Int that express a Date in milliseconds
   * finishDate - is an Int that express a Date in milliseconds
   * @return a Json value containing the result of the operation.
   * BadRequest is returns if the Json is not present or is bad formed.
   * Otherwise will be return an ok response containing the result of the search.
   */
  def searchTopRequestedTagsJson = Action.async { request =>

      implicit val restFormatter = recommendationsystem.formatters.json.RangeFormatters.readerFormatter
      val jsonData = request.body.asJson
      jsonData match  {
        case Some(json) => json.validate(restFormatter) match  {
          case JsSuccess(input, _) => calculateRequests(input) flatMap {result =>
           val jsonArray = for(el <- result) yield Json.toJson(el)
              Future{Ok(Json.obj("tags" -> jsonArray))}
          }
          case e: JsError => Future{BadRequest("Json bad formed")}
        }
        case None => Future{BadRequest("Need a json value")}
      }

  }


  /**
   * Method that display the form for show the requests on a range.
   * @return a Result containing the HTML page.
   */
  def showRequestForm = Action {
    Ok(recommendationsystem.views.html.manager.statistics.forms.rangeform(rangeForm)(recommendationsystem.controllers.manager.routes.StatisticsController.searchTopRequestTagsValidateForm()))

  }

  /**
   * Method that check if the form is completed correctly.
   * @return a Future[Result] containing the html page.
   */
  def searchTopRequestTagsValidateForm = Action {implicit request =>
    rangeForm.bindFromRequest().fold(
      errors => BadRequest(recommendationsystem.views.html.manager.statistics.forms.rangeform(errors)(recommendationsystem.controllers.manager.routes.StatisticsController.showRequestForm())),
      success => Redirect(recommendationsystem.controllers.manager.routes.StatisticsController.displayTopRequestResult(success.startDate, success.finishDate))
    )
  }

  /**
   * Method that display the top request clicked finds.
   * @param start - the start date (in milliseconds).
   * @param finish - the finish date (in milliseconds).
   * @return a Future[Result] containing the HTML page.
   */
  def displayTopRequestResult(start: Long, finish: Long) = Action.async {
    val range = Range(start, finish)
    calculateRequests(range) map {list =>
      Ok(recommendationsystem.views.html.manager.statistics.requestedtag(list))
    }
  }

  /**
   * Method that calculate the top advices in a range clicked
   * @param range - the range on which search the tags.
   * @return
   */
  private def calculateTopAdvicesTagsInRange(range: Range) : Future[List[(String, Int)]] = {

    //calculate the number of occurrencies of a tag in the Advice collection.
    def findTag(tag: Tag): Future[(String, Int)] = {
      val tagName = tag.category + ":" + tag.attr
      val tagCondition = Json.obj("tags.tag" -> tagName)
      val gte = Json.obj("date" -> Json.obj("$gte" -> range.startDate))
      val lte = Json.obj("date" -> Json.obj("$lte" -> range.finishDate))
      val query = Json.obj("$and" -> Json.arr(tagCondition, gte, lte))
      Requests.find(query).toList flatMap { results =>
        if(results.size > 0)
          Future{(tagName, results.size)}
        else
          Future{(tagName, 0)}
      }
    }

    val futureTags = Tags.all.toList flatMap {tags =>
      Future.sequence(for(tag <- tags) yield findTag(tag))
    } flatMap {result => Future{result}}

      futureTags map {tags =>
        val newList = tags.filter(_._2 > 0) //only the tag that compare at least one times
        newList
      }

    }

  /**
   * Method that search the most used tags in a range.
   * @return a Future[Result]
   */
  def topAdvicesJson = Action.async {request =>

    implicit val restFormatter = recommendationsystem.formatters.json.RangeFormatters.readerFormatter
    val jsonData = request.body.asJson
    jsonData match  {
      case Some(json) => json.validate(restFormatter) match  {
        case JsSuccess(input, _) => calculateTopAdvicesTagsInRange(input) flatMap {result =>
          val jsonArray = for(el <- result) yield Json.obj("tag" -> el._1, "number" -> el._2)
          Future{Ok(Json.obj("tags" -> jsonArray))}
        }
        case e: JsError => Future{BadRequest("Json bad formed")}
      }
      case None => Future{BadRequest("Need a json value")}
    }


  }

  /**
   * Method that display the form for select the range for the top advices.
   * @return a Result object (the html page).
   */
  def showTopAdvicesForm = Action {
    Ok(recommendationsystem.views.html.manager.statistics.forms.rangeform(rangeForm)(recommendationsystem.controllers.manager.routes.StatisticsController.showTopAdvicesValidateForm()))
  }

  /**
   * Method that validate the form.
   * @return a Result.
   */
  def showTopAdvicesValidateForm = Action { implicit request =>

    rangeForm.bindFromRequest().fold(
    hasErrors => BadRequest(recommendationsystem.views.html.manager.statistics.forms.rangeform(rangeForm)(recommendationsystem.controllers.manager.routes.StatisticsController.showTopAdvicesValidateForm())),
    range => Redirect(recommendationsystem.controllers.manager.routes.StatisticsController.displayTopAdvicesResults(range.startDate, range.finishDate))
    )

  }

  def displayTopAdvicesResults(start: Long, finish: Long) = Action.async {
    val range = Range(start, finish)
    calculateTopAdvicesTagsInRange(range) flatMap {results => Future{Ok(recommendationsystem.views.html.manager.statistics.topadvices(results))}

    }

  }

}
*/