package controllers

import javax.inject.Inject

import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.orient.OrientGraph
import play.api._
import play.api.mvc._
import com.orientechnologies.orient

//imports for OrientDB
//import scala.collection.JavaConverters._
//import scala.collection.JavaConversions._


class Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

}
