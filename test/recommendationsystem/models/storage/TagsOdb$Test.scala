package recommendationsystem.models.storage


import recommendationsystem.models.Tag
import scala.concurrent.{duration, Await}
import scala.concurrent.duration.Duration
import scala.util.{Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by aandelie on 14/10/15.
 */
class TagsOdb$Test extends org.scalatest.FunSuite {

  val t = Tag("cat", "attr")

  test("TagsOdb.count is invoked") {
    val fres = TagsOdb.count
    fres onComplete {
      case Success(count) => assert(count == 0L)
      case Failure(t) => assert(false)
    }
    Await.result(fres,Duration(5000,duration.MILLISECONDS))
  }

  test("TagsOdb.save is invoked and TagsOdb.remove is invoked") {
    val fresSave = TagsOdb.save(t)
    fresSave onComplete {
      case Success(r) => assert(r)
      case Failure(t) => assert(false)
    }
    Await.result(fresSave,Duration(5000,duration.MILLISECONDS))
    val fresRemove = TagsOdb.remove(t)
    fresRemove onComplete {
      case Success(b) => assert(b)
      case Failure(t) => assert(false)
    }
    Await.result(fresRemove,Duration(5000,duration.MILLISECONDS))
  }

  test("TagsOdb.all is invoked") {
    val t1 = Tag("uno","uno")
    val t2 = Tag("due","due")
    val t3 = Tag("tre","tre")
    val tlst = t1 :: t2 :: t3 :: List()
    val threadsInsert = tlst map ( x => TagsOdb.save(x) )
    threadsInsert foreach (
      t => t onComplete {
        case Success(r) => assert(r)
        case Failure(t) => assert(false)
      }
      )
    threadsInsert foreach (thread => Await.result(thread,Duration(3,duration.SECONDS)))
    val all = TagsOdb.all
    all onComplete {
      case Success(lst) => assert(lst.size == 3)
      case Failure(t) => assert(false)
    }
    Await.result(all,Duration(3,duration.SECONDS))
    Odb.clearDb
  }

  test("TagsOdb.update is invoked") {
    val ot = Tag("sette","sette")
    val save = TagsOdb.save(ot)
    save onComplete {
      case Success(r) => assert(r)
      case Failure(t) => assert(false)
    }
    Await.result(save,Duration(5000,duration.MILLISECONDS))
    val nt = Tag(ot.attr,"te")
    val update = TagsOdb.update(nt,ot)
    update onComplete {
      case Success(r) => assert(r)
      case Failure(t) => assert(false)
    }
    Await.result(update,Duration(5000,duration.MILLISECONDS))
    Odb.clearDb
  }

  test("TagsOdb.find is invoked") {
    val save = TagsOdb.save(t)
    save onComplete {
      case Success(r) => assert(r)
      case Failure(t) => assert(false)
    }
    Await.result(save,Duration(5000,duration.MILLISECONDS))
    val find = TagsOdb.find(t.flatten)
    find onComplete {
      case Success(l) => assert(l.equals(t))
      case Failure(t) => assert(false)
    }
    Await.result(find,Duration(5000,duration.MILLISECONDS))
    Odb.clearDb
  }

}
