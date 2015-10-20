package recommendationsystem.models.storage

import scala.concurrent.Future

/**
 * Created by bsuieric on 19/10/15.
 */

trait RequestDao {
  def count: Future[Long]
}

object  RequestOdb extends RequestDao {
  override def count: Future[Long] = {
    val graph = Odb.factory.getNoTx
    val count = graph.countVertices("Requests")
    graph.shutdown()
    Future{count}
  }
}