package recommendationsystem.models.storage

import scala.util.{Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global
/**
 * Created by aandelie on 14/10/15.
 */
class TagsOdb$Test extends org.scalatest.FunSuite {
  test("TagsOdb.count is invoked") {
    val fres = TagsOdb.count
    fres onComplete {
      case Success(count) => assert(count == 1L)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
  }
}
