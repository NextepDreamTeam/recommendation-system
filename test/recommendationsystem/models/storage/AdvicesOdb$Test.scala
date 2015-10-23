package recommendationsystem.models.storage

import java.util.Calendar
import java.util.concurrent.TimeUnit

import org.scalatest.{BeforeAndAfterEach, FunSuite}
import recommendationsystem.models.{User, Tag, Advice}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

/**
 * Created by aandelie on 16/10/15.
 */
class AdvicesOdb$Test extends FunSuite with BeforeAndAfterEach {

  val t1 = Tag("tag", "caso")
  val t2 = Tag("caso", "tag")
  val t3 = Tag("test", "test")

  val user1 = User("utenteTest", Option("utente@test"), None, Option((t1, 0D, 0L) :: List()))
  val user2 = User("utendeTest", Option("utende@test"), None, Option((t1, 0D, 0L) :: List()))
  val advice1 = Advice(
    "idDaEliminare",
    user1,
    (t2, 0D) :: List(),
    Calendar.getInstance().getTimeInMillis, clicked = true, "system"
  )
  val advice2 = Advice(
    "idDaEliminaree",
    user2,
    (t1, 0D) ::(t2, 0D) :: List(),
    Calendar.getInstance().getTimeInMillis, clicked = true, "system"
  )

  val minDur = Duration(3,TimeUnit.SECONDS)

  override def beforeEach() {
    Odb.clearDb
    Await.result(TagsOdb.save(t1),minDur)
    Await.result(TagsOdb.save(t2),minDur)
    Await.result(TagsOdb.save(t3),minDur)
    Await.result(UsersOdb.save(user1),minDur)
    Await.result(UsersOdb.save(user2),minDur)
  }

  override def afterEach() {
    Odb.clearDb
  }

  test("AdviceOdb.count is invoked") {
    Await.result(AdvicesOdb.save(advice1),minDur)
    Await.result(AdvicesOdb.save(advice2),minDur)
    val fres = AdvicesOdb.count
    fres onComplete {
      case Success(x) => assert(x==2)
      case Failure(t) => assert(false); println("An error has occured: " + t.getMessage)
    }
    Await.result(fres,minDur)
  }

  test("AdviceOdb.all is invoked") {
    Await.result(AdvicesOdb.save(advice1),minDur)
    Await.result(AdvicesOdb.save(advice2),minDur)
    val fres = AdvicesOdb.all
    fres onComplete {
      case Success(x) => assert(x.size==2)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
    Await.result(fres,minDur)
  }

  test("AdviceOdb.save and remove is invoked") {
    val save = AdvicesOdb.save(advice1)
    save onComplete {
      case Success(x) => assert(x)
      case Failure(t) => assert(false)
    }
    Await.result(save,minDur)
    val find = AdvicesOdb.find(advice1.id)
    find onComplete {
      case Success(o) => o match {
        case Some(x) => assert (true)
        case None => assert(false)
      }
    }
    Await.result(find,minDur)
    val remove = AdvicesOdb.remove(advice1)
    remove onComplete {
      case Success(x) => assert(x)
      case Failure(t) => assert(false)
    }
    Await.result(remove,minDur)
  }

  test("AdviceOdb.update is invoked") {
    Await.result(AdvicesOdb.save(advice1),minDur)
    val advice3 = Advice(
      "idDaEliminare",
      user2,
      (t1, 0D) ::(t3, 0D) :: List(),
      Calendar.getInstance().getTimeInMillis, clicked = true, "system"
    )
    val update = AdvicesOdb.update(advice3)
    update onComplete {
      case Success(x) => assert(x)
      case Failure(t) => println("An error has occured: " + t.getMessage); assert(false)
    }
    Await.result(update,minDur)
  }

}
