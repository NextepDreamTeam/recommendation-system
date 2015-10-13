package recommendationsystem.controllers.manager

import play.api._
import play.api.mvc._
import reactivemongo.api._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._
import recommendationsystem.algorithms.Pearson
import scala.concurrent.Future

class Manager extends Controller{

	def startPearson = Action.async{
	  val futureResult = Pearson.calculate
	  /*TODO: Creare interfaccia amministrazione andamento algoritmo e gestione*/
	  Future {Ok(Json.obj("result" -> "ok"))}
	}
	
	
	def documents = Action.async {
	  Future {Ok(recommendationsystem.views.html.files.documents("documents"))}
	}
	
	def plugins = Action.async {
	  Future {Ok(recommendationsystem.views.html.files.plugins("plugins"))}
	}
}