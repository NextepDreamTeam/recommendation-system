package recommendationsystem.models.storage

import java.util.Calendar

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import _root_.recommendationsystem.models.{Request, Tag, User}
import org.scalatest.{BeforeAndAfterEach, FunSuite}

import scala.concurrent.duration.Duration
import scala.util.Success

/**
 * Created by bsuieric on 19/10/15.
 */
class RequestsOdb$Test extends FunSuite with BeforeAndAfterEach {

  val t1 = Tag("r1", "r1")
  val t2 = Tag("r2", "r2")
  val user1 = User("r1", Option("utente@test"), None, Option((t1, 0D, 0L) :: List()))
  val user2 = User("r2", Option("utende@test"), None, Option((t1, 0D, 0L) :: List()))

  val t3 = Tag("r3", "3")
  val t4 = Tag("r4", "r4")

  val request1 = Request(
    "req1",
    user1,
    Option((t2) :: List()),
    None,
    Calendar.getInstance().getTimeInMillis
  )
  val request2 = Request(
    "req2",
    user2,
    Option((t2) :: List()),
    None,
    Calendar.getInstance().getTimeInMillis
  )

  override def beforeEach(): Unit = {
    TagsOdb.save(t1)
    TagsOdb.save(t2)
    TagsOdb.save(t3)
    TagsOdb.save(t4)
    UsersOdb.save(user1)
    UsersOdb.save(user2)
  }

  override def afterEach(): Unit = {
    UsersOdb.remove(user1)
    UsersOdb.remove(user2)
    TagsOdb.remove(t1)
    TagsOdb.remove(t2)
    TagsOdb.remove(t3)
    TagsOdb.remove(t4)
  }
/*
  test("RequestOdb save count remove invoked"){
    RequestsOdb.save(request1)
    RequestsOdb.save(request2)
    val fresCount = RequestsOdb.count
    fresCount onComplete{
      case Success(x) => println(x); assert(x==2)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
    RequestsOdb.remove(request1)
    RequestsOdb.remove(request2)
  }


  test("RequestsOdb.all invoked"){
    RequestsOdb.save(request1)
    RequestsOdb.save(request2)
    val fresAll = RequestsOdb.all
    fresAll onComplete{
      case Success(x) => assert(x.size==2)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
    RequestsOdb.remove(request1)
    RequestsOdb.remove(request2)
  }
  */

  test("RequestsOdb.find invoked"){
    RequestsOdb.save(request1)
    RequestsOdb.save(request2)
    val fres = RequestsOdb.find("req2")
    fres onComplete{
      case Success(o) => o match {
        case Some(x) => assert (true)
        case None => assert(false)
      }
    }
    Await.result(fres,Duration(3,))
    RequestsOdb.remove(request1)
    RequestsOdb.remove(request2)
  }

}
