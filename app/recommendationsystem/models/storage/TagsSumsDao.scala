package recommendationsystem.models.storage

import com.orientechnologies.orient.core.sql.OCommandSQL
import com.tinkerpop.blueprints.impls.orient.OrientDynaElementIterable
import recommendationsystem.algorithms.TagSum
import com.tinkerpop.blueprints.{Edge, Direction, Vertex}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._

/**
 *
 */
trait TagsSumsDao {
  def count: Future[Long]

  def save(e: TagSum, upsert: Boolean = false): Future[Boolean]

  def update(newTagSum: TagSum): Future[Boolean]

  def remove(e: TagSum): Future[Boolean]

  def all: Future[List[TagSum]]

  def find(query: String): Future[List[TagSum]]
}

object TagsSumsOdb extends TagsSumsDao {
  def getTagSum(rid: AnyRef): TagSum = {
    val graph = Odb.factory.getNoTx
    val edgeTagSum = graph.getEdge(rid)
    val tagVertex = edgeTagSum.getVertex(Direction.OUT)
    val fieldsTagSum = edgeTagSum.getProperties
    val fieldTag = tagVertex.getProperties
    TagSum(
      fieldTag.get("tag").asInstanceOf[String],
      fieldsTagSum.get("sum").asInstanceOf[Double],
      fieldsTagSum.get("sumQ").asInstanceOf[Double]
    )
  }

  override def count: Future[Long] = Future {
    val graph = Odb.factory.getNoTx
    graph.countEdges("TagsSums")
  }

  override def update(e: TagSum): Future[Boolean] = Future {
    val graph = Odb.factory.getTx
    val tagVertices = graph.getVertices("Tags.tag",e._id).asScala
    tagVertices match {
      case tagVertex :: xs => {
        val tagSumEdges = tagVertex.getEdges(Direction.OUT,"TagsSums").asScala
        tagSumEdges match {
          case Nil => false
          case tagSumEdge :: xd => {
            tagSumEdge.setProperty("sum",e.sum)
            tagSumEdge.setProperty("sumQ",e.sumQ)
            graph.commit()
            true
          }
        }
      }
      case Nil => false
    }
  }

  override def all: Future[List[TagSum]] = Future {
    val graph = Odb.factory.getNoTx
    val edgesTagsSums = graph.getEdgesOfClass("TagsSums").asScala
    edgesTagsSums.map(e => getTagSum(e.getId)).toList
  }

  override def remove(e: TagSum): Future[Boolean] = Future {
    val graph = Odb.factory.getTx
    val tagVertices = graph.getVertices("Tags.tag",e._id).asScala
    if(tagVertices.isEmpty) throw new Exception("Tag not present in database")
    val tagSumEdge = tagVertices.head.getEdges(Direction.OUT,"TagsSums").asScala
    tagSumEdge.map(e => e.remove()) //should be one
    true
  }

  override def save(e: TagSum, upsert: Boolean): Future[Boolean] = Future {
    val graph = Odb.factory.getTx
    val tagVertices = graph.getVertices("Tags.tag",e._id).asScala
    tagVertices match {
      case tagVertex :: xs => {
        val tagSumEdges = tagVertex.getEdges(Direction.OUT,"TagsSums").asScala
        tagSumEdges match {
          case Nil => {
            val tagSumEdge = graph.addEdge(null,tagVertex,tagVertex,"TagsSums")
            tagSumEdge.setProperty("sum",e.sum)
            tagSumEdge.setProperty("sumQ",e.sumQ)
            graph.commit()
            true
          }
          case tagSumEdge :: xd => {
            upsert match {
              case true => {
                tagSumEdge.setProperty("sum",e.sum)
                tagSumEdge.setProperty("sumQ",e.sumQ)
                graph.commit()
                true
              }
              case false => false
            }
          }
        }
      }
      case Nil => false
    }
  }

  override def find(query: String): Future[List[TagSum]] = Future {
    val graph = Odb.factory.getNoTx
    val res: OrientDynaElementIterable = graph.command(new OCommandSQL(query)).execute()
    val ridEdges: Iterable[Edge] = res.asScala.asInstanceOf[Iterable[Edge]]
    ridEdges.map(rid => getTagSum(rid.getId)).toList
  }
}
