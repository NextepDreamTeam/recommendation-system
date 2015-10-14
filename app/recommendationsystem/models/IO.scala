/*package recommendationsystem.models

/**
 * This classes represents the input and output of service 
 */

/*
case class Recommendation(
  idR: String,
  tags: Option[List[(String, Option[Double])]],
  kind: Option[String]) {

}
*/

case class Input(
  user: Option[User],
  tags: Option[List[Tag]],
  mandatoryTags: Option[List[Tag]],
  feedback: Option[String]) {

}

case class Output(recommendation: Advice, user: Option[User], mandatoryTags: Option[List[Tag]]) {

}

/**
 * Class that represent a REST request used for find all the users that has already bought product
 * of the given category, but non the given product.
 * @constructor Construct a FindSuggestion object.
 * @param category - the category given
 * @param product - the product given
 * @author Alberto Adami
 */

case class FindSuggestion(
    category: String,
    product: String)
    
/**
 * object that provides the formatters from a json to a FindSuggestion value.
 * @author Alberto Adami
 */
object FindSuggestion {
  implicit val storageFormatters = recommendationsystem.formatters.json.FindSuggestionFormatters
}

/**
 * Class that represents a REST request for a range in a period.
 * @param startDate - represents the startDate in milliseconds
 * @param finishDate - represents the finish date in milliseconds.
 * @constructor Create an InputGoodAdvise object.
 * @author Alberto Adami
 */
case class Range (
      startDate: Long,
      finishDate: Long)


object Range {
  implicit val storageFormatters = recommendationsystem.formatters.json.InputFormatters
}
*/