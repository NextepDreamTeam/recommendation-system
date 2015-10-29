package recommendationsystem.models

import recommendationsystem._
import play.api.libs.functional._
import play.api.libs.json._
import recommendationsystem.models.storage.{CorrelationsOdb, CorrelationsDao}

import scala.concurrent.Future

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
                       weight: Double){
  lazy val flatten = category + ":" + attribute
}
/**
 * Object that represent the companion object of the Correlation class.
 * @author Alberto Adami 
 */
object Correlation {
  /**
   * Implicit converter from json to Correlation object and vice versa.
   */
  //implicit val storageFormat = formatters.json.CorrelationFormatters.storageFormatter
}


/**
 * Object that access on the "recommendation.correlation" collection.
 * It write/read/update/remove Correlation data from the db.
 * @author Alberto Adami
 */
object Correlations extends CorrelationsDao {

  /*
  /** Collection name of mongodb */
  val collectionName = "correlation"//"recommendation.correlation"
  /**
   * Implicit converter from json to Correlation object and viceversa.
   */
  implicit val storageFormat = formatters.json.CorrelationFormatters.storageFormatter*/
  override def count: Future[Long] = CorrelationsOdb.count

  override def update(newCorrelation: Correlation): Future[Boolean] = CorrelationsOdb.update(newCorrelation)

  override def all: Future[List[Correlation]] = CorrelationsOdb.all

  override def remove(e: Correlation): Future[Boolean] = CorrelationsOdb.remove(e)

  override def save(e: Correlation, upsert: Boolean): Future[Boolean] = CorrelationsOdb.save(e,upsert)

  override def find(query: String): Future[List[Correlation]] = CorrelationsOdb.find(query)
}
