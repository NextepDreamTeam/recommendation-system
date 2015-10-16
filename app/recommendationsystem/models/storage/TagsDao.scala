package recommendationsystem.models.storage

import com.orientechnologies.orient.core.command.OCommandRequest
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal
import com.orientechnologies.orient.core.sql.OCommandSQL
import com.tinkerpop.blueprints.{Parameter, Vertex}
import com.tinkerpop.blueprints.impls.orient.{OrientVertex, OrientDynaElementIterable}

import recommendationsystem.models.Tag
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._

/**
 * Created by aandelie on 14/10/15.
 */
trait TagsDao {
  def count: Future[Long]

  def save(e: Tag, upsert: Boolean = false): Future[Boolean]

  def update(newTag: Tag, oldTag: Tag): Future[Boolean] //it will be used???

  def remove(e: Tag): Future[Boolean]

  def all: Future[List[Tag]]

  def find(id: String): Future[List[Tag]]
}


object TagsOdb extends TagsDao {
  override def count: Future[Long] = {
    val graph = Odb.factory.getNoTx
    val count = graph.countVertices("Tags")
    graph.shutdown()
    Future{count}
  }

  override def update(newTag: Tag, oldTag: Tag): Future[Boolean] = {
    val graph = Odb.factory.getTx
    val vl = graph.getVertices("Tags.tag",oldTag.flatten).asScala
    vl map (v => v.setProperty("tag",newTag.flatten))
    graph.commit
    Future{true}
  }

  override def all: Future[List[Tag]] = {
    val graph = Odb.factory.getTx
    ODatabaseRecordThreadLocal.INSTANCE.set(graph.getRawGraph)
    val vlst: Iterable[Vertex] = graph.getVerticesOfClass("Tags").asScala
    graph commit;
    val lst = vlst map (v => Tag(v.getProperty("tag"),None)) toList ;
    graph shutdown ;
    Future{lst}
  }

  override def remove(e: Tag): Future[Boolean] = {
    val graph = Odb.factory.getTx
    val vl: Iterable[Vertex] = graph.getVertices("Tags.tag",e.flatten).asScala
    //val vl: Iterable[Vertex] = graph.getVertices("Tags",Array("tag"),Array(e.flatten)).asScala
    graph.commit
    for(v <- vl) graph.removeVertex(v)
    graph.commit
    graph.shutdown
    Future{true}
  }

  override def save(e: Tag, upsert: Boolean): Future[Boolean] = {
    val graph = Odb.factory.getTx
    val v = graph.addVertex("Tags", null)
    v.setProperty("tag", e.flatten)
    graph.commit
    graph.shutdown()
    Future {true}
  }

  override def find(id: String): Future[List[Tag]] = {
    val graph = Odb.factory.getNoTx
    val tlst = graph.getVerticesOfClass("Tags").asScala.filter(p => p.getProperty("tag").equals(id))
    Future {tlst map (v => Tag(v.getProperty("tag"),None)) toList}
  }
}