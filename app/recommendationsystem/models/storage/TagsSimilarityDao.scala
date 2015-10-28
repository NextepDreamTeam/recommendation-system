package recommendationsystem.models.storage

import com.orientechnologies.orient.core.sql.OCommandSQL
import com.tinkerpop.blueprints.{Edge, Direction}
import com.tinkerpop.blueprints.impls.orient.OrientDynaElementIterable
import recommendationsystem.algorithms.Similarity

import scala.concurrent.Future
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._

/**
 * Created by aandelie on 28/10/15.
 */
trait TagsSimilarityDao {
  def count: Future[Long]

  def save(e: Similarity, upsert: Boolean = false): Future[Boolean]

  def update(newTagSum: Similarity): Future[Boolean]

  def remove(e: Similarity): Future[Boolean]

  def all: Future[List[Similarity]]

  def find(query: String): Future[List[Similarity]]
}

object TagsSimilarityOdb extends TagsSimilarityDao {

  def getSimilarity(rid: AnyRef): Similarity = {
    val graph = Odb.factory.getNoTx
    val tagsSimilarityEdge = graph.getEdge(rid)
    val tagOneVertex = tagsSimilarityEdge.getVertex(Direction.IN)
    val tagTwoVertex = tagsSimilarityEdge.getVertex(Direction.OUT)
    Similarity(
      tagOneVertex.getProperty("tag"),
      tagTwoVertex.getProperty("tag"),
      tagsSimilarityEdge.getProperty("eq")
    )
  }

  override def count: Future[Long] = Future {
    val graph = Odb.factory.getNoTx
    graph.countEdges("TagsSimilarity")
  }

  override def update(e: Similarity): Future[Boolean] = Future{
    val graph = Odb.factory.getTx
    val tagOneVertices = graph.getVertices("Tags.tag",e.tag1Name).asScala
    val tagTwoVertices = graph.getVertices("Tags.tag",e.tag2Name).asScala
    (tagOneVertices,tagTwoVertices) match {
      case (tagOneVertex :: xt, tagTwoVertex :: xo) => {
        val edgesOne = tagOneVertex.getEdges(Direction.OUT,"TagsSimilarity").asScala
        val edgesTwo = tagTwoVertex.getEdges(Direction.IN,"TagsSimilarity").asScala
        val edges = edgesOne zip edgesTwo filter ( m => m._1.equals(m._2) ) map ( d => d._1)
        edges match {
          case Nil => false
          case edgeToOverride :: xc => {
            edgeToOverride.setProperty("eq",e.eq)
            graph.commit()
            true
          }
        }
      }
      case (Nil,Nil) => false
    }
  }

  override def all: Future[List[Similarity]] = Future {
    val graph = Odb.factory.getNoTx
    val edges = graph.getEdgesOfClass("TagsSimilarity").asScala
    edges.map(e => getSimilarity(e.getId)).toList
  }

  override def remove(e: Similarity): Future[Boolean] = Future{
    val graph = Odb.factory.getTx
    val tagOneVertices = graph.getVertices("Tags.tag",e.tag1Name).asScala
    val tagTwoVertices = graph.getVertices("Tags.tag",e.tag2Name).asScala
    (tagOneVertices,tagTwoVertices) match {
      case (tagOneVertex :: xt, tagTwoVertex :: xo) => {
        val edgesOne = tagOneVertex.getEdges(Direction.OUT,"TagsSimilarity").asScala
        val edgesTwo = tagTwoVertex.getEdges(Direction.IN,"TagsSimilarity").asScala
        val edges = edgesOne zip edgesTwo filter ( m => m._1.equals(m._2) ) map ( d => d._1)
        edges match {
          case Nil => false
          case edgeToRemove :: xc => edgeToRemove.remove(); graph.commit(); true
        }
      }
      case (Nil,Nil) => false
    }
  }

  override def save(e: Similarity, upsert: Boolean): Future[Boolean] = Future{
    val graph = Odb.factory.getTx
    val tagOneVertices = graph.getVertices("Tags.tag",e.tag1Name).asScala
    val tagTwoVertices = graph.getVertices("Tags.tag",e.tag2Name).asScala
    (tagOneVertices,tagTwoVertices) match {
      case (tagOneVertex :: xt, tagTwoVertex :: xo) => {
        val edgesOne = tagOneVertex.getEdges(Direction.OUT,"TagsSimilarity").asScala
        val edgesTwo = tagTwoVertex.getEdges(Direction.IN,"TagsSimilarity").asScala
        val edges = edgesOne zip edgesTwo filter ( m => m._1.equals(m._2) ) map ( d => d._1)
        edges match {
          case Nil => {
            val newEdge = graph.addEdge(null,tagOneVertex,tagTwoVertex,"TagsMatch")
            newEdge.setProperty("eq",e.eq)
            graph.commit()
            true
          }
          case edgeToOverride :: xc => {
            upsert match {
              case false => false
              case true => {
                edgeToOverride.setProperty("eq",e.eq)
                graph.commit()
                true
              }
            }
          }
        }
      }
      case (Nil,Nil) => false
    }
  }

  override def find(query: String): Future[List[Similarity]] = Future{
    val graph = Odb.factory.getNoTx
    val res: OrientDynaElementIterable = graph.command(new OCommandSQL(query)).execute()
    val ridEdges: Iterable[Edge] = res.asScala.asInstanceOf[Iterable[Edge]]
    ridEdges.map(rid => getSimilarity(rid.getId)).toList
  }
}