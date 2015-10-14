/*package recommendationsystem.formatters.json

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._

import recommendationsystem.models._

object AdviceFormatters {

  /**
   * Storage format, example for mongoDB
   */
  implicit val storageFormatter = {
    val adviceReads: Reads[Advice] = (
      (__ \ "id").read[String] ~
      (__ \ "user").read[User] ~
      (__ \ "output").lazyRead(list[(String, Double)](
        (__ \ "tag").read[String] ~
          (__ \ "score").read[Double] tupled)).map(list => list.map(t => (Tag(t._1, None), t._2))) ~
      (__ \ "date").readNullable[Long].map(f => f.getOrElse(System.currentTimeMillis)) ~
      (__ \ "clicked").readNullable[Boolean].map(f => f.getOrElse(false)) ~
      (__ \ "type").readNullable[String].map(f => f.getOrElse("undefined")))(Advice.apply(_, _, _, _, _, _))

    import play.api.libs.json.Writes._
    val adviceWrites: Writes[Advice] = (
      (__ \ "id").write[String] ~
      (__ \ "user").write[User] ~
      (__ \ "output").lazyWrite(Writes.traversableWrites[(Tag, Double)](
        (__ \ "tag").write[String].contramap[Tag](t => t.flatten) ~
          (__ \ "score").write[Double] tupled)) ~
      (__ \ "date").write[Long] ~
      (__ \ "clicked").writeNullable[Boolean].contramap { f: Boolean => if (f) Some(f) else None } ~
      (__ \ "type").writeNullable[String].contramap { s: String => if (!s.isEmpty) Some(s) else None })(unlift(Advice.unapply _))

    Format(adviceReads, adviceWrites) //Json.format[Advice]
  }

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
*/