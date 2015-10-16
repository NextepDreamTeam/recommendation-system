package recommendationsystem.models.storage

import _root_.recommendationsystem.models.User
import org.scalatest.{BeforeAndAfterEach, FunSuite}

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by bsuieric on 15/10/15.
 */
class UsersOdb$Test extends FunSuite with BeforeAndAfterEach {

  test("UsersOdb.count method invoked") {
    val fres = UsersOdb.count
    fres onComplete {
      case Success(count) => println(count); assert(true);
      case Failure(t) => println("An error has occured : " + t.getMessage)
    }
  }

  test("UsersOdb.save and remove method invoked testSave") {
    val test = User("12345", Option("bgd@hot.it"))
    val fresSave = UsersOdb.save(test)
    fresSave onComplete{
      case Success(b) => assert(b)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
    val fresDelete = UsersOdb.remove(test)
    fresDelete onComplete{
      case Success(b) => assert(b)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
  }

  test("UsersOdb.all method invoked testAll") {
    val u1 = User("primo")
    val u2 = User("secondo")
    val u3 = User("terzo")
    val uList = u1 :: u2 :: u3 :: List()
    uList map (x => UsersOdb.save(x)) map(
      t => t onComplete {
        case Success(r) => assert(r)
        case Failure(t) => println("An error has occured: " + t.getMessage)
      }
      )
    UsersOdb.all onComplete {
      case Success(list) => assert(list.size == 3)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
    uList map (x => UsersOdb.remove(x)) map (
      t => t onComplete {
       case Success(r) => assert(r)
       case Failure(t) => println("An error has occured: " + t.getMessage)
      }
     )
    assert(true)
  }



}
