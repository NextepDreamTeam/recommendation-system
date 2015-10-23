package recommendationsystem.models.storage

import _root_.recommendationsystem.models.{Request, User}
import com.tinkerpop.blueprints.{Direction, Vertex}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
 * Created by bsuieric on 19/10/15.
 */

case class NothingThereException(message: String) extends Exception(message)

trait RequestsDao {
  def count: Future[Long]

  def save(e: Request, upsert: Boolean = false): Future[Boolean]

  def remove(e: Request) : Future[Boolean]

  def all : Future[List[Request]]

  def find(id : String) : Future[Option[Request]]

}

object  RequestsOdb extends RequestsDao {
  override def count: Future[Long] = {
    val graph = Odb.factory.getNoTx
    val count = graph.countVertices("Requests")
    graph.shutdown()
    Future{count}
  }

}

  override  def save(e: Request, upsert: Boolean = false) : Future[Boolean] = {
    val graph = Odb.factory.getTx
    val requestVertex = graph.addVertex("Requests", null)
    requestVertex.setProperty("reqid", e.id)
    requestVertex.setProperty("date", e.date)
    graph.commit()
    val usersVertex = graph.getVertices("Users.uid", e.user.id).asScala
    if(usersVertex.isEmpty)
      throw NothingThereException("User not found in database")
    val userVertex = usersVertex.head
    graph.addEdge(null, requestVertex, userVertex, "RequestUser")
    graph.commit()

    e.tags match {
      case Some(tagList) => {
        tagList map
          (
            t => {
              val tagVertex = graph.getVertices("Tags.tag", t.flatten).asScala.head
              graph.addEdge(null, requestVertex, tagVertex, "RequestHoldTag")
            }
            )
      }
    }
    graph.commit()
    graph.shutdown()
    Future{true}
  }

  override def remove(e: Request) : Future[Boolean] = {
    val graph = Odb.factory.getTx
    val requestsVertex = graph.getVertices("Requests.reqid", e.id).asScala
    if(requestsVertex.isEmpty)
      throw NothingThereException("Request not found in database")
    val requestVertex = requestsVertex.head
    graph.removeVertex(requestVertex)
    graph.commit()
    graph.shutdown()
    Future{true}
  }

  override  def all : Future[List[Request]] = {
    val graph = Odb.factory.getTx
    //ODatabaseRecordThreadLocal.INSTANCE.set(graph.getRawGraph)
    val requestListVertex: Iterable[Vertex] = graph.getVerticesOfClass("Requests").asScala
    val requestList = requestListVertex map(requestVertex => {

      val requestHoldTagVertex = requestVertex.getEdges(Direction.OUT, "RequestHoldTag").asScala.map(
        v => v.getVertex(Direction.OUT))
      val output = requestHoldTagVertex map( x => (x.getProperty("tag"))) toList

      //user info
      val requestUserEdge = requestVertex.getEdges(Direction.OUT, "RequestUser").asScala.map(
      v => v.getVertex(Direction.OUT)).head

      val requestUserTagsVertex = requestUserEdge.getEdges(Direction.OUT, "HoldsTag").asScala
      val userTagsVertex = requestUserTagsVertex map(
        e => e.getVertex(Direction.OUT))
      val tagList = requestUserTagsVertex zip userTagsVertex map(
        x => (x._2.getProperty("tag"), x._1.getProperty("weight"), x._1.getProperty("lastInsert"))) toList
      val user = User(requestUserEdge.getProperty("uid"), requestUserEdge.getProperty("email"), None, Option(tagList))

      Request(
      requestVertex.getProperty("reqid"),
      user,
      Option(output),
      None,
      requestVertex.getProperty("date")
      )
    })
    graph.shutdown()
    Future{requestList.toList}
  }

  override  def find(id: String): Future[Option[Request]] = {
    val graph = Odb.factory.getNoTx
    val requestVertices = graph.getVertices("Requests.reqid", id).asScala
    if(requestVertices.isEmpty)
      Future{None}
    else {
      val requestVertice = requestVertices.head

      val requestTagEdgeList = requestVertice.getEdges(Direction.OUT, "RequestHoldTag").asScala.map(
        ta => ta.getVertex(Direction.OUT))

        val output = requestTagEdgeList map (rt => (rt.getProperty("tag"))) toList

        val userVertex = requestVertice.getEdges(Direction.OUT, "RequestUser").asScala.map(
          v => v.getVertex(Direction.OUT)).head
        val requestUserTagsVertex = userVertex.getEdges(Direction.OUT, "HoldsTag").asScala
        val userTagsVertex = requestUserTagsVertex map (
          e => e.getVertex(Direction.OUT))
        val tagList = requestUserTagsVertex zip userTagsVertex map (
          x => (x._2.getProperty("tag"), x._1.getProperty("weight"), x._1.getProperty("lastInsert"))) toList
        val user = User(userVertex.getProperty("uid"), userVertex.getProperty("email"), None, Option(tagList))

        val request = Request(requestVertice.getProperty("reqid"), user, Option(output), None, requestVertice.getProperty("date"))
        Future {
          Option(request)
        }
    }
  }




}

