package recommendationsystem.models.storage

import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory
import scala.collection.JavaConverters._

/**
 * Created by aandelie on 14/10/15.
 */
object Odb {
  val factory = new OrientGraphFactory("remote:localhost:2424/recommendation-system","root","root").setupPool(1,10)

  def clearDb = {
    val graph = factory.getNoTx
    val vertices = graph.getVertices().asScala.map(v => v.remove())
  }
}
