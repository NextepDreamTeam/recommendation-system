/*package recommendationsystem.formatters.json

import play.api.libs.json._
import play.api.libs.functional.syntax._

import recommendationsystem.models._

/**
 * Formatters for class Input
 */
object InputFormatters {

  /**
   * Rest formatter
   */
  implicit val restFormatter: Format[Input] = {
    val reader: Reads[Input] = (
      (__ \ "user").readNullable[User] ~
      (__ \ "tags").readNullable(Reads.list[Tag]) ~
      (__ \ "mandatoryTags").readNullable(Reads.list[Tag]) ~
      ((__ \ "feedback" \ "idR").readNullable[String] orElse (__ \ "feedback").readNullable[String]))(Input.apply _)

    val writer: Writes[Input] = (
      (__ \ "user").writeNullable[User] ~
      (__ \ "tags").writeNullable(Writes.list[Tag]) ~
      (__ \ "mandatoryTags").writeNullable(Writes.list[Tag]) ~
      (__ \ "feedback" \ "idR").writeNullable[String])(unlift(Input.unapply _))

    Format(reader, writer)
  }

}

/**
 * Formatters for class Output
 */
object OutputFormatters {

  /**
   * Rest Writer
   */
  implicit val restWriter: Writes[Output] = {
    implicit val adviceWriter = AdviceFormatters.restWriter
    val writer: Writes[Output] = (
      (__ \ "recommendation").write[Advice] ~
      (__ \ "user").writeNullable[User] ~
      (__ \ "mandatoryTags").writeNullable(Writes.list[Tag]))(unlift(Output.unapply _))
    writer
    //Format(reader, writer)
  }

}
/**
 * object that represents the formatters for a FindSuggestion object.
 * It provides the implicit to pass from a JsValue to a FindSuggestion object.
 * @author Alberto Adami
 */
object FindSuggestionFormatters {
	implicit val readerFormatter = Json.reads[FindSuggestion]
}

/**
 * object that represents the formatter for a Range object.
 * It provides the functionality to pass from a JsValue to a Range object.
 * @author Alberto Adami
 */
object RangeFormatters {
  implicit val readerFormatter = Json.reads[Range]
}
*/