package recommendationsystem.models.storage

import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal
import com.tinkerpop.blueprints.{Direction, Vertex}
import recommendationsystem.models.{Tag, User, Advice}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._

/**
 * Created by aandelie on 16/10/15.
 */
trait AdvicesDao {
  def count: Future[Long]

  def save(e: Advice, upsert: Boolean = false): Future[Boolean]

  //def update(newTag: Advice, oldTag: Advice): Future[Boolean]

  //def remove(e: Advice): Future[Boolean]

  //def all: Future[List[Advice]]

  //def find(id: String): Future[List[Advice]]
}

object AdvicesOdb extends AdvicesDao {
  override def count: Future[Long] = {
    val graph = Odb.factory.getNoTx
    val count = graph.countVertices("Advices")
    graph.shutdown()
    Future{count}
  }

  //override def update(newTag: Advice, oldTag: Advice): Future[Boolean] = ???

  /*override def all: Future[List[Advice]] = {
    val graph = Odb.factory.getTx
    ODatabaseRecordThreadLocal.INSTANCE.set(graph.getRawGraph)
    val vlst: Iterable[Vertex] = graph.getVerticesOfClass("Advice").asScala
    graph commit;
    //val lst = vlst map (v => Advice()) toList ;
    val lst = vlst map (av => {
      //get tags of thi advice
      val tagsAdviceVertex = av.getEdges(Direction.OUT,"AdviceOutput").asScala.map(v => v.getVertex(Direction.OUT))
      val output = tagsAdviceVertex map (x => (x.getProperty("tag"),0D)) toList

      //get user information
      val uv = av.getEdges(Direction.OUT,"AdviceUser").asScala.map(v => v.getVertex(Direction.OUT)).head
      //user must be one
      val userTagsEdge = av.getEdges(Direction.OUT,"HoldsTag").asScala
      val userTagsVertex = userTagsEdge map (e => e.getVertex(Direction.OUT))
      val tagList = userTagsEdge zip userTagsVertex map
        (x => (x._2.getProperty("tag"),x._1.getProperty("weight"),x._1.getProperty("lastInsert"))) toList
      val user = User(uv.getProperty("uid"),uv.getProperty("email"),None,Option(tagList))

      Advice(av.getProperty("aid"),user,output,av.getProperty("date"),av.getProperty("type"))
    })
    graph.shutdown
    Future{lst.toList}
  }*/

  //override def remove(e: Advice): Future[Boolean] = ???

  override def save(e: Advice, upsert: Boolean): Future[Boolean] = {
    val graph = Odb.factory.getTx
    val adviceVertex = graph.addVertex("Advices",null)
    adviceVertex.setProperty("aid",e.id)
    adviceVertex.setProperty("date",e.date)
    adviceVertex.setProperty("system",e.kind)
    graph.commit
    Future{true}
  }

  //override def find(id: String): Future[List[Advice]] = ???
}