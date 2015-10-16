package recommendationsystem.models.storage

import recommendationsystem.models.Tag

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
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
  }

  test("TagsOdb.save is invoked and TagsOdb.remove is invoked") {
    val fres1 = TagsOdb.save(t)
    fres1 onComplete {
      case Success(b) => assert(b)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
    val fres2 = TagsOdb.remove(t)
    fres2 onComplete {
      case Success(b) => assert(b)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
  }

  test("TagsOdb.all is invoked") {
    val t1 = Tag("uno","uno")
    val t2 = Tag("due","due")
    val t3 = Tag("tre","tre")
    val tlst = t1 :: t2 :: t3 :: List()
    tlst map ( x => TagsOdb.save(x) ) map (
      t => t onComplete {
        case Success(r) => assert(r)
        case Failure(t) => println("An error has occured: " + t.getMessage)
      }
      )
    TagsOdb.all onComplete {
      case Success(lst) => assert(lst.size == 3)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
    tlst map ( x => TagsOdb.remove(x) ) map (
      t => t onComplete {
        case Success(r) => assert(r)
        case Failure(t) => println("An error has occured: " + t.getMessage)
      }
      )
    assert(true)
  }

  test("TagsOdb.update is invoked") {
    val ot = Tag("sette","sette")
    TagsOdb.save(ot) onComplete {
      case Success(r) => assert(r)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
    val nt = Tag(ot.attr,"te")
    TagsOdb.update(nt,ot) onComplete {
      case Success(r) => assert(r)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
    TagsOdb.remove(nt) onComplete {
      case Success(b) => assert(b)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
  }

  test("TagsOdb.find is invoked") {
    TagsOdb.save(t) onComplete {
      case Success(r) => assert(r)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
    TagsOdb.find(t.flatten) onComplete {
      case Success(l) => assert(l.size == 1); assert(l.head.equals(t))
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
    TagsOdb.remove(t) onComplete {
      case Success(b) => assert(b)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
  }

}
