package recommendationsystem.models.storage

import _root_.recommendationsystem.models.{User, Advice}
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal
import com.orientechnologies.orient.core.sql.OCommandSQL
import com.tinkerpop.blueprints.impls.orient.OrientDynaElementIterable
import com.tinkerpop.blueprints.{Direction, Vertex}
import recommendationsystem.models.{Tag, User, Advice}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._

/**
 * Created by aandelie on 16/10/15.
 */
trait AdvicesDao {
  def count: Future[Long]

  def save(e: Advice, upsert: Boolean = false): Future[Boolean]

  def update(newAdvice: Advice): Future[Boolean]

  def remove(e: Advice): Future[Boolean]

  def all: Future[List[Advice]]

  def find(id: String): Future[List[Advice]]
}

object AdvicesOdb extends AdvicesDao {
  override def count: Future[Long] = Future {
    val graph = Odb.factory.getNoTx
    graph.countVertices("Advices")
  }

  override def update(newAdvice: Advice): Future[Boolean] = Future { synchronized {
    val graph = Odb.factory.getTx
    val adviceVertices = graph.getVertices("Advices.aid",newAdvice.id).asScala
    if (adviceVertices.isEmpty) throw new Exception("Advice not present in database")
    val adviceVertex = adviceVertices.head
    adviceVertex.setProperty("clicked",newAdvice.clicked)
    adviceVertex.setProperty("date",newAdvice.date)
    adviceVertex.setProperty("type",newAdvice.kind)

    //update user
    val adviceUserEdges = adviceVertex.getEdges(Direction.OUT,"AdviceUser").asScala
    if (adviceUserEdges.isEmpty) throw new Exception("oldAdviceUser not present in database")
    val adviceUserEdge = adviceUserEdges.head
    val userVertex = adviceUserEdge.getVertex(Direction.IN)
    val actualUser = userVertex.getProperty("uid").toString
    if(! actualUser.equals(newAdvice.user.id)) {
      val newUserVertices = graph.getVertices("Users.uid",newAdvice.user.id).asScala
      if (newUserVertices.isEmpty) new Exception("newUser not present in database")
      val newUserVertex = newUserVertices.head
      adviceUserEdge.remove()
      graph.addEdge(null,adviceVertex,newUserVertex,"AdviceUser")
    }
    //update tags
    val tagsAdviceEdges = adviceVertex.getEdges(Direction.OUT,"AdviceOutput").asScala
    val tagsAdviceVertex = tagsAdviceEdges.map(v => v.getVertex(Direction.IN))
    val outputEdgesVertices = tagsAdviceEdges zip tagsAdviceVertex
    val tagList = newAdvice.output.map(x => x._1)

    println("Tag presenti nel db")
    tagsAdviceVertex.foreach(x => println(x.getProperty("tag").toString))

    //tags to add into db
    val tagsToAdd = tagList.filter(
      t => ! tagsAdviceVertex.map(x => x.getProperty("tag").toString).contains(t.flatten)
    )
    //adding missing edges
    println("Tag aggiunti:")
    for (t <- tagsToAdd ) {
      println(t.flatten)
      //println(adviceVertex.getProperty("aid"))
      val tagVertices = graph.getVertices("Tags.tag",t.flatten).asScala
      if(tagVertices.isEmpty) throw new Exception("Tag not present in database")
      val tagVertex = tagVertices.head
      graph.addEdge(null,adviceVertex,tagVertex,"AdviceOutput")
    }

    //edges to remove from db
    val edgesToRemove = outputEdgesVertices.filter( oev => !tagList.map(t => t.flatten).contains(oev._2.getProperty("tag")) ).map(ev => ev._1)
    //removing edges
    println("tag rimossi:")
    edgesToRemove.map(x => x.getVertex(Direction.IN)).foreach(v => println(v.getProperty("tag")))
    edgesToRemove.foreach(e => e.remove())
    graph.commit()
    true
  }
  }

