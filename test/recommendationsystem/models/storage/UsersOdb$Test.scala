package recommendationsystem.models.storage

import org.scalatest.{BeforeAndAfterEach, FunSuite}

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by bsuieric on 15/10/15.
 */
class UsersOdb$Test extends FunSuite with BeforeAndAfterEach {

  //  override def beforeEach() {

  //}

  //override def afterEach() {

  //}

  test("testCount") {
    val fres = UsersOdb.count
    fres onComplete {
      case Success(count) => println(count); assert(true);
      case Failure(t) => println("An error has occured : " + t.getMessage)
    }
  }

  //test("testRemove") {

  //}

  //test("testSave") {

  //}

}
