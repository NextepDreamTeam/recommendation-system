package recommendationsystem.controllers

import play.api._
import play.api.mvc._
import play.api.cache.Cache
import play.api.libs.ws._
import scala.concurrent.Future
import play.api.libs.json._
import play.api.Play.current
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.Iteratee
import recommendationsystem.models._
import recommendationsystem.utils.CorsAction

import recommendationsystem.formatters.json._

import play.Logger

import scala.util.{Failure, Success}

/**
 * Object that represents the controller that has the goal to insert the data on the mongodb instance.
 * It's used by the bdrim application for insert tag, user and update the user tags.
 * @author Alberto Adami
 */
object BridgeController extends Controller {
/*
  /**
   * Method that search all the user that match the given tags.
   * The list of tags are passed in json like the following: {"tags": [{"tag": "cat1:attri1:"}, {"tag": "cat2:attr1"}]}
   * tags - the tags to match with users.
   * @return an http response.
   * If the input json is correct, will be returned a list of user in the json format like the following: 
   * {users: [{"user": {"email": "email@email.it", "id": "userid1"}}, {"user": {"email": "email1@email.it", "id": "userid2"}}]}
   */
  def userMatchingTag = CorsAction.async { request =>
    //Get all the User that match the List of given Tags
    def findUsers(tags: List[String]): Future[Option[List[User]]] = {
      val jsonTags = for(tag <- tags) yield Json.obj("tags.tag" -> tag) //create the array of tags
      val query = Json.obj("$and" -> jsonTags)
      print(query)
      Users.find(query).toList flatMap { users =>
        Future{if(users.size > 0) Some(users) else None}
      }
    }
    //get the json value
    val jsonObject = request.body.asJson
    //the reader for a List[RestTag]
    val tagsRead = (__ \ "tags").read[List[RestTag]] 
    jsonObject match {
      case Some(x) => val validate = x.validate(tagsRead)//validate the json
    		  	validate match {
    		  		case JsSuccess(list, _) => val tagsList = for(el <- list) yield el.tag
    		  				findUsers(tagsList).flatMap(result => result match {//find if exists at least one user that match all the tags
    		  				  case Some(x) => val jsonUsers = for(user <- x) yield Json.obj("id" -> user.id , "email" -> user.email )//users array
    		  						  		  Future{Ok(Json.obj("users" -> jsonUsers))}//return the correct json
    		  				  case None => Future{Ok(Json.obj())} //no one user match all the given tags
    		  				}
    		  				)
    		  		case e: JsError => Future{BadRequest("json bad formed")} //error on the validate of the json
      		}
      case None => Future{BadRequest("Need json")} // no json object given
    }
  }
*/
  /**
   * Method that has the goal to add a new Tag on the "recommendation.tags"
   * The data are passed in a json value like the following: {"category": "c1", "attr": "a1"}.
   * category - the category of the tag (product category).
   * attr - the attribute of the tag (product name).
   * @return An http response that indicates the status of the operation.
   *         Ok - the insert was completed successful.
   *         BadRequest - the insert was not completed successful because the json was bad formed.
   *         InternalServerError - the insert was not completed successful because the Server cannot access to the MongoDB instance.
   */
  def addTag() = CorsAction.async { request =>

    def insertTagOnTheDB(tag: Tag): Future[Boolean] = {
      Tags.save(tag, upsert = true) onComplete {
        case Success(x) => Future {x}
        case Failure(t) => println("An error has occured: " + t.getMessage); Future {false}
      }
      Future{true}
    }

    //get the json data
    val jsonData = request.body.asJson
    jsonData match {
      case Some(x) => val category = x \ "category" get;
        val attr = x \ "attr" get; // get the tags values
        (category, attr) match {
          case (c: JsString, a: JsString) => {
            val tag = new Tag(c.as[String], a.as[String])
            //insert the tag on the recommendation.tags collection
            insertTagOnTheDB(tag).flatMap {
              status => status match {
                case true => Future {Ok} //insert ok
                case _ => Future {InternalServerError("Cannot acess to the db now")} //insert fail
              }
            }
          }
          case _ => Future {BadRequest("Json bad formed")} //the json is not in the correct form.
        }
      case None => Future {BadRequest("Need a json value")} //here no json get from the request
    }

  }

