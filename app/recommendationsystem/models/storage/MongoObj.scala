/*package recommendationsystem.models.storage

import scala.concurrent.{ ExecutionContext, Future }

// Reactive Mongo imports
import reactivemongo.api._
import reactivemongo.api.collections._
import reactivemongo.core.commands._

// Play Json imports
import play.modules.reactivemongo._
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.libs.iteratee.Enumerator
import play.api.libs.json._

import play.api.Play.current

trait MongoObj[T] {
  def collectionName: String
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  /** Switch to implicit lazy val on production */
  implicit def db = ReactiveMongoPlugin.db
  /** Switch to implicit val on production */
  def collection: JSONCollection = db.collection[JSONCollection](collectionName)
  /** storage formatter */
  implicit val storageFormat: Format[T]

  case class MyQueryBuilder(gqb: GenericQueryBuilder[JsObject, Reads, Writes]) {

    def sort(by: JsObject): MyQueryBuilder = {
      MyQueryBuilder(gqb.sort(by))
    }

    /**
     * Skip first s element
     * @param s number of element to skip
     * Implicit T -> JsObject must be in scope
     */
    def skip(s: Int): MyQueryBuilder = {
      MyQueryBuilder(gqb.options(QueryOpts(s)))
    }

    /**
     * Take only l element
     * @param l number of element to take
     * Implicit T -> JsObject must be in scope
     */
    def limit(l: Int): MyQueryBuilder = {
      val opt = gqb.options
      MyQueryBuilder(gqb.options(opt.batchSize(l)))
      //collection.find(Json.obj()).sort(sortBy).options(QueryOpts(skip, limit)).cursor[T].collect[List](limit)
    }

    /**
     * Return list of all result elements
     * Implicit T -> JsObject must be in scope
     */
    def toList(implicit read: Reads[T]): Future[List[T]] = {
      if (gqb.options.batchSizeN > 0)
        gqb.cursor[T].collect[List](gqb.options.batchSizeN)
      else
        gqb.cursor[T].collect[List](100000)
    }

    /**
     * Return an enumerator of all elements
     * Implicit T -> JsObject must be in scope
     */
    def toEnum(implicit read: Reads[T]): Enumerator[T] = {
      gqb.cursor[T].enumerate()
    }

    /**
     * skip parameter
     * Implicit T -> JsObject must be in scope
     */
    def one(implicit read: Reads[T]): Future[Option[T]] = {
      gqb.one[T]
    }

  }

  /**
   * Return number of all documents on collection
   */
  def count: Future[Int] = collection.db.command(Count(collection.name))

  /**
   * Insert an element to collection,
   * Implicit T -> JsObject must be in scope
   */
  def save(e: T, upsert: Boolean = false): Future[LastError] = {
    if (upsert) collection.update(e, e, GetLastError(), upsert)
    else collection.insert(e)
  }

  /**
   * Update an element to collection with parameter e, the element to update is selected by parameter query
   * Implicit T -> JsObject must be in scope
   */
  def update(query: JsObject, e: JsObject): Future[LastError] = {
    collection.update(query, Json.obj("$set" -> e), GetLastError())
  }

  /**
   * Update an element to collection with parameter e, the element to update is selected by parameter query
   * Implicit T -> JsObject must be in scope
   */
  def update(query: JsObject, e: T, upsert: Boolean = false): Future[LastError] = {
    collection.update(query, e, GetLastError(), upsert)
  }

  /**
   * Remove an element from collection,
   * Implicit T -> JsObject must be in scope
   */
  def remove(e: T): Future[LastError] = {
    collection.remove(e)
  }

  /**
   * Return all element in collection
   * Implicit T -> JsObject must be in scope
   */
  def all(implicit tformat: Format[T]): MyQueryBuilder =
    MyQueryBuilder(collection.find(Json.obj()))

  /**
   * Find elements in collection, sorted by sortBy parameter
   * Implicit T -> JsObject must be in scope
   */
  def find(selector: JsValue)(implicit tformat: Format[T]): MyQueryBuilder = {
    MyQueryBuilder(collection.find(selector))
  }

}
*/