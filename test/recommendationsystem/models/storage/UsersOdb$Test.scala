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
    Odb.clearDb
    val add = TagsOdb.save(tagTest)
    Await.result(add,Duration(3,TimeUnit.SECONDS))
  }

  override def afterEach() {
    Odb.clearDb
  }

  test("UsersOdb.count method invoked") {
    val fres = UsersOdb.count
    fres onComplete {
      case Success(count) => println(count); assert(true);
      case Failure(t) => assert(false)
    }
    Await.result(fres,Duration(3,TimeUnit.SECONDS))
  }

  test("UsersOdb.save, find and remove method invoked testSave") {
    val test = User("12345", Option("bgd@hot.it"), None, Option((tagTest,42D,42L)::List()))
    val save = UsersOdb.save(test)
    save onComplete{
      case Success(b) => assert(b)
      case Failure(t) => assert(false)
    }
    Await.result(save,Duration(3,TimeUnit.SECONDS))
    val find = UsersOdb.find("12345")
    find onComplete {
      case Success(l) => assert(l.nonEmpty)
      case Failure(t) => assert(false)
    }
    Await.result(find,Duration(3,TimeUnit.SECONDS))
    val delete = UsersOdb.remove(test)
    delete onComplete{
      case Success(b) => assert(b)
      case Failure(t) => assert(false)
    }
    Await.result(delete,Duration(3,TimeUnit.SECONDS))
  }

  test("UsersOdb.all method invoked testAll") {
    val uList =
      User("primo", Option("bgd@hot.it"), None, Option((tagTest,42D,42L)::List())) ::
      User("secondo", Option("bgd@hot.it"), None, Option((tagTest,42D,42L)::List())) ::
      User("terzo", Option("bgd@hot.it"), None, Option((tagTest,42D,42L)::List())) ::
        List()
    val trdIns = uList map (x => UsersOdb.save(x))
    trdIns foreach(
      t => t onComplete {
        case Success(r) => assert(r)
        case Failure(t) => assert(false)
      })
    trdIns map ( u => Await.result(u,Duration(3,TimeUnit.SECONDS)))
    val all = UsersOdb.all
    all onComplete {
      case Success(list) => assert(list.size == 3)
      case Failure(t) => assert(false)
    }
    Await.result(all,Duration(3,TimeUnit.SECONDS))
  }

  test("UserOdb.update is invoked") {
    val newTagTest = Tag("nuovo","tag")
    Await.result(TagsOdb.save(newTagTest),Duration(3,TimeUnit.SECONDS))
    val oldUser = User("12345", Option("bgd@hot.it"), None,Option((tagTest,42D,42L)::List()))
    Await.result(UsersOdb.save(oldUser),Duration(3,TimeUnit.SECONDS))
    val newUser = User("12345",Option("bgd@hat.ru"),None,Option((newTagTest,50D,40L)::List()))
    val update = UsersOdb.update(newUser)
    update onComplete {
      case Success(r) => assert(r)
      case Failure(t) => assert(false)
    }
    Await.result(update,Duration(3,TimeUnit.SECONDS))
  }

  /*test("provaFind is invoked") {
    UsersOdb.provaFind()
    assert(true)
  }*/

}

