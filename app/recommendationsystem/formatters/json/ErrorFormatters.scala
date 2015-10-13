package recommendationsystem.formatters.json

import play.api.libs.json._
import recommendationsystem.models.Error


object ErrorFormatters {
  
  implicit val generalFormatter = Json.format[Error]

}