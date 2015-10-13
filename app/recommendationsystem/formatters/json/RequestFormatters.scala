package recommendationsystem.formatters.json

import play.api.libs.json._

import recommendationsystem.models._

object RequestFormatters {

    import play.api.libs.json._
  //  import play.api.libs.functional.syntax._
  //  import play.api.libs.json.Reads._
  //
  //  val requestReads: Reads[Request] = (
  //    (__ \ "id").readNullable[String] ~
  //    (__ \ "email").readNullable[String] ~
  //    (__ \ "tags").read(
  //      list[Tag]))(Request)
  
  implicit val storageFormatter = Json.format[Request]

}