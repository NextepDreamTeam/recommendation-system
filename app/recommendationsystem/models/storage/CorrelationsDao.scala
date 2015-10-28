package recommendationsystem.models.storage

import com.orientechnologies.orient.core.sql.OCommandSQL
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.orient.OrientDynaElementIterable
import recommendationsystem.models.Correlation

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._

/**
 * Created by aandelie on 28/10/15.
 */
trait CorrelationsDao {
  def count: Future[Long]

  def save(e: Correlation, upsert: Boolean = false): Future[Boolean]

  def update(newAdvice: Correlation): Future[Boolean]

  def remove(e: Correlation): Future[Boolean]

  def all: Future[List[Correlation]]

  def find(query: String): Future[List[Correlation]]
}

object CorrelationsOdb extends CorrelationsDao {

  def getCorrelation(rid: AnyRef): Correlation = {
    val graph = Odb.factory.getNoTx
    val correlationVertex = graph.getVertex(rid)
    val fields = correlationVertex.getProperties
    val tag = fields.get("tag").asInstanceOf[String].split(":+").toList
    Correlation(
      tag.get(0),
      tag.get(1),
      fields.get("average").asInstanceOf[Double],
      fields.get("weight").asInstanceOf[Double])
  }

  override def count: Future[Long] = Future {
    val graph = Odb.factory.getNoTx
    graph.countVertices("Correlations")
  }

  override def update(newCorrelation: Correlation): Future[Boolean] = Future {
    val graph = Odb.factory.getTx
    val correlationsVertices = graph.getVertices("Correlations.tag",newCorrelation.flatten).asScala
    if(correlationsVertices.isEmpty) throw new Exception("Correlation not present in db")
    val correlationVertex = correlationsVertices.head
    correlationVertex.setProperty("average",newCorrelation.average)
    correlationVertex.setProperty("weight",newCorrelation.weight)
    graph.commit()
    true
  }

  override def all: Future[List[Correlation]] = Future {
    val graph = Odb.factory.getNoTx
    val vertices = graph.getVerticesOfClass("Correlations").asScala
    vertices.map( v => getCorrelation(v.getId)).toList
  }

  override def remove(e: Correlation): Future[Boolean] = Future {
    val graph = Odb.factory.getTx
    val correlationsVertices = graph.getVertices("Correlations.tag", e.flatten).asScala
    if(correlationsVertices.isEmpty) throw new Exception("Correlation not present in db")
    val correlationVertex = correlationsVertices.head
    correlationVertex.remove()
    graph.commit()
    true
  }

  override def save(e: Correlation, upsert: Boolean): Future[Boolean] = Future {
    val graph = Odb.factory.getTx
    val correlationsVertices = graph.getVertices("Correlations.tag", e.flatten).asScala
    correlationsVertices match {
      case correlationVertex :: cs => {
        upsert match {
          case true => {
            correlationVertex.setProperty("average",e.average)
            correlationVertex.setProperty("weight",e.weight)
            graph.commit()
            true
          }
          case false => false
        }
      }
      case Nil => {
        val correlationVertex = graph.addVertex("Correlations", null)
        correlationVertex.setProperty("tag", e.flatten)
        correlationVertex.setProperty("average", e.average)
        correlationVertex.setProperty("weight", e.weight)
        graph.commit()
        true
      }
    }
  }

  override def find(query: String): Future[List[Correlation]] = Future {
    val graph = Odb.factory.getNoTx
    val res: OrientDynaElementIterable = graph.command(new OCommandSQL(query)).execute()
    val ridCorrelations: Iterable[Vertex] = res.asScala.asInstanceOf[Iterable[Vertex]]
    ridCorrelations.map(r => getCorrelation(r)).toList
  }
}