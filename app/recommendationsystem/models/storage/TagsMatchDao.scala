package recommendationsystem.models.storage

import com.orientechnologies.orient.core.sql.OCommandSQL
import com.tinkerpop.blueprints.{Edge, Direction}
import com.tinkerpop.blueprints.impls.orient.OrientDynaElementIterable
import recommendationsystem.algorithms.TagsMatch

import scala.concurrent.Future
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._

/**
 * Created by aandelie on 28/10/15.
 */
trait TagsMatchDao {
  def count: Future[Long]

  def save(e: TagsMatch, upsert: Boolean = false): Future[Boolean]

  def update(newTagSum: TagsMatch): Future[Boolean]

  def remove(e: TagsMatch): Future[Boolean]

  def all: Future[List[TagsMatch]]

  def find(query: String): Future[List[TagsMatch]]
}

object TagsMatchOdb extends TagsMatchDao {

  def getTagsMatch(rid: AnyRef): TagsMatch = {
    val graph = Odb.factory.getNoTx
    val tagsMatchEdge = graph.getEdge(rid)
    val tagOneVertex = tagsMatchEdge.getVertex(Direction.IN)
    val tagTwoVertex = tagsMatchEdge.getVertex(Direction.OUT)
    val tagOne = tagOneVertex.getProperty("tag")
    val tagTwo = tagTwoVertex.getProperty("tag")
    val fields = tagsMatchEdge.getProperties
    TagsMatch(
      tagOne,tagTwo,
      fields.get("sum1").asInstanceOf[Double],
      fields.get("sum2").asInstanceOf[Double],
      fields.get("sumQ1").asInstanceOf[Double],
      fields.get("sumQ2").asInstanceOf[Double],
      fields.get("sumProd").asInstanceOf[Double],
      fields.get("count").asInstanceOf[Int]
    )
  }

  override def count: Future[Long] = Future {
    val graph = Odb.factory.getNoTx
    graph.countEdges("TagsMatch")
  }

  override def update(e: TagsMatch): Future[Boolean] = Future {
    val graph = Odb.factory.getTx
    val tagOneVertices = graph.getVertices("Tags.tag",e.tag1).asScala
    val tagTwoVertices = graph.getVertices("Tags.tag",e.tag2).asScala
    (tagOneVertices,tagTwoVertices) match {
      case (tagOneVertex :: xt, tagTwoVertex :: xo) => {
        val edgesOne = tagOneVertex.getEdges(Direction.OUT,"TagsMatch").asScala
        val edgesTwo = tagTwoVertex.getEdges(Direction.IN,"TagsMatch").asScala
        val edges = edgesOne zip edgesTwo filter ( m => m._1.equals(m._2) ) map ( d => d._1)
        edges match {
          case Nil => false
          case edgeToOverride :: xc => {
            edgeToOverride.setProperty("sum1",e.sum1)
            edgeToOverride.setProperty("sum2",e.sum2)
            edgeToOverride.setProperty("sumQ1",e.sumQ1)
            edgeToOverride.setProperty("sumQ1",e.sumQ1)
            edgeToOverride.setProperty("sumProd",e.sumProd)
            edgeToOverride.setProperty("count",e.count)
            graph.commit()
            true
          }
        }
      }
      case (Nil,Nil) => false
    }

  }

  override def all: Future[List[TagsMatch]] = Future{
    val graph = Odb.factory.getNoTx
    val edges = graph.getEdgesOfClass("TagsMatch").asScala
    edges.map(e => getTagsMatch(e.getId)).toList
  }

  override def remove(e: TagsMatch): Future[Boolean] = Future {
    val graph = Odb.factory.getTx
    val tagOneVertices = graph.getVertices("Tags.tag",e.tag1).asScala
    val tagTwoVertices = graph.getVertices("Tags.tag",e.tag2).asScala
    (tagOneVertices,tagTwoVertices) match {
      case (tagOneVertex :: xt, tagTwoVertex :: xo) => {
        val edgesOne = tagOneVertex.getEdges(Direction.OUT,"TagsMatch").asScala
        val edgesTwo = tagTwoVertex.getEdges(Direction.IN,"TagsMatch").asScala
        val edges = edgesOne zip edgesTwo filter ( m => m._1.equals(m._2) ) map ( d => d._1)
        edges match {
          case Nil => false
          case edgeToRemove :: xc => edgeToRemove.remove(); graph.commit(); true
        }
      }
      case (Nil,Nil) => false
    }
  }

  override def save(e: TagsMatch, upsert: Boolean): Future[Boolean] = Future{
    val graph = Odb.factory.getTx
    val tagOneVertices = graph.getVertices("Tags.tag",e.tag1).asScala
    val tagTwoVertices = graph.getVertices("Tags.tag",e.tag2).asScala
    (tagOneVertices,tagTwoVertices) match {
      case (tagOneVertex :: xt, tagTwoVertex :: xo) => {
        val edgesOne = tagOneVertex.getEdges(Direction.OUT,"TagsMatch").asScala
        val edgesTwo = tagTwoVertex.getEdges(Direction.IN,"TagsMatch").asScala
        val edges = edgesOne zip edgesTwo filter ( m => m._1.equals(m._2) ) map ( d => d._1)
        edges match {
          case Nil => {
            val newEdge = graph.addEdge(null,tagOneVertex,tagTwoVertex,"TagsMatch")
            newEdge.setProperty("sum1",e.sum1)
            newEdge.setProperty("sum2",e.sum2)
            newEdge.setProperty("sumQ1",e.sumQ1)
            newEdge.setProperty("sumQ1",e.sumQ1)
            newEdge.setProperty("sumProd",e.sumProd)
            newEdge.setProperty("count",e.count)
            graph.commit()
            true
          }
          case edgeToOverride :: xc => {
            upsert match {
              case false => false
              case true => {
                edgeToOverride.setProperty("sum1",e.sum1)
                edgeToOverride.setProperty("sum2",e.sum2)
                edgeToOverride.setProperty("sumQ1",e.sumQ1)
                edgeToOverride.setProperty("sumQ1",e.sumQ1)
                edgeToOverride.setProperty("sumProd",e.sumProd)
                edgeToOverride.setProperty("count",e.count)
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

  override def find(query: String): Future[List[TagsMatch]] = {
    val graph = Odb.factory.getNoTx
    val res: OrientDynaElementIterable = graph.command(new OCommandSQL(query)).execute()
    val ridEdges: Iterable[Edge] = res.asScala.asInstanceOf[Iterable[Edge]]
    ridEdges.map(rid => getTagsMatch(rid.getId)).toList
  }
}