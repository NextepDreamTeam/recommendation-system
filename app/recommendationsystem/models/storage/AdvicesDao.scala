/*package recommendationsystem.models.storage

import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal
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

  def find(id: String): Future[Option[Advice]]
}

object AdvicesOdb extends AdvicesDao {
  override def count: Future[Long] = {
    val graph = Odb.factory.getNoTx
    val count = graph.countVertices("Advices")
    graph.shutdown()
    Future{count}
  }

  override def update(newAdvice: Advice): Future[Boolean] = {
    val graph = Odb.factory.getTx
    val adviceVertex = graph.getVertices("Advices.aid",newAdvice.id).asScala.head
    adviceVertex.setProperty("date",newAdvice.date)
    adviceVertex.setProperty("type",newAdvice.kind)
    //update user
    val adviceUserEdge = adviceVertex.getEdges(Direction.OUT,"AdviceUser").asScala.head
    val userVertex = adviceUserEdge.getVertex(Direction.IN)
    val actualUser = userVertex.getProperty("uid").toString
    if(! actualUser.equals(newAdvice.user.id)) {
      val newUserVertex = graph.getVertices("Users.uid",newAdvice.user.id).asScala.head
      adviceUserEdge.remove()
      graph.addEdge(null,adviceVertex,newUserVertex,"AdviceUser")
    }
    //update tags
    val tagsAdviceEdges = adviceVertex.getEdges(Direction.OUT,"AdviceOutput").asScala
    val tagsAdviceVertex = tagsAdviceEdges.map(v => v.getVertex(Direction.IN))
    val outputEdgesVertices = tagsAdviceEdges zip tagsAdviceVertex
    val tagList = newAdvice.output.map(x => x._1)

    println("Tag presenti nel db")
    tagsAdviceVertex.map(x => println(x.getProperty("tag").toString))

    //tags to add into db
    val tagsToAdd = tagList.filter(
      t => ! tagsAdviceVertex.map(x => x.getProperty("tag").toString).contains(t.flatten)
    )
    //adding missing edges
    println("Tag aggiunti:")
    for (t <- tagsToAdd ) {
      println(t.flatten)
      //println(adviceVertex.getProperty("aid"))
      val tagVertex = graph.getVertices("Tags.tag",t.flatten).asScala.head
      graph.addEdge(null,adviceVertex,tagVertex,"AdviceOutput")
    }

    //edges to remove from db
    val edgesToRemove = outputEdgesVertices.filter( oev => !tagList.map(t => t.flatten).contains(oev._2.getProperty("tag")) ).map(ev => ev._1)
    //removing edges
    println("tag rimossi:")
    edgesToRemove.map(x => x.getVertex(Direction.IN)).map(v => println(v.getProperty("tag")))
    edgesToRemove.map(e => e.remove())
    graph.commit()
    graph.shutdown()
    Future{true}
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
        adviceVertex.getProperty("type")
      )
    })
    graph.shutdown
    Future{lst.toList}
  }

  override def remove(e: Advice): Future[Boolean] = {
    val graph = Odb.factory.getTx
    val adviceVertex = graph.getVertices("Advices.aid",e.id).asScala.head
    graph.removeVertex(adviceVertex)
    graph.commit()
    Future{true}
  }

  override def save(e: Advice, upsert: Boolean): Future[Boolean] = {
    val graph = Odb.factory.getTx
    val adviceVertex = graph.addVertex("Advices",null)
    adviceVertex.setProperty("aid",e.id)
    adviceVertex.setProperty("date",e.date)
    adviceVertex.setProperty("type",e.kind)
    //adding AdviceUser edge
    val userVertex = graph.getVertices("Users.uid",e.user.id).asScala.head //must be one
    val adviceUserEdge = graph.addEdge(null,adviceVertex,userVertex,"AdviceUser")
    //adding AdviceOutput edge
    for ( (tag, pt) <- e.output ) {
      val tagVertex = graph.getVertices("Tags.tag",tag.flatten).asScala.head
      val adviceOutputEdge = graph.addEdge(null,adviceVertex,tagVertex,"AdviceOutput")
    }
    graph.commit
    graph.shutdown
    Future{true}
  }

  override def find(id: String): Future[Option[Advice]] = {
    val graph = Odb.factory.getNoTx
    val adviceVertices = graph.getVertices("Advices.aid",id).asScala
    if(adviceVertices.isEmpty)
      Future{None}
    else {
      val adviceVertex = adviceVertices.head //must be one
      val userVertex = adviceVertex.getEdges(Direction.OUT,"AdviceUser").asScala
          .map(v => v.getVertex(Direction.OUT)).head
      val tagsAdviceVertex = adviceVertex.getEdges(Direction.OUT,"AdviceOutput").asScala
        .map(v => v.getVertex(Direction.OUT))
      val output = tagsAdviceVertex map (x => (x.getProperty("tag"),0D)) toList
      val advice = Advice(
        adviceVertex.getProperty("aid"),
        userVertex.getProperty("uid"),
        output,
        adviceVertex.getProperty("date"),
        adviceVertex.getProperty("type")
      )
      Future{Option(advice)}
    }
  }
}*/