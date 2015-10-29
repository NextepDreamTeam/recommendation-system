package recommendationsystem.models.storage

import java.util.Calendar
import java.util.concurrent.TimeUnit

import _root_.recommendationsystem.models.{Request, Tag, User}
import org.scalatest.{BeforeAndAfterEach, FunSuite}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.Success

/**
 * Created by bsuieric on 19/10/15.
 */
class RequestsOdb$Test extends FunSuite with BeforeAndAfterEach {

  val t1 = Tag("r1", "r11")
  val t2 = Tag("r2", "r22")

  val user1 = User("u1", Option("utente@test"), None, Option((t1, 0D, 0L) :: List()))
  val user2 = User("u2", Option("utende@test"), None, Option((t1, 0D, 0L) :: List()))

    val request1 = Request(
      "req1",
      user1,
      Option((t1) :: (t2) :: List()),
      None,
      Calendar.getInstance().getTimeInMillis
    )
    val request2 = Request(
      "req2",
      user2,
      Option((t1) :: (t2) :: List()),
      None,
      Calendar.getInstance().getTimeInMillis
    )


  override def beforeEach() {
    Odb.clearDb
    val addTag1 = TagsOdb.save(t1)
    Await.result(addTag1,Duration(3,TimeUnit.SECONDS))
    val addTag2 = TagsOdb.save(t2)
    Await.result(addTag1,Duration(3,TimeUnit.SECONDS))

    val addU1 = UsersOdb.save(user1)
    Await.result(addU1,Duration(3,TimeUnit.SECONDS))
    val addU2 = UsersOdb.save(user2)
    Await.result(addU2,Duration(3,TimeUnit.SECONDS))
  }

  override def afterEach() {
    Odb.clearDb
  }

  test("RequestsOdb.find invoked"){
    val addR1 = RequestsOdb.save(request1)
    Await.result(addR1,Duration(3,TimeUnit.SECONDS))
    val addR2 = RequestsOdb.save(request2)
    Await.result(addR2,Duration(3,TimeUnit.SECONDS))
    val fres = RequestsOdb.find("req2")
    fres onComplete{
      case Success(o) => o match {
        case x :: xs => Odb.clearDb; assert (true)
        case Nil => Odb.clearDb; assert(false)
      }
    }
    Await.result(fres,Duration(3,TimeUnit.SECONDS))
  }

/*
  test("RequestOdb save count remove invoked"){
    val addR1 = RequestsOdb.save(request1)
    Await.result(addR1,Duration(3,TimeUnit.SECONDS))
    val addR2 = RequestsOdb.save(request2)
    Await.result(addR2,Duration(3,TimeUnit.SECONDS))
    val fresCount = RequestsOdb.count
    fresCount onComplete{
      case Success(x) => println(x); assert(x==2)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
    Await.result(fresCount,Duration(3,TimeUnit.SECONDS))

  }
*/
/*
  test("RequestsOdb.all invoked"){
    val addR1 = RequestsOdb.save(request1)
    Await.result(addR1,Duration(3,TimeUnit.SECONDS))
    val addR2 = RequestsOdb.save(request2)
    Await.result(addR2,Duration(3,TimeUnit.SECONDS))
    val fresAll = RequestsOdb.all
    fresAll onComplete{
      case Success(x) => assert(x.size==2)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
    Await.result(fresAll,Duration(3,TimeUnit.SECONDS))

    Odb.clearDb
  }
  */

}
