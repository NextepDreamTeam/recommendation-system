package recommendationsystem.models.storage

import _root_.recommendationsystem.models.User
import com.tinkerpop.blueprints._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.collection._

/**
 * Created by bsuieric on 15/10/15.
 */
trait UsersDao {
  def count: Future[Long]

  def save(e: User, upsert: Boolean = false): Future[Boolean]

  def remove(e: User): Future[Boolean]

  def all: Future[List[User]]

  def find(id: String): Future[List[User]]

}

object UsersOdb extends UsersDao{

  override def count: Future[Long] = {
    val graph = Odb.factory.getTx
    val count = graph.countVertices("Users")
    graph.shutdown()
    Future {count}
  }

  override  def save(e: User, upsert: Boolean): Future[Boolean]={
    val graph = Odb.factory.getTx
    val v = graph.addVertex("Users", null)
    v.setProperty("uid", e.id)
    v.setProperty("email", e.email)
    graph.commit()
    graph.shutdown()
    Future{true}
  }

  override  def remove(e : User): Future[Boolean]={
    val graph = Odb.factory.getTx
    val vert : Iterable[Vertex] = graph.getVertices("Users.uid", e.id).asScala
    graph.commit()
    for(v <- vert) graph.removeVertex(v)
    graph.commit()
    graph.shutdown()
    Future{true}
  }

  override def all: Future[List[User]] = {
    val graph = Odb.factory.getTx
    val vList: Iterable[Vertex] = graph.getVerticesOfClass("Users").asScala
    graph.commit;
    val list = vList map (v=> User(v.getProperty("uid"))) toList;
    graph.shutdown
    Future{list}
  }

  override  def find(id: String) : Future[List[User]] = {
    val graph = Odb.factory.getTx
    val uList = graph.getVerticesOfClass("Users").asScala
    Future {uList map( v => User(v.getProperty("uid"))) toList}
  }

}

