package recommendationsystem.formatters.json

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._

import recommendationsystem.models._

object AdviceFormatters {

  /**
   * Rest format, hide some informations
   */
  implicit val restWriter: Writes[Advice] = {
    def fromAdvice(advice: Advice) = {
      Some(advice.id,
        Some(advice.output.map(pair => (pair._1.flatten, Some(pair._2)))),
        Some(advice.kind))
    }
    val writes = (
      (__ \ "idR").write[String] ~
      (__ \ "tags").writeNullable(Writes.traversableWrites[(String, Option[Double])](
        (__ \ "tag").write[String] ~
          (__ \ "score").writeNullable[Double] tupled)) ~
      (__ \ "type").writeNullable[String])(unlift(fromAdvice))
    writes
  }

}
