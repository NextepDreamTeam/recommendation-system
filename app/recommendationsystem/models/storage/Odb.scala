package recommendationsystem.models.storage

import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory

/**
 * Created by aandelie on 14/10/15.
 */
object Odb {
  val factory = new OrientGraphFactory("remote:localhost:2424/recommendation-system").setupPool(1,10);
}
