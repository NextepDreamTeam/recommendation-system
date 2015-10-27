package recommendationsystem.models.storage

import _root_.recommendationsystem.models.{Tag, Request, User}
import com.orientechnologies.orient.core.sql.OCommandSQL
import com.tinkerpop.blueprints.impls.orient.OrientDynaElementIterable
import com.tinkerpop.blueprints.{Direction, Vertex}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


trait RequestsDao {
  def count: Future[Long]

  def save(e: Request, upsert: Boolean = false): Future[Boolean]

  def remove(e: Request) : Future[Boolean]

  def all : Future[List[Request]]

  def find(query : String) : Future[List[Request]]

}

object  RequestsOdb extends RequestsDao {

  def getRequest(rid: AnyRef): Request = {
    val graph = Odb.factory.getNoTx
    val requestVertex = graph.getVertex(rid)
    val userVertex = requestVertex.getEdges(Direction.OUT, "RequestUser").asScala.map(
      v => v.getVertex(Direction.IN)).head
    val user: User = UsersOdb.getUser(userVertex.getId)
    val requestTagEdgeList = requestVertex.getEdges(Direction.OUT, "RequestHoldTag").asScala.map(
      ta => ta.getVertex(Direction.IN))
    val outputTagList = requestTagEdgeList map (rt => Tag(rt.getProperty("tag"),None))
    val output = Option(outputTagList.toList)
    Request(
      requestVertex.getProperty("reqid"),
      user,
      output ,
      None,
      requestVertex.getProperty("date")
    )
  }

  override def count: Future[Long] = Future {
    val graph = Odb.factory.getNoTx
    val count = graph.countVertices("Requests")
    count
  }

  override  def save(e: Request, upsert: Boolean = false) : Future[Boolean] = Future {
    synchronized {
      val graph = Odb.factory.getTx
      val requestVertex = graph.addVertex("Requests", null)
      requestVertex.setProperty("reqid", e.id)
      requestVertex.setProperty("date", e.date)
      val usersVertex = graph.getVertices("Users.uid", e.user.id).asScala
      if (usersVertex.isEmpty)
        throw new Exception("User not found in database")
      val userVertex = usersVertex.head
      graph.addEdge(null, requestVertex, userVertex, "RequestUser")

      e.tags match {
        case Some(tagList) =>
          tagList map
            (
              t => {
                val tagVertex = graph.getVertices("Tags.tag", t.flatten).asScala.head
                graph.addEdge(null, requestVertex, tagVertex, "RequestHoldTag")
              }
              )
        case None =>
      }
      graph.commit()
      true
    }
  }

  override def remove(e: Request) : Future[Boolean] = Future {
    synchronized {
      val graph = Odb.factory.getTx
      val requestsVertex = graph.getVertices("Requests.reqid", e.id).asScala
      if (requestsVertex.isEmpty)
        throw new Exception("Request not found in database")
      val requestVertex = requestsVertex.head
      graph.removeVertex(requestVertex)
      graph.commit()
      true
    }
  }

  override  def all : Future[List[Request]] = Future {
    val graph = Odb.factory.getNoTx
    val requestListVertex: Iterable[Vertex] = graph.getVerticesOfClass("Requests").asScala
    requestListVertex.map(r => getRequest(r)).toList
  }

  override  def find(query: String): Future[List[Request]] = Future {
    val graph = Odb.factory.getNoTx
    val res: OrientDynaElementIterable = graph.command(new OCommandSQL(query)).execute()
    val ridAdvices: Iterable[Vertex] = res.asScala.asInstanceOf[Iterable[Vertex]]

    ridAdvices.map(r => getRequest(r)).toList
  }

}