  override def all: Future[List[Advice]] = {
    val graph = Odb.factory.getNoTx
    ODatabaseRecordThreadLocal.INSTANCE.set(graph.getRawGraph)
    val vlst: Iterable[Vertex] = graph.getVerticesOfClass("Advices").asScala
    val lst = vlst map (adviceVertex => {
      //get tags of this advice
      val tagsAdviceVertex = adviceVertex.getEdges(Direction.OUT,"AdviceOutput").asScala
        .map(v => v.getVertex(Direction.OUT))
      val output = tagsAdviceVertex map (x => (x.getProperty("tag"),0D)) toList

      //get user information
      val userVertex = adviceVertex.getEdges(Direction.OUT,"AdviceUser").asScala.map(v => v.getVertex(Direction.OUT)).head
      //user must be one
      val userTagsEdge = adviceVertex.getEdges(Direction.OUT,"HoldsTag").asScala
      val userTagsVertex = userTagsEdge map (e => e.getVertex(Direction.OUT))
      val tagList = userTagsEdge zip userTagsVertex map
        (x => (x._2.getProperty("tag"),x._1.getProperty("weight"),x._1.getProperty("lastInsert"))) toList
      val user = User(userVertex.getProperty("uid"),userVertex.getProperty("email"),None,Option(tagList))

      Advice(
        adviceVertex.getProperty("aid"),
        user,output,
        adviceVertex.getProperty("date"),
        adviceVertex.getProperty("clicked"),
        adviceVertex.getProperty("type")
      )
    })
    Future{lst.toList}
  }

  override def remove(e: Advice): Future[Boolean] = {
    val graph = Odb.factory.getTx
    val adviceVertex = graph.getVertices("Advices.aid",e.id).asScala.head
    graph.removeVertex(adviceVertex)
    graph.commit()
    Future{true}
  }

  override def save(e: Advice, upsert: Boolean): Future[Boolean] = Future { synchronized {
    val graph = Odb.factory.getTx
    val adviceVertex = graph.addVertex("Advices", null)
    adviceVertex.setProperty("aid", e.id)
    adviceVertex.setProperty("date", e.date)
    adviceVertex.setProperty("clicked", e.clicked)
    adviceVertex.setProperty("type", e.kind)

    //adding AdviceUser edge
    val userVertices = graph.getVertices("Users.uid", e.user.id).asScala
    if (userVertices.isEmpty) throw new Exception("User not present in database")
    val userVertex = userVertices.head //must be one
    graph.addEdge(null, adviceVertex, userVertex, "AdviceUser")

    //adding AdviceOutput edge
    for ((tag, pt) <- e.output) {
      val tagVertices = graph.getVertices("Tags.tag", tag.flatten).asScala
      if (tagVertices.isEmpty) throw new Exception("Tag not present in database")
      val tagVertex = tagVertices.head
      graph.addEdge(null, adviceVertex, tagVertex, "AdviceOutput")
    }

    graph.commit
    true
  }
  }

  override def find(query: String): Future[List[Advice]] = Future {
    val graph = Odb.factory.getNoTx
    val res: OrientDynaElementIterable = graph.command(new OCommandSQL(query)).execute()
    val ridAdvices: Iterable[Vertex] = res.asScala.asInstanceOf[Iterable[Vertex]]

    def getAdvice(rid: AnyRef): Advice = {
      val adviceVertex = graph.getVertex(rid)
      val userVertex = adviceVertex.getEdges(Direction.OUT, "AdviceUser").asScala
        .map(v => v.getVertex(Direction.OUT)).head
      val user: User = UsersOdb.getUser(userVertex.getId)
      val tagsAdviceVertex = adviceVertex.getEdges(Direction.OUT, "AdviceOutput").asScala
        .map(v => v.getVertex(Direction.OUT))
      val output = tagsAdviceVertex.map(x => (x.getProperty("tag"), 0D)).toList
      Advice(
        adviceVertex.getProperty("aid"),
        user,
        output,
        adviceVertex.getProperty("date"),
        adviceVertex.getProperty("clicked"),
        adviceVertex.getProperty("type")
      )
    }
    ridAdvices.map(rid => getAdvice(rid.getId)).toList
  }
}