package recommendationsystem.models

import scala.concurrent.Future
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor._
import akka.util.Timeout
import akka.pattern._
import scala.concurrent.duration._
import play.api.libs.iteratee.Iteratee
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.JsValue
import scala.util._
import scala.concurrent.Promise
import play.api.Logger
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import recommendationsystem.algorithms.Similarity

object Recommender {
  implicit val timeout = Timeout(10 seconds)

  val system = ActorSystem("Recommender")
  val actor = system.actorOf(Props[Master], "master")

  /**
   * Take a request and create an Future advice
   */
  def advise(request: Request): Future[Advice] = {
    //val id = "" + java.util.UUID.randomUUID.toString
    val input = request.tags
    val output = (request.tags).getOrElse(List())

    /** Load user */
    val futureUser = request.user match {
      case User(id, Some(email), _, _) =>
        val query = "select from Users where email in "+Json.arr(email)
        Users.find(query)
      case User(id, None, _, _) =>
        val query = "select from Users where uid in "+Json.arr(id)
        Users.find(query)
    }

    val futureResult = Promise[Advice];
    futureUser.foreach {
      /*L'utente ?? presente*/
      case user :: xs => user.tags match {
        /*L'utente ha tag*/
        case Some(tags) => (actor ? Elaborate(request.id, tags)).mapTo[List[(Tag, Double)]] andThen {
          case Success(Nil) =>
            Logger.debug(s"Empty recommendation")
            defaultAdvise(request).onComplete {
              case Success(a) => futureResult.success(a)
              case Failure(e) => futureResult.failure(e)
            }
          case Success(result) =>
            Logger.debug(s"RESULT: $result")
            futureResult.success(Advice(request.id, request.user, result /*result.map(_._1).distinct*/ ))
          case Failure(e) =>
            Logger.debug(s"$e")
            futureResult.failure(e)
        } recover { case _ => Logger.debug("Recover"); List() }
        // Accumulator ! Accumulate(request.id, futureResult)
        /*L'utente non ha tag*/
        case None => defaultAdvise(request).onComplete {
          case Success(a) => futureResult.success(a)
          case Failure(e) => futureResult.failure(e)
        }
      }
      /*L'utente non ?? presente*/
      case Nil => defaultAdvise(request).onComplete {
        case Success(a) => futureResult.success(a)
        case Failure(e) => futureResult.failure(e)
      }
    }

    futureResult.future
  }

  /**
   * Return a default advice
   */
  def defaultAdvise(request: Request): Future[Advice] = {
    val futureTags = Tags.all//.limit(5).toList
    futureTags.map(listTags => Advice(request.id, request.user, listTags.take(5).map((_, 0D)), "default"))
  }
}

/**
 * Message of actors
 */
sealed trait AdviceMessage
case class Elaborate(id: String, tags: List[(Tag, Double, Long)]) extends AdviceMessage
case class Calculate(id: String, tag: (Tag, Double, Long)) extends AdviceMessage
case class Completed(id: String, tags: List[(String, Double, Double)]) extends AdviceMessage
case class Failed(id: String) extends AdviceMessage
case class TimeExpired(id: String) extends AdviceMessage

/**
 * Master Actor, this actor create and manage other actor, also accumulate the results of his child actors
 */
class Master extends Actor with akka.actor.ActorLogging {

  /**
   * Create an actor with specified name
   * @param name of the actor
   * @return ActorRef object
   */
  def createActor(name: String): ActorRef = context.actorOf(Props[TagActor], name)

  /** name -> worker*/
  var actorsMap = Map.empty[String, ActorRef]
  /** id -> results*/
  var results = Map.empty[String, List[(Tag, Double, Double)]]
  /** id -> (schedule, nOfResponse)*/
  var waiting = Map.empty[String, (akka.actor.Cancellable, Int)]
  /** id -> sender*/
  var pending = Map.empty[String, ActorRef]
  /***/
  val timeout = 10 second

  /**
   * 
   */
  def actorsTag(name: String) =
    if (actorsMap.contains(name))
      actorsMap(name)
    else {
      actorsMap += name -> createActor(name)
      actorsMap(name)
    }

  /**
   *
   */
  def accumulateResult(id: String, tags: List[(Tag, Double, Double)]) = {
    /** devo raggrupare per nome/id tag e sommare */
    if (!results.contains(id)) results += (id -> tags)
    else results += (id -> (results(id) ++ tags))

    waiting.get(id).map { old =>
      if (old._2 > 1) waiting += (id -> (old._1, old._2 - 1))
      else resultCompleted(id)
    }
  }

  /**
   *
   */
  def resultCompleted(id: String) = {
    Logger.debug("resultCompleted WAITING VAR: " + waiting.get(id))
    waiting -= id
    removeTimeout(id)
    results.get(id) match {
      case Some(Nil) =>
        pending(id) ! Nil
      case Some(result) =>
        val map = result.groupBy(value => value._1.flatten)
        Logger.debug(s"Result - $map")
        val res = map.map { value =>
          val aux = (value._1, value._2.foldLeft(0D, 0D)((acc, v) => (acc._1 + v._2, acc._2 + v._3)))
          if (aux._2._2 == 0) (Tag(value._1, None), 0D)
          else (Tag(value._1, None), aux._2._1 / aux._2._2)
        }

        pending(id) ! (res.toList /*.filter(_._2 != 0)*/ )
      case None =>
        pending(id) ! Nil
    }
    results -= id
    pending -= id

  }
  /**
   *
   */
  def setTimeout(id: String, requests: Int) = {
    val cancellable = context.system.scheduler.scheduleOnce(timeout) { self ! TimeExpired(id) }
    waiting += id -> (cancellable, requests)
    Logger.debug(s"WAITING START: $waiting")
  }

  /**
   *
   */
  def removeTimeout(id: String) = waiting.get(id).map(_._1.cancel)

  /**
   * 
   */
  def receive = {
    case Elaborate(id, tags) =>
      pending += id -> sender
      tags.map { tag =>
        val actorName = (tag._1.id)
        actorsTag(actorName) ! Calculate(id, tag)
        setTimeout(id, tags.size)
      }
    case c @ Completed(id, tags) =>
      Logger.debug(s"$c")
      val tagsTransformed = tags.map(t => (Tag(t._1, None), t._2, t._3))
      accumulateResult(id, tagsTransformed)
    case Failed(id) =>
      log.debug(s"Failed calculate $id")
    case TimeExpired(id) =>
      if (pending.contains(id)) resultCompleted(id)
  }
}


/**
 * Smallest worker actor, only calculate his similar tags, and send the list to the master actor
 */
class TagActor extends Actor with akka.actor.ActorLogging {
  /**
   *
   */
  def receive = {
    case Calculate(id, (tag, weight, l)) =>
      val query = "select from TagsSimilarity where outV() IN (select from Tags where tag = \"" + tag.id + "\")"
      val equalsTag = Similarity.find(query)//.toEnum
      def tagIteratee: Iteratee[Similarity, List[(String, Double, Double)]] = Iteratee.fold(List[(String, Double, Double)]()) { (acc, json) =>
        //log.debug(s"tagIteratee $acc json: $json")
        val eq = json.eq //(json \ "eq").as[Double]
        val n = weight * eq //- (quante volte compaiono questi due tag) + (p * qualcosa)
        (json.tag2Name, n, eq) :: acc
      }
      val keepSender = sender
      //TODO
      equalsTag.run(tagIteratee).onComplete {
        case Success(list) => keepSender ! Completed(id, list)
        case Failure(e) => keepSender ! Failed(id)
      }
  }
}
