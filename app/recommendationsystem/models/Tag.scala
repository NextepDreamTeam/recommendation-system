package recommendationsystem.models

import recommendationsystem._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import play.api.Play.current
import play.api.libs.json.Json
import play.api.libs.iteratee.Enumerator

/**
 *
 */

case class Tag(
                category: String,
                attr: String,
                equalsTo: Option[List[(Tag, Double)]] = None,
                var rid: String = null) {

  lazy val id = (category + ":" + attr).foldLeft("")((acc, c) => acc + c.toInt)
  lazy val flatten = category + ":" + attr

  override def equals(that: Any): Boolean = that match {
    case t: Tag => (category == t.category && attr == t.attr)
    case _ => false
  }
}

object Tag {
  def apply(tag: String, eqTo: Option[List[(Tag, Double)]], rid: String) = {
    val splitted = tag.split(":+").toList
    splitted match {
      case x :: y :: Nil => new Tag(splitted(0), splitted(1), eqTo, rid)
      case _ => throw new Exception("Tag.apply() - No valid tag")
    }
    //new Tag(splitted(0), splitted(1), eqTo)
  }

  def unapplyTag(tag: Tag) = {
    tag.equalsTo match {
      case Some(eq) => Some((tag.category + ":" + tag.attr, tag.equalsTo))
      case None => Some((tag.category + ":" + tag.attr, None))
    }
    //    val a = (tag.category + ":" + tag.attr, tag.equalsTo)
    //    Some(a)
  }

  //da eliminare
  //implicit val tagFormatter = formatters.json.TagFormatters.storageFormatter

}

/*
object Tags extends MongoObj[Tag] {
  val collectionName = "tags"//"recommendation.tags"

  implicit val storageFormat = formatters.json.TagFormatters.storageFormatter
  //  implicit def ec: ExecutionContext = ExecutionContext.Implicits.global
  //
  //  implicit /*lazy val*/def db = ReactiveMongoPlugin.db
  //
  //  def collection: JSONCollection = db.collection[JSONCollection]("tags")

}
/**
 * Class that represents a tag in a REST request.
 * @constructor Construct a RestTag object.
 * @param tag - the tag String.
 * @author Alberto Adami
 */
 case class RestTag(tag: String)
 /**
  * Object that represents the formatter for a RestTag object.
  * @author Alberto Adami
  */
 object RestTag {
   /**
    * The converter from a JsValue to a RestTag object.
    */
  implicit val tagReader = Json.reads[RestTag]
 }
*/