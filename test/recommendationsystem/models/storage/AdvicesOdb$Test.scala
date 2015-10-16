package recommendationsystem.models.storage

import java.util.Calendar

import org.scalatest.{BeforeAndAfterEach, FunSuite}
import recommendationsystem.models.{User, Tag, Advice}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
 * Created by aandelie on 16/10/15.
 */
class AdvicesOdb$Test extends FunSuite with BeforeAndAfterEach {

  /*val advice = {
    val user = User("utendeTest",Option("utente@test"),None,Option((Tag("tag","caso"),0D,0L)::List()))
    Advice(
      "idDaEliminare",
      user,
      (Tag("tag","caso"),0D)::(Tag("caso","tag"),0D)::List(),
      Calendar.getInstance().getTimeInMillis,true,"system"
    )
  }*/

  /*override def beforeEach() {

  }

  override def afterEach() {

  }*/

  /*test("AdviceOdb.count is invoked") {
    val fres = AdvicesOdb.count
    fres onComplete {
      case Success(x) => assert(x==0)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
  }*/

  /*test("AdviceOdb.all is invoked") {
    val fres = AdvicesOdb.all
    fres onComplete {
      //TODO improve
      case Success(x) => assert(x.size==0)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
  }*/

  /*test("AdviceOdb.save is invoked") {
    val fres = AdvicesOdb.save(advice)
    fres onComplete {
      case Success(x) => assert(x)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
  }*/

}
