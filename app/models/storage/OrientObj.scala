package models.storage

import com.tinkerpop.blueprints.impls.orient.OrientGraph

import scala.concurrent.{ ExecutionContext, Future }
import play.api.libs.iteratee.Enumerator
import play.api.libs.json._
import play.api.Play.current



trait OrientObj[T] {

	def collectionName: String
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global	//???


	/**
   * Return number of all documents on collection
   */
  //def count: Future[Int] = collection.db.command(Count(collection.name))
  def count: Future[Int] = {
    var graph = new OrientGraph("remote://localhost:2727/recommendation-system")
    try {
      //val bogdan = graph.addVertex(null,"V")
      //bogdan.setProperty("name","surieca")
      //graph.commit()
      var result = graph.query().has("name")

    } catch {
      case e: Exception => graph.rollback()
    } finally {
      graph.shutdown()
    }
  }

  /**
   * Insert an element to collection,
   * Implicit T -> JsObject must be in scope
   */
  //def save(e: T, upsert: Boolean = false): Future[LastError] = {
  //  if (upsert) collection.update(e, e, GetLastError(), upsert)
  //  else collection.insert(e)
  //}

  /**
   * Update an element to collection with parameter e, the element to update is selected by parameter query
   * Implicit T -> JsObject must be in scope
   */
  //def update(query: JsObject, e: JsObject): Future[LastError] = {
  //  collection.update(query, Json.obj("$set" -> e), GetLastError())
  //}

  /**
   * Update an element to collection with parameter e, the element to update is selected by parameter query
   * Implicit T -> JsObject must be in scope
   */
  //def update(query: T, e: T, upsert: Boolean): Future[LastError] = {
  //  collection.update(query, e, GetLastError(), upsert)
  //}

  /**
   * Update an element to collection with parameter e, the element to update is selected by parameter query
   * Implicit T -> JsObject must be in scope
   */
  //def update(query: JsObject, e: T, upsert: Boolean = false): Future[LastError] = {
  //  collection.update(query, e, GetLastError(), upsert)
  //}

  /**
   * Remove an element from collection,
   * Implicit T -> JsObject must be in scope
   */
  //def remove(e: T): Future[LastError] = {
  //  collection.remove(e)
  //}

  /**
   * Return all element in collection
   * Implicit T -> JsObject must be in scope
   */
  //def all(implicit tformat: Format[T]): MyQueryBuilder =
  //  MyQueryBuilder(collection.find(Json.obj()))

  /**
   * Find elements in collection, sorted by sortBy parameter
   * Implicit T -> JsObject must be in scope
   */
  //def find(selector: JsValue)(implicit tformat: Format[T]): MyQueryBuilder = {
  //  MyQueryBuilder(collection.find(selector))
  //}

}
/*
  var graph = new OrientGraph("remote://localhost:2727/recommendation-system")
    try {
      //val bogdan = graph.addVertex(null,"V")
      //bogdan.setProperty("name","surieca")
      //graph.commit()
      var it = graph.getVertices.iterator()
      while (it.hasNext) {
        print(it.next().getProperty("name"))
      }

    } catch {
      case e: Exception => graph.rollback()
    } finally {
      graph.shutdown()
    }
 */