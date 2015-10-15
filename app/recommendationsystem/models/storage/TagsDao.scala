package recommendationsystem.models.storage

import com.orientechnologies.orient.core.command.OCommandRequest
import com.orientechnologies.orient.core.sql.OCommandSQL
import com.tinkerpop.blueprints.impls.orient.{OrientVertex, OrientDynaElementIterable}

//import recommendationsystem.models.Tag
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.collection.JavaConverters._
/**
 * Created by aandelie on 14/10/15.
 */
trait TagsDao {
  def count: Future[Long]

  //def save(e: Tag, upsert: Boolean = false): Future[Boolean]

  //def update(oldTag: Tag, newTag: Tag): Future[Boolean] //it will be used???

  //def remove(e: Tag): Future[Boolean]

  //def all: Future[List[Tag]]

  //def find(id: String): Future[List[Tag]]
}


object TagsOdb extends TagsDao{
  override def count: Future[Long] = {
    val graph = Odb.factory.getNoTx
    val c: OrientDynaElementIterable = graph.command(new OCommandSQL("select count(*) from Tags")).execute()
    val it = c.iterator().asScala
    var count = 0L
    for (e <- it) count = e.asInstanceOf[OrientVertex].getProperty("count")
    graph.shutdown()
    Future {count}
  }

  //override def update(oldTag: Tag, newTag: Tag): Future[Boolean] = ???

  //override def all: Future[List[Tag]] = ???

  //it will be used???
  //override def remove(e: Tag): Future[Boolean] = ???

  //override def save(e: Tag, upsert: Boolean): Future[Boolean] = ???

  //override def find(id: String): Future[List[Tag]] = ???
}