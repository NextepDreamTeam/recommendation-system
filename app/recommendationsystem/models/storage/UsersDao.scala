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

object UsersOdb extends UsersDao {

  override def count: Future[Long] = {
    val graph = Odb.factory.getNoTx
    val count = graph.countVertices("Users")
    graph.shutdown()
    Future {
      count
    }
  }

  override def save(e: User, upsert: Boolean): Future[Boolean] = {
    val graph = Odb.factory.getTx
    val v = graph.addVertex("Users", null)
    v.setProperty("uid", e.id)
    e.email match {
      case Some(mail) => v.setProperty("email", mail)
    }
    //Inserting edges in HoldsTag
    e.tags match {
      case Some(tagList) => {
        //tags must be present in database; creating edges from User to Tag
        tagList map
          (t => {
            val tagVertex = graph.getVertices("Tags.tag", t._1.flatten).asScala.head
            val userHoldsTagEdge = graph.addEdge(null,v,tagVertex,"HoldsTag")
            userHoldsTagEdge.setProperty("weight",t._2)
            userHoldsTagEdge.setProperty("lastInsert",t._3)
          })
      }
    }
    graph.commit()
    graph.shutdown()
    Future {
      true
    }
  }

  override def remove(e: User): Future[Boolean] = {
    val graph = Odb.factory.getTx
    val userVertex = graph.getVertices("Users.uid", e.id).asScala.head
    graph.removeVertex(userVertex)
    graph.commit()
    graph.shutdown()
    Future {true}
  }

  override def all: Future[List[User]] = {
    val graph = Odb.factory.getNoTx
    val usersListVertex = graph.getVerticesOfClass("Users").asScala.toList
    val usersList: List[User] = usersListVertex map (userVertex => {
      val userTagsEdge = userVertex.getEdges(Direction.OUT,"HoldsTag").asScala
      val userTagsVertex = userTagsEdge map (e => e.getVertex(Direction.OUT))
      val tagList = userTagsEdge zip userTagsVertex map
        (x => (x._2.getProperty("tag"),x._1.getProperty("weight"),x._1.getProperty("lastInsert"))) toList;
      User(userVertex.getProperty("uid"),Option(userVertex.getProperty("email")),None,Option(tagList))
    })
    graph.shutdown
    Future {usersList}
  }

  override def find(id: String): Future[List[User]] = {
    val graph = Odb.factory.getTx
    val usersListVertex = graph.getVerticesOfClass("Users").asScala.toList
    val usersList: List[User] = usersListVertex map (userVertex => {
      val userTagsEdge = userVertex.getEdges(Direction.OUT,"HoldsTag").asScala
      val userTagsVertex = userTagsEdge map (e => e.getVertex(Direction.OUT))
      val tagList = userTagsEdge zip userTagsVertex map
        (x => (x._2.getProperty("tag"),x._1.getProperty("weight"),x._1.getProperty("lastInsert"))) toList;
      User(userVertex.getProperty("uid"),Option(userVertex.getProperty("email")),None,Option(tagList))
    })
    graph.shutdown
    Future {usersList}
  }

}

