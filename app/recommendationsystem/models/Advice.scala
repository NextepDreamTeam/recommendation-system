package recommendationsystem.models

import play.api.libs.functional._
import play.api.libs.json._
import recommendationsystem.models.storage.{AdvicesOdb, AdvicesDao}

import scala.concurrent.Future

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


/**
 * 
 */
object Advices extends AdvicesDao {
  override def count: Future[Long] = AdvicesOdb.count

  override def update(newAdvice: Advice): Future[Boolean] = AdvicesOdb.update(newAdvice)

  override def all: Future[List[Advice]] = AdvicesOdb.all

  override def remove(e: Advice): Future[Boolean] = AdvicesOdb.remove(e)

  override def save(e: Advice, upsert: Boolean): Future[Boolean] = AdvicesOdb.save(e,upsert)

  override def find(query: String): Future[List[Advice]] = AdvicesOdb.find(query)
}
