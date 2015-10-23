/*package recommendationsystem.formatters.json

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.data.validation.ValidationError

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import play.api.Play.current
import play.api.libs.json.Json
import play.api.libs.iteratee.Enumerator
import play.api.data.validation.ValidationError

import recommendationsystem.models.Tag

object TagFormatters {

  def validTag(implicit r: Reads[String]): Reads[String] =
    Reads.filter(ValidationError("validate.error.unexpected.value", "tag format isn't correct, maybe missing colon?"))(
      _.toString.split(":+").toList match {
        case x :: y :: Nil => !x.isEmpty && !y.isEmpty
        case _ => false
      })

  val reader: Reads[Tag] = (
    (__ \ "tag").read[String](validTag) ~
    (__ \ "equalsTo").lazyReadNullable(
      Reads.list[(Tag, Double)](
        (__ \ "tag").lazyRead(reader) ~
          (__ \ "weight").read[Double] tupled)))(Tag(_, _))

  import play.api.libs.json.Writes._
  val writer: Writes[Tag] = (
    (__ \ "tag").write[String] ~
    (__ \ "equalsTo").lazyWriteNullable(
      Writes.traversableWrites[(Tag, Double)](
        (__ \ "tag").lazyWrite(writer) ~
          (__ \ "weight").write[Double] tupled)))(unlift(Tag.unapplyTag))

  implicit val storageFormatter = Format(reader, writer)

}
*/