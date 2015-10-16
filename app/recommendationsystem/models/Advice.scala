package recommendationsystem.models

import play.api.libs.functional._
import play.api.libs.json._
import recommendationsystem._

/**
 * 
 */
case class Advice(
  id: String,
  user: User,
  output: List[(Tag, Double)],
  date: Long,
  clicked: Boolean,
  kind: String = "system") {
  def this(id: String, user: User, output: List[(Tag, Double)]) = this(id, user, output, System.currentTimeMillis, false, "system")
  def this(id: String, user: User, output: List[(Tag, Double)], date: Long) = this(id, user, output, date, false, "system")
  def this(id: String, user: User, output: List[(Tag, Double)], kind: String) = this(id, user, output, System.currentTimeMillis, false, kind)
}

/**
 * 
 */
object Advice {
  def apply(id: String, user: User, output: List[(Tag, Double)]) = new Advice(id, user, output)
  def apply(id: String, user: User, input: Option[List[Tag]], output: List[(Tag, Double)]) = new Advice(id, user, output)
  def apply(id: String, user: User, output: List[(Tag, Double)], kind: String) = new Advice(id, user, output, kind)

  //implicit val adviceFormatter = formatters.json.AdviceFormatters.storageFormatter

}

/*
/**
 * 
 */
object Advices extends MongoObj[Advice] {
  val collectionName = "advices"//"recommendation.advices"
  implicit val storageFormat = formatters.json.AdviceFormatters.storageFormatter
}
*/