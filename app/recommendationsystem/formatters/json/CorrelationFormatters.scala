package recommendationsystem.formatters.json

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import recommendationsystem.models._
import play.api.data.validation.ValidationError
import play.api.libs.json.Writes._

/**
 * Object that represents the formatter for a Correlation object.
 * @author Alberto Adami
 */
object CorrelationFormatters {
  
   implicit val storageFormatter: Format[Correlation] = {     
   val correlationReads: Reads[Correlation] = (
       (__ \ "category").read[String] and
       (__  \"attribute").read[String] and
       (__  \ "value" \ "average").read[Double] and
       (__  \"value" \ "weight").read[Double]
       )(Correlation.apply _)
       
   val correlationWrites: Writes[Correlation] = (
        (__ \ "category").write[String] and
        (__ \ "attribute").write[String] and
        (__ \ "value" \ "average").write[Double] and
        (__ \ "value" \ "weight").write[Double]
        )(unlift(Correlation.unapply _))
        
   Format(correlationReads, correlationWrites)
   }
      
}