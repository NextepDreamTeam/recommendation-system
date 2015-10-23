package recommendationsystem.models.storage

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

  def update(newTag: Tag, oldTag: Tag): Future[Boolean]

  def remove(e: Tag): Future[Boolean]

  def all: Future[List[Tag]]

  def find(id: String): Future[Tag]
}


object TagsOdb extends TagsDao {
  override def count: Future[Long] = Future {
    val graph = Odb.factory.getNoTx
    val count = graph.countVertices("Tags")
    count
  }

  override def update(newTag: Tag, oldTag: Tag): Future[Boolean] = Future {
    val graph = Odb.factory.getTx
    val tagVertices = graph.getVertices("Tags.tag",oldTag.flatten).asScala
    if(tagVertices.isEmpty) throw new Exception("Tag not found: "+oldTag.id)
    tagVertices.head.setProperty("tag",newTag.flatten)
    graph.commit()
    true
  }

  override def all: Future[List[Tag]] = Future {
    val graph = Odb.factory.getNoTx
    val tagVertices = graph.getVerticesOfClass("Tags").asScala
    val tagList = tagVertices.map(v => Tag(v.getProperty("tag"),None)).toList
    tagList
  }

  override def remove(e: Tag): Future[Boolean] = Future {
    val graph = Odb.factory.getTx
    val tagVertices = graph.getVertices("Tags.tag",e.flatten).asScala
    if(tagVertices.isEmpty) throw new Exception("Tag not found: "+e.flatten)
    tagVertices.head.remove()
    graph.commit()
    true
  }

  override def save(e: Tag, upsert: Boolean = false): Future[Boolean] = Future {
    val graph = Odb.factory.getTx
    val v = graph.getVertices("Tags.tag",e.flatten).asScala
    if(v.nonEmpty) {
      if (upsert)
        v.head.setProperty("tag", e.flatten)
      else
        throw new Exception("Element already in database")
    } else {
      val tagVertex = graph.addVertex("Tags", null)
      tagVertex.setProperty("tag", e.flatten)
    }
    graph.commit()
    true
  }

  override def find(id: String): Future[Tag] = Future {
    val graph = Odb.factory.getNoTx
    val tagVertices = graph.getVertices("Tags.tag",id).asScala
    if(tagVertices.isEmpty) throw new Exception("Tag not found: "+id)
    Tag(tagVertices.head.getProperty("tag"),None)
  }
}