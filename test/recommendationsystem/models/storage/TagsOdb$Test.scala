package recommendationsystem.models.storage

import recommendationsystem.models.Tag

import scala.util.{Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global
/**
 * Created by aandelie on 14/10/15.
 */
class TagsOdb$Test extends org.scalatest.FunSuite {
  test("TagsOdb.count is invoked") {
    val fres = TagsOdb.count
    fres onComplete {
      case Success(count) => assert(count == 0L)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
  }

  test("TagsOdb.save is invoked") {
    val t = Tag("cat","attr")
    val fres = TagsOdb.save(t)
    fres onComplete {
      case Success(b) => assert(b)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
  }


}
