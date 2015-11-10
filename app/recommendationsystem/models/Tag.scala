package recommendationsystem.models

import play.api.libs.json.Json
import recommendationsystem.models.storage.{TagsDao, TagsOdb}

import scala.concurrent.Future

/**
 *
 */

case class Tag(
                category: String,
                attr: String,
                equalsTo: Option[List[(Tag, Double)]] = None) {

  lazy val id = (category + ":" + attr).foldLeft("")((acc, c) => acc + c.toInt)
  lazy val flatten = category + ":" + attr

  override def equals(that: Any): Boolean = that match {
    case t: Tag => category == t.category && attr == t.attr
    case _ => false
  }
}

object Tag {
  def apply(tag: String, eqTo: Option[List[(Tag, Double)]]) = {
    val splitted = tag.split(":+").toList
    splitted match {
      case x :: y :: Nil => new Tag(x, y, eqTo)
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

  implicit val tagFormatter = recommendationsystem.formatters.json.TagFormatters.storageFormatter

}


object Tags extends TagsDao {

  def count: Future[Long] = TagsOdb.count

  def save(tag: Tag, upsert: Boolean = false): Future[Boolean] = TagsOdb.save(tag,upsert)

  def update(newTag: Tag, oldTag: Tag): Future[Boolean] = TagsOdb.update(newTag,oldTag)

  def remove(tag: Tag): Future[Boolean] = TagsOdb.remove(tag)

  def all: Future[List[Tag]] = TagsOdb.all

  def find(query: String): Future[List[Tag]] = TagsOdb.find(query)

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