package recommendationsystem.models.storage

import _root_.recommendationsystem.models.User

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by bsuieric on 15/10/15.
 */
trait UsersDao {
  def count: Future[Long]

  def save(e: User, upsert: Boolean = false): Future[Boolean]

  def remove(e: User): Future[Boolean]

}

object UsersOdb extends UsersDao{

  override def count: Future[Long] = {
    val graph = Odb.factory.getTx
    val count = graph.countVertices("Users")
    graph.shutdown()
    Future {count}
  }

  override  def save(e: User): Future[Boolean]={
    val graph = Odb.factory.getNoTx
    val v = graph.addVertex("Users",12)
    v.setProperty("id", e.id)
    v.setProperty("email", e.email)
    graph.commit()
    graph.shutdown()
    Future{true}
  }

  override  def remove(e : User,): Future[Boolean]={
    val graph = Odb.factory.getTx
    graph.removeVertex(e.id)
    graph.shutdown()
    Future{true}
  }


}

