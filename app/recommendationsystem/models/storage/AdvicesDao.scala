/*package recommendationsystem.models.storage

import java.util.concurrent.Future

import recommendationsystem.models.Advice

/**
 * Created by aandelie on 16/10/15.
 */
trait AdvicesDao {
  def count: Future[Long]

  def save(e: Advice, upsert: Boolean = false): Future[Boolean]

  def update(newTag: Advice, oldTag: Advice): Future[Boolean]

  def remove(e: Advice): Future[Boolean]

  def all: Future[List[Advice]]

  def find(id: String): Future[List[Advice]]
}

*/
