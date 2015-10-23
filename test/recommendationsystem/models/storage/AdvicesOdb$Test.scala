/*package recommendationsystem.models.storage

import java.util.Calendar

import org.scalatest.{BeforeAndAfterEach, FunSuite}
import recommendationsystem.models.{User, Tag, Advice}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
 * Created by aandelie on 16/10/15.
 */
class AdvicesOdb$Test extends FunSuite with BeforeAndAfterEach {

  val t1 = Tag("tag", "caso")
  val t2 = Tag("caso", "tag")
  val user1 = User("utenteTest", Option("utente@test"), None, Option((t1, 0D, 0L) :: List()))
  val user2 = User("utendeTest", Option("utende@test"), None, Option((t1, 0D, 0L) :: List()))
  val advice1 = Advice(
    "idDaEliminare",
    user1,
    (t2, 0D) :: List(),
    Calendar.getInstance().getTimeInMillis, true, "system"
  )
  val advice2 = Advice(
    "idDaEliminaree",
    user2,
    (t1, 0D) ::(t2, 0D) :: List(),
    Calendar.getInstance().getTimeInMillis, true, "system"
  )

  override def beforeEach() {
    TagsOdb.save(t1)
    TagsOdb.save(t2)
    UsersOdb.save(user1)
    UsersOdb.save(user2)
  }

  override def afterEach() {
    UsersOdb.remove(user1)
    UsersOdb.remove(user2)
    TagsOdb.remove(t1)
    TagsOdb.remove(t2)
  }

  test("AdviceOdb.count is invoked") {
    AdvicesOdb.save(advice1)
    AdvicesOdb.save(advice2)
    val fres = AdvicesOdb.count
    fres onComplete {
      case Success(x) => assert(x==2)
      case Failure(t) => assert(false); println("An error has occured: " + t.getMessage)
    }
    AdvicesOdb.remove(advice1)
    AdvicesOdb.remove(advice2)
  }

  test("AdviceOdb.all is invoked") {
    AdvicesOdb.save(advice1)
    AdvicesOdb.save(advice2)
    val fres = AdvicesOdb.all
    fres onComplete {
      case Success(x) => assert(x.size==2)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
    AdvicesOdb.remove(advice1)
    AdvicesOdb.remove(advice2)
  }

  test("AdviceOdb.save and remove is invoked") {
    val fResSave = AdvicesOdb.save(advice1)
    fResSave onComplete {
      case Success(x) => assert(x)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
    val fResFind = AdvicesOdb.find(advice1.id)
    fResFind onComplete {
      case Success(o) => o match {
        case Some(x) => assert (true)
        case None => assert(false)
      }
    }
    val fResRemove = AdvicesOdb.remove(advice1)
    fResRemove onComplete {
      case Success(x) => assert(x)
      case Failure(t) => println("An error has occured: " + t.getMessage); assert(false)
    }
  }

  test("AdviceOdb.update is invoked") {
    AdvicesOdb.save(advice1)
    val advice3 = Advice(
      "idDaEliminare",
      user2,
      (t1, 0D) ::(t2, 0D) :: List(),
      Calendar.getInstance().getTimeInMillis, true, "system"
    )
    val fResUpdate = AdvicesOdb.update(advice3)
    fResUpdate onComplete {
      case Success(x) => assert(x)
      case Failure(t) => println("An error has occured: " + t.getMessage); assert(false)
    }
    AdvicesOdb.remove(advice3)
  }

}
*/