/*package recommendationsystem.controllers

import play.api._
import play.api.mvc._
import play.api.cache.Cache
import reactivemongo.api._
import reactivemongo.core.commands._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.json._
import play.api.Play.current
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.Iteratee
import recommendationsystem.models._
import recommendationsystem.utils.CorsAction
import recommendationsystem.formatters.json.{InputFormatters, OutputFormatters, UserFormatters}


import recommendationsystem.models.{Input, Output}


object Application extends Controller {

  /**
   *
   */
  def advise = CorsAction.async { request =>

    implicit val userRestFormat = UserFormatters.restFormatter
    implicit val inputFormat = InputFormatters.restFormatter
    implicit val outputWriter = OutputFormatters.restWriter

    def elaborate(json: JsValue) = {
      /** da attivare per la session CORS con chrome e firefox  */
      val userId = request.session.get("userId").getOrElse("")

      json.validate[Input] match {
        case JsSuccess(input, _) => {
          val inputUser = input.user.getOrElse(User(userId))
          val dreId = MatchingCookie(inputUser.id) { inputUser.id }
          val user = inputUser.copy(id = dreId)
        		 
          /** if (feedback) update recommendation to database */
          input.feedback.foreach { idR =>
            Advices.update(Json.obj("id" -> idR), Json.obj("clicked" -> true))
          }
          //** save new tags*/
          input.tags foreach {
            _.foreach(t => Tags.save(t, upsert = true))
          }

          //TODO: check if user save part is correct, think about uid and email
          /** save/update (upsert) user preferences */
          val futureUser = Users.find(Json.obj("id" -> user.id)).one
          //** set callback */
          futureUser.onSuccess {
            case Some(u) =>
              val updatedUser = input.tags.map { listTag =>
                user.merge(u.addTags(listTag))
              }.getOrElse(user.merge(u))
              user.email match {
                case Some(email) => Users.update(Json.obj("email" -> user.email.get), updatedUser.copy(id = u.id))
                case None => Users.update(Json.obj("id" -> user.id), updatedUser)
              }
            case None =>
              val updatedUser = input.tags.map { listTag =>
                user.addTags(listTag)
              }.getOrElse(user)
              /** se non l'ho trovato pu?? essere che sia la prima volta che mette la mail */
              updatedUser.email match {
                case Some(email) => Users.update(Json.obj("id" -> user.id), updatedUser, upsert = true)
                case None if user.id != "" => Users.save(updatedUser)
                case _ => ()
                /** questo per evitare di salvare un utente alla prima possibile richiesta PRIMA del redirect */
              }
          }

          /** create request */
          val req = recommendationsystem.models.Request(java.util.UUID.randomUUID.toString, user, input.tags, input.mandatoryTags, System.currentTimeMillis)
          /** save input request, devo sostituire lo user con quello trovato */
          futureUser.onSuccess {
            case Some(u) =>
              Requests.save(req.copy(user = u))
            case None =>
              Requests.save(req)
          }
          /** make advice */
          val futureAdvice: Future[Advice] = Recommender.advise(req)
          futureAdvice.map { advice =>
            /** save advice to database and save the input (Request) */
            Advices.save(advice)
            /** create output and serve */
            val output = Output(advice, Some(advice.user.copy(tags = None)), input.mandatoryTags)
            Ok(Json.toJson(output)) //.withSession("userId" -> userId)
          }
        }
        case e: JsError => Future { Ok(Json.obj("error" -> JsError.toFlatJson(e) /*, "request" -> json*/ )) }
      }
    }

    // Gestire il caso di json malformato con Json.parse(...)
    request.body match {
      case AnyContentAsJson(json) => elaborate(json)
      case AnyContentAsText(text) => elaborate(Json.parse(text))
      case AnyContentAsRaw(raw) => {
        raw.asBytes() match {
          case Some(bytes) => elaborate(Json.parse(new String(bytes)))
          case None => Future { Ok(Json.obj("Error" -> "no-body")) }
        }
      }
      case other => Future { Ok(Json.obj("Error" -> "Expected a application/json")) }
    }
  }

  /**
   * For OPTION call for CORS protocol
   */
  def checkPreAdvise = CorsAction { request =>
    Ok
  }
  /**
   * Method that returns the correlation data to be display in the bubbles-chart.
   * @return a Json value containing the values to display.
   * The json has the following form: {"elements": [{"category": "cat1", "attribute": "attr1", "value": {"average": 2, "weight": 5}}]}
   * If no one elements is saved in the db will be returns the following json: {"elements": []}
   */
  def correlation = CorsAction.async {
      //find all the correlation elements on the db
      //Correlations.save(new Correlation("carne", "piazza", 1, 2))
      Correlations.all.toList flatMap { elements: List[Correlation] =>
        val correlations = for(el <- elements) yield Json.obj("category" -> el.category,
          "attribute" -> el.attribute.split(":")(1),
          "value" -> Json.obj("average" -> el.average, "weight" -> el.weight)) //create the sequence
        Future{Ok(Json.obj("elements" -> correlations))} //return the json result
      }
  }
  /**
   * Method that returns a list of users who are associated with a given category, but haven't purchased any products of a given product.
   * The category and the product are passed in a json value like the following: {"category": "c1", "product": "c1:p1"}
   * @return a json data containing the user that match the query in a format like the following:
   * {users: [{"id": "usid1", "email": "email@example1.it"}, {"id": "usid2", "email: "email@example2.it"}]}
   */
  def suggestion = CorsAction.async { request =>

    def findUsers(input: FindSuggestion): Future[Option[List[User]]] = {
      val category = input.category 
      val product = input.product 
      val regexString = Json.obj("$regex" ->  JsString(category + ":.*")) //the regex condition
      val existsCategory = Json.obj("tags.tag" -> regexString) //exists category condition
      val notExistsProduct = Json.obj("tags.tag" -> Json.obj("$ne" -> product )) //the not exists already product condition
      val conditions = Seq(existsCategory, notExistsProduct)
      val query = Json.obj("$and" -> conditions) //the and for the two conditions
      Users.find(query).toList.flatMap(users =>
        if(users.nonEmpty)
          Future{Some(users)}
        else
          Future{None}
          )
    }
    
    implicit val readerFormatter = Json.reads[FindSuggestion]

    //get the json data
    val jsonData =  request.body.asJson
    jsonData match {
      case Some(x) => val validate = x.validate(readerFormatter)
    		  		  validate match {
        case JsSuccess(input, _) => findUsers(input).flatMap(result =>
          result match {
            case Some(users) => val usersJson = for(user <- users) yield Json.obj("id" -> user.id , "email" -> user.email)
            		Future{Ok(Json.obj("users" -> usersJson))}
            case None => Future{Ok(Json.obj())}
          	}
              )
       case e: JsError => Future{BadRequest("json bad formed")} //error on the validate of the json

          }
      case None => Future{BadRequest("Need json")}
    }
    
  }
    
}
*/