  /**
   * Method that inserts a user on the "recommendation.users" collections.
   * @param user - the user to add to the db.
   * @return a Future[Boolean] that indicates the state of the operation.
   * true indicates that the insert was completed successful or that the user is already presents in the db.
   * false indicates that the insert was not completed successful (db error).
   **/
  private def addUserToDb(user: User): Future[Boolean] = {
    Users.find(user.id) onComplete {
      case Success(o) => o match {
        case Some(x) => Future(true)
        case None => Users.save(user)
      }
      case Failure(t) => Future(false)
    }
    Future(true)
  }

  
  /** Method that has the goal to save a user on the "recommendation.user collection".
     *  The data are passed in a json value like the following: {"id": "myid", "email": "myemail@examle.it"}
     *  id - the user id.
     *  email - the user email.
     *  @return an http response that indicates the status of the operation.
     *  Ok - the insert was completed successful.
     * BadRequest - the json was bad formed and the insert was not complete successful.
     * InternalServerError - the Server cannot access to the MongoDB instance.
     **/
  def addUser() = CorsAction.async { request =>
    val jsonData = request.body.asJson
    jsonData match {
      case Some(x) =>
        val userId = x \ "id" get;
        val userEmail = x \ "email" get;
        (userId, userEmail) match {
          case (id: JsString, email: JsString) =>
            val user = new User(id.as[String], Some(email.as[String])) //create a user Instance
            addUserToDb(user).flatMap{status => status match {
              case true => Future{Ok} //the insert was complete successful
              case _ => Future{InternalServerError("Cannot access to the db now")} //the db returns an error on the insert.
            }
            }
          case (_, _) => Future{BadRequest("json bad formed")} //the json is bad formed
        }
      case None => Future{BadRequest("Need a json value")} //need a json value
    }
  }

   /** Method that add a Tag on the user document.
    * The data are passed in a json value like the following: {"user": {"id": "myid", "email": "myemail@example.it"}, "tags": [{"tag": "myc:attr"}]}.
    * user - the user informations.
    * tags - the Tags to associate to the user.
    *  @return an http response that indicates the status of the operation.
    *  Ok - the insert was completed successful.
    * BadRequest - the insert was not completed successful. 
    */
   def addTagToUser() = CorsAction.async { request =>
     implicit val userRestFormat = UserFormatters.restFormatter
     implicit val inputFormat = InputFormatters.restFormatter
     implicit val outputWriter = OutputFormatters.restWriter
     //TODO controllare il metodo
     def updateTagToUserDB(input: Input): Future[Boolean] = {
       val userId = request.session.get("userId").getOrElse("")
       val inputUser = input.user.getOrElse(User(userId))
       val dreId = MatchingCookie(inputUser.id) { inputUser.id }
       val user = inputUser.copy(id = dreId)
       //** save new tags*/
       input.tags foreach {
         _.foreach(t => Tags.save(t, upsert = true))
       }
       Users.find(inputUser.id).onComplete {
         case Success(futureUser) => {
           futureUser match {
             case Some(u) => {
               u.tags map (t => println(t))
               val updatedUser = user.merge(u) /*input.tags.map { listTag =>
                 user.merge(u.addTags(listTag))
               }.getOrElse(user.merge(u))*/
               user.merge(u).tags.get map (t => println(t._1))
               Users.update(updatedUser)
             }
             case None => Future{false} //user doesn't exists
           }
         }
         case Failure(t) => None
       }
       Future(true)
     }
     val jsonData = request.body.asJson //get the json data
     jsonData match {
       case Some(x) => x.validate[Input] match {
         case JsSuccess(input, _) => updateTagToUserDB(input).flatMap(status => status match {
           case true => Future{Ok}
           case _ => Future{InternalServerError("Error on update the users tags")}
         })
         case e: JsError => Future{BadRequest("json bad formed")}
       }
       case None => Future{BadRequest("need a json value")}
     }
   }
}
