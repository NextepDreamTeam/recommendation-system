/*package recommendationsystem.controllers

import play.api._
import play.api.mvc._
import play.api.cache.Cache
import play.api.libs.ws._
import scala.concurrent.Future
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
import recommendationsystem.formatters.json._
import play.Logger

/**
 * Object that represents the controller that has the goal to insert the data on the mongodb instance.
 * It's used by the bdrim application for insert tag, user and update the user tags.
 * @author Alberto Adami
 */
object BridgeController extends Controller {
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
  
  /**
   * Method that has the goal to add a new Tag on the "recommendation.tags"
   * The data are passed in a json value like the following: {"category": "c1", "attr": "a1"}.
   * category - the category of the tag (product category).
   * attr - the attribute of the tag (product name).
   * @return An http response that indicates the status of the operation.
   * Ok - the insert was completed successful.
   * BadRequest - the insert was not completed successful because the json was bad formed.
   * InternalServerError - the insert was not completed successful because the Server cannot access to the MongoDB instance.
   */
  def addTag = CorsAction.async { request =>
    
    def insertTagOnTheDB(tag: Tag): Future[Boolean] = {
      val query = Json.obj("tag" -> (tag.category  + ":" + tag.attr))
      Tags.save(tag, upsert = true).flatMap {status => status match {//upsert = true insert the tag on the db if doesn't exists
        	case LastError(ok, _, _, _, _, _, _) => Future{true}
        	case _ => Future{false} //cannot write to the db.
      }
      }
    }
    //get the json data
    val jsonData = request.body.asJson
    jsonData match {
      case Some(x) => val category = x \ "category"; val attr = x \ "attr"; // get the tags values
      				  (category, attr) match {
      				    case (c: JsString, a: JsString) => val tag = new Tag(c.as[String], a.as[String])
      				    								//insert the tag on the recommendation.tags collection
      				    								insertTagOnTheDB(tag).flatMap{status => status match {
      				    								  case true => Future{Ok} //insert ok
      				    								  case _ => Future{InternalServerError("Cannot acess to the db now")} //insert fail
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
   */
  private def addUserToDb(user: User): Future[Boolean] = {
    	 Users.find(Json.obj("id" -> user.id ,
   							"email" -> user.email)).toList.flatMap{users =>
   							  	val number = users.size // number of document matching the query
   							  	if(number > 0)//check if the user is already saved in the db.
   							  	  Future{true}
   							  	else {
   							  	  Users.save(user).flatMap {error => error match {
   							  	    	case LastError(ok, _, _, _, _, _, _) => Future{true} //insert complete successful
   							  	    	case _ => Future{false} //insert failure
   							  	  }}
   							  	}
   							  
   							}
  }
  
  /** Method that has the goal to save a user on the "recommendation.user collection".
     *  The data are passed in a json value like the following: {"id": "myid", "email": "myemail@examle.it"}
     *  id - the user id.
     *  email - the user email.
     *  @return an http response that indicates the status of the operation.
     *  Ok - the insert was completed successful.
     * BadRequest - the json was bad formed and the insert was not complete successful.
     * InternalServerError - the Server cannot access to the MongoDB instance.
     */
   def addUser = CorsAction.async { request =>
      val jsonData = request.body.asJson
      jsonData match {
        case Some(x) => val userId = x \ "id"; val userEmail = x \ "email";
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
   def addTagToUser = CorsAction.async { request =>
     implicit val userRestFormat = UserFormatters.restFormatter
     implicit val inputFormat = InputFormatters.restFormatter
     implicit val outputWriter = OutputFormatters.restWriter

     def updateTagToUserDB(input: Input): Future[Boolean] = {
       val userId = request.session.get("userId").getOrElse("")
       val inputUser = input.user.getOrElse(User(userId))
       val dreId = MatchingCookie(inputUser.id) { inputUser.id }
       val user = inputUser.copy(id = dreId)
       //** save new tags*/
       input.tags foreach {
         _.foreach(t => Tags.save(t, upsert = true))
       }
       val futureUser = Users.find(Json.obj("id" -> inputUser.id)).one
       futureUser flatMap(userF => userF match {
         case Some(u) =>
           val updatedUser = input.tags.map { listTag =>
             user.merge(u.addTags(listTag))
           }.getOrElse(user.merge(u))
           Users.update(Json.obj("id" -> user.id), updatedUser, upsert = true) flatMap{
            status => status match {//update the docuemnt if exists, otherwise create that.
             case LastError(ok, _, _, _, _, _, _) => Future{true} //update the document correctly
             case _ => Future{false} //update fail
           }
          }
         case None => Future{false} //user doesn't exists
       })


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
*/