package recommendationsystem.models.storage

import java.util.concurrent.TimeUnit

import recommendationsystem.models.{User, Tag}
import org.scalatest.{BeforeAndAfterEach, FunSuite}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by bsuieric on 15/10/15.
 */
class UsersOdb$Test extends FunSuite with BeforeAndAfterEach {

  val tagTest = Tag("tag","test")

  override def beforeEach() {
    TagsOdb.save(tagTest)
  }

  override def afterEach() {
    TagsOdb.remove(tagTest)
  }

  test("UsersOdb.count method invoked") {
    val fres = UsersOdb.count
    fres onComplete {
      case Success(count) => println(count); assert(true);
      case Failure(t) => assert(false)
    }
    Await.result(fres,Duration(3,TimeUnit.SECONDS))
  }

  /*test("UsersOdb.save, find and remove method invoked testSave") {
    val test = User("12345", Option("bgd@hot.it"), None, Option((tagTest,42D,42L)::List()))
    val fresSave = UsersOdb.save(test)
    fresSave onComplete{
      case Success(b) => assert(b)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
    val fresFind = UsersOdb.find("12345")
    fresFind onComplete {
      case Success(l) => assert(l.size==1)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
    val fresDelete = UsersOdb.remove(test)
    fresDelete onComplete{
      case Success(b) => assert(b)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
  }

  test("UsersOdb.all method invoked testAll") {
    val u1 = User("primo", Option("bgd@hot.it"), None, Option((tagTest,42D,42L)::List()))
    val u2 = User("secondo", Option("bgd@hot.it"), None, Option((tagTest,42D,42L)::List()))
    val u3 = User("terzo", Option("bgd@hot.it"), None, Option((tagTest,42D,42L)::List()))
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

  test("UserOdb.update is invoked") {
    TagsOdb.save(tagTest,true)
    /*val newTagTest = Tag("nuovo","tag")
    TagsOdb.save(newTagTest)
    val oldUser = User("12345", Option("bgd@hot.it"), None,Option((tagTest,42D,42L)::List()))
    UsersOdb.save(oldUser)
    val newUser = User("12345",Option("bgd@hat.ru"),None,Option((newTagTest,50D,40L)::List()))
    UsersOdb.update(newUser) onComplete {
      case Success(r) => assert(r)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
    UsersOdb.remove(newUser)
    TagsOdb.remove(newTagTest)*/
    assert(true)
  }*/

}

