package recommendationsystem.models.storage

import recommendationsystem.models.User
import com.tinkerpop.blueprints._
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by bsuieric on 15/10/15.
 */
trait UsersDao {
  def count: Future[Long]

  def update(e: User): Future[Boolean]

  def save(e: User, upsert: Boolean = false): Future[Boolean]

  def remove(e: User): Future[Boolean]

  def all: Future[List[User]]

  def find(id: String): Future[Option[User]]

}

object UsersOdb extends UsersDao {

  override def count: Future[Long] = Future {
    val graph = Odb.factory.getNoTx
    graph.countVertices("Users")
  }

  override def save(e: User, upsert: Boolean): Future[Boolean] = Future {
    val graph = Odb.factory.getTx()
    val v = graph.addVertex("Users", null)
    v.setProperty("uid", e.id)
    e.email match {
      case Some(mail) => v.setProperty("email", mail)
      case None => {} //do nothing
    }
    //Inserting edges in HoldsTag
    e.tags match {
      case Some(tagList) => {
        //tags must be present in database; creating edges from User to Tag
        tagList foreach
          (t => {
            val tagVertex = graph.getVertices("Tags.tag", t._1.flatten).asScala.head
            val userHoldsTagEdge = graph.addEdge(null,v,tagVertex,"HoldsTag")
            userHoldsTagEdge.setProperty("weight",t._2)
            userHoldsTagEdge.setProperty("lastInsert",t._3)
          })
      }
      case None => {}
    }
    graph.commit()
    true
  }

  override def remove(e: User): Future[Boolean] = Future {
    val graph = Odb.factory.getTx
    val userVertex = graph.getVertices("Users.uid", e.id).asScala.head
    graph.removeVertex(userVertex)
    graph.commit()
    true
  }

  override def all: Future[List[User]] = Future {
    val graph = Odb.factory.getNoTx
    val usersListVertex = graph.getVerticesOfClass("Users").asScala.toList
    val usersList: List[User] = usersListVertex map (userVertex => {
      val userTagsEdge = userVertex.getEdges(Direction.OUT,"HoldsTag").asScala
      val userTagsVertex = userTagsEdge map (e => e.getVertex(Direction.OUT))
      val tagList = userTagsEdge zip userTagsVertex map
        (x => (x._2.getProperty("tag"),x._1.getProperty("weight"),x._1.getProperty("lastInsert"))) toList;
      User(userVertex.getProperty("uid"),Option(userVertex.getProperty("email")),None,Option(tagList))
    })
    usersList
  }

  override def find(id: String): Future[Option[User]] = Future {
    val graph = Odb.factory.getNoTx()
    val usersListVertex = graph.getVertices("Users.uid", id) .asScala.toList
    usersListVertex match {
      case userVertex :: xs => {
        val userTagsEdge = userVertex.getEdges(Direction.OUT,"HoldsTag").asScala
        val userTagsVertex = userTagsEdge map (e => e.getVertex(Direction.OUT))
        val tagList = userTagsEdge zip userTagsVertex map
          (x => (x._2.getProperty("tag"),x._1.getProperty("weight"),x._1.getProperty("lastInsert"))) toList;
        Option(User(userVertex.getProperty("uid"),Option(userVertex.getProperty("email")),None,Option(tagList)))
      }
      case Nil => None
    }

  }

  //da controllare
  override def update(e: User): Future[Boolean] = Future {
    val graph = Odb.factory.getTx
    val usersListVertex = graph.getVertices("Users.uid", e.id) .asScala.toList
    usersListVertex match {
      case userVertex :: xs => {
        //updating user field
        e.email match {
          case Some(mail) => userVertex.setProperty("email",mail)
          case None => {}
        }
        val userTagsEdge = userVertex.getEdges(Direction.OUT,"HoldsTag").asScala.toList
        e.tags match {
          case Some(tl) => {
            tl match {
              case hts :: ts => { //maybe there's tags to add
                val userTagsVertex = userTagsEdge map (e => e.getVertex(Direction.OUT))
                val edgesToAdd = tl.filter(
                  t => ! userTagsVertex.map(x => x.getProperty("tag").toString).contains(t._1.flatten)
                )
                edgesToAdd.map(
                  e => {
                    val tagVertex = graph.getVertices("Tags.tag",e._1.flatten).asScala.head
                    val holdsTagEdge = graph.addEdge(null,userVertex,tagVertex,"HoldsTag")
                    holdsTagEdge.setProperty("lastInsert",e._3)
                    holdsTagEdge.setProperty("weight",e._2)
                  }
                )
                val tagList = userTagsVertex zip userTagsEdge //tags into db
                val edgesToRemove = tagList.filter(
                  ove => !tl.map(t => t._1.flatten).contains(ove._1.getProperty("tag"))
                  )
                edgesToRemove.map( e => e._2.remove())
              }
              case Nil => userTagsEdge.map(e => e.remove())
            }
          }
          case None => userTagsEdge.map(e => e.remove())
        }
        graph.commit()
        true
      }
      case Nil => false
    }
  }
}
