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

  test("UsersOdb.save method invoked testSave") {
    val test = User("1234", Option("bgd@hot.it"))
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

}
