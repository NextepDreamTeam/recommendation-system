package recommendationsystem.models

import play.modules.reactivemongo._
import recommendationsystem._
import recommendationsystem.models.storage.MongoObj
import play.api.libs.functional._
import play.api.libs.json._

/**
 * Class that represent the correlation between a category and an attribute.

 * @constructor Create a Correlation object.
 * @param category - the category name.
 * @param attribute - the attribute correlated with the category.
 * @param average - the correlation value between the category and the attribute.
 * @param weight - the weight of the correlation element.
 * @author Alberto Adami
 */
case class Correlation(category: String,
                       attribute: String,
				               average: Double,
                       weight: Double)

/**
 * Object that represent the companion object of the Correlation class.
 * @author Alberto Adami 
 */
object Correlation {
  /**
   * Implicit converter from json to Correlation object and vice versa.
   */
  implicit val storageFormat = formatters.json.CorrelationFormatters.storageFormatter
}


/**
 * Object that access on the "recommendation.correlation" collection.
 * It write/read/update/remove Correlation data from the db.
 * @author Alberto Adami
 */
object Correlations extends MongoObj[Correlation] {
  /** Collection name of mongodb */
  val collectionName = "correlation"//"recommendation.correlation"
  /**
   * Implicit converter from json to Correlation object and viceversa.
   */
  implicit val storageFormat = formatters.json.CorrelationFormatters.storageFormatter
}
