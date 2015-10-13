package recommendationsystem.algorithms

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorSystem
import akka.routing.RoundRobinRouter
import akka.routing.Broadcast
import play.api.Play.current
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.libs.iteratee._
import scala.collection.mutable.HashMap
import play.api.libs.concurrent.Execution.Implicits._
import recommendationsystem.models.storage.MongoObj
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.core.commands._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.util._
import scala.concurrent._
import recommendationsystem.models._

case class TagSum(_id: String, sum: Double, sumQ: Double)
object TagSum {
  implicit val TagSumFormat = (
    (__ \ "_id").format[String] ~
    (__ \ "value" \ "sum").format[Double] ~
    (__ \ "value" \ "sumQ").format[Double])(TagSum.apply, unlift(TagSum.unapply))
}

object TagsSums extends MongoObj[TagSum] {
  val collectionName = "tagsSums"//"recommendation.tagsSums"
  implicit val storageFormat = TagSum.TagSumFormat
}

case class TagsMatch(_id: String, tag1: String, tag2: String, sum1: Double, sum2: Double, sumQ1: Double, sumQ2: Double, sumProd: Double, count: Int)
object TagsMatch {
  implicit val TagsMatchFormat = (
    (__ \ "_id").format[String] ~
    (__ \ "value" \ "tag1").format[String] ~
    (__ \ "value" \ "tag2").format[String] ~
    (__ \ "value" \ "sum1").format[Double] ~
    (__ \ "value" \ "sum2").format[Double] ~
    (__ \ "value" \ "sumQ1").format[Double] ~
    (__ \ "value" \ "sumQ2").format[Double] ~
    (__ \ "value" \ "sumProd").format[Double] ~
    (__ \ "value" \ "count").format[Int])(TagsMatch.apply, unlift(TagsMatch.unapply))
}

object TagsCompared extends MongoObj[TagsMatch] {
  val collectionName = "tagsMatch"//"recommendation.tagsMatch"
  implicit val storageFormat = TagsMatch.TagsMatchFormat
}

case class Similarity(id: String, tag1: String, tag1Name: String, tag2: String, tag2Name: String, eq: Double)
object Similarity extends MongoObj[Similarity] {
  val collectionName = "tagsSimilarity"//"recommendation.tagsSimilarity"
  implicit val storageFormat: OFormat[Similarity] = Json.format[Similarity]
  //  implicit val TagsSimilarityformat = (
  //    (__ \ "id").format[String] ~
  //    (__ \ "tag1").format[String] ~
  //    (__ \ "tag2").format[String] ~
  //    (__ \ "eq").format[Double] ~
  //    (__ \ "count").format[Int])(TagsMatch.apply, unlift(TagsMatch.unapply))
}

object Pearson {
  val map1 = """
      function() {
		var rows = this.tags;
			if (rows != undefined) {
				rows.forEach( function(row) {
					var w = row.weight;
					var key = row.tag;
					var value = {
					tagId: row.tag,
					sum: w,
					sumQ: Math.pow(w, 2)
				}
				emit( key, value);
				});
			}
		};"""
  val reduce1 = """
      function(key, values) {
		    var reducedObject = {
		        tagId: key,
		        sum: 0,
		        sumQ: 0
		    }
		    values.forEach( function(value) {
		        reducedObject.sum += value.sum;
		        reducedObject.sumQ += value.sumQ;
		    });
		    return reducedObject; 
		};"""
  val map2 = """
      function() {
		    var rows = this.tags;    
		    if (rows != undefined) {
    		    /** ordino i tag in ordine crescente */
		    	rows = rows.sort(function(a, b){return a.tag > b.tag}) 
		        rows.forEach( function(rowThis) {
		            if (rows != undefined) {
		                /** prendo solo i maggiori di quello che sto confrontando */
		                var toCompare = rows.filter(function(e){return rowThis.tag  < e.tag;})
		                toCompare.forEach( function(rowThat) {
		                    var key = rowThis.tag + "-" + rowThat.tag;
		                    var value = {
		                        tag1: rowThis.tag,
		                        tag2: rowThat.tag,
		    					sum1: rowThis.weight,
		                        sum2: rowThat.weight,
		                        sumQ1: Math.pow(rowThis.weight, 2),
		                        sumQ2: Math.pow(rowThat.weight, 2),
		                        sumProd: rowThis.weight * rowThat.weight,
		                        count: (Math.random() * 100) + 1
		                    }
		                    emit(key, value);
		                });
		            }
		        });
		    }
		};"""
  val reduce2 = """
      function(key, values) {
		    var reducedObject = {
		        tag1: "",
		        tag2: "",
    			sum1: 0,
			    sum2: 0,
			    sumQ1: 0,
			    sumQ2: 0,
		        sumProd: 0,
		        count: 0
		    }
		    values.forEach( function(value) {
		        reducedObject.tag1 = value.tag1;
		        reducedObject.tag2 = value.tag2;
	            reducedObject.sum1 += value.sum1;
		        reducedObject.sum2 += value.sum2;
		        reducedObject.sumQ1 += value.sumQ1;
		        reducedObject.sumQ2 += value.sumQ2;
		        reducedObject.sumProd += value.sumProd;
		        reducedObject.count += value.count;
		    });
		    return reducedObject; 
		};"""
  val map3 = """
      function() {
		    var rows = this.tags;
		
		    if (rows != undefined) {
    			/** ordino i tag in ordine crescente */
		    	rows = rows.sort(function(a, b){return a.tag > b.tag})     
		        rows.forEach( function(rowThis) {
		            if (rows != undefined) {
		                /** prendo solo i maggiori di quello che sto confrontando */
		                var toCompare = rows.filter(function(e){return rowThis.tag  < e.tag;})
		                toCompare.forEach( function(rowThat) {
		                    var key = rowThis.tag + "-" + rowThat.tag;
		                    var value = {
		                        tag1: rowThis.tag,
		                        tag2: rowThat.tag,
		                        prod: rowThis.weight * rowThat.weight,
		                        count: 1
		                    }
		                    emit(key, value);
		                });
		            }
		        });
		    }
		};"""
  val reduce3 = """
      function(key, values) {
		    var reducedObject = {
		        tag1: "",
		        tag2: "",
		        count: 0
		    }
		    values.forEach( function(value) {
		        reducedObject.tag1 = value.tag1;
		        reducedObject.tag2 = value.tag2;
		        reducedObject.count += value.count;
		    });
		    return reducedObject; 
		};"""

  /**
   * Execute map reduce on mongoDB
   * @param inCollecion collection where take the data
   * @param mapFunction string that contains javascript function for map
   * @param reduceFunction string that contains javascript funciton for reduce
   * @param outCollecion collection where save the data
   * @return a future json result
   */
  def mapReduce(inCollection: String, mapFunction: String, reduceFunction: String, outCollection: String): Future[JsValue] = {
    import reactivemongo.core.commands._
    import reactivemongo.bson._

    val output = Promise[JsValue]()
    val mapReduceCommand = BSONDocument(
      "mapreduce" -> inCollection,
      "map" -> mapFunction,
      "reduce" -> reduceFunction,
      "out" -> BSONDocument("replace" -> outCollection)) //BSONDocument("replace" -> "tagsSums"))
    val result = Similarity.collection.db.command(RawCommand(mapReduceCommand))
    result.onComplete {
      case Success(v) => output.complete(Try(Json.obj("ok" -> 1)))
      case Failure(e) => output.failure(e)
    }
    output.future
  }

  /**
   * First start map reduce, than calculate the pearson operation
   */
  def calculate: Future[String] = {
    /** start map/reduce */
    //val tagsSumsMapReduceFuture = mapReduce("recommendation.users", map1, reduce1, "recommendation.tagsSums")
    val tagsMatchMapReduceFuture = mapReduce("recommendation.users", map2, reduce2, "recommendation.tagsMatch")
    //val tagsMatchCounterMapReduceFuture = mapReduce("recommendation.users", map3, reduce3, "recommendation.tagsMatchCounter")

    val futureResult = for {
      //tagsSumsMapReduce <- tagsSumsMapReduceFuture
      tagsMatchMapReduce <- tagsMatchMapReduceFuture
      //tagsMatchCounterMapReduce <- tagsMatchCounterMapReduceFuture
    } yield {
      play.Logger.debug("finish MapReduces")
      play.Logger.debug("Start Pearson")
      startPearson
    }
    futureResult.flatMap(res => res)
  }

  /**
   * The Pearson operation
   */
  protected def startPearson: Future[String] = {
    /** Obtain my Enumerator with all users */
    val tagsMatch /*: Enumerator[TagsMatch]*/ = TagsCompared.all.toEnum

    /** create an Iteratee for processing each chunk */
    val tagsSumIteratee = Iteratee.foreach[TagsMatch] { tagsMatch =>
      val t1 = TagSum(tagsMatch.tag1, tagsMatch.sum1, tagsMatch.sumQ1) //TagsSums.find(Json.obj("_id" -> recommendation.tagsMatch.tag1)).one
      val t2 = TagSum(tagsMatch.tag2, tagsMatch.sum2, tagsMatch.sumQ2) //TagsSums.find(Json.obj("_id" -> recommendation.tagsMatch.tag2)).one

      val num = (tagsMatch.sumProd * tagsMatch.count) - (t1.sum * t2.sum)
      val den1 = ((tagsMatch.count * t1.sumQ) - Math.pow(t1.sum, 2))
      val den2 = ((tagsMatch.count * t2.sumQ) - Math.pow(t2.sum, 2))
      val den = Math.sqrt(den1) * Math.sqrt(den2)

      play.Logger.debug(
        s"""**** ITERATION ****
            t1 = ${Json.toJson(t1)}
            t2 = ${Json.toJson(t2)}
            t1 - t2 = ${Json.toJson(tagsMatch)}
            num = (${tagsMatch.count.toString} * ${tagsMatch.sumProd.toString}) - (${t1.sum.toString} * ${t2.sum.toString})
            den1 = (${tagsMatch.count.toString} * ${t1.sumQ.toString}) - ${t1.sum.toString}^2
            den2 = (${tagsMatch.count.toString} * ${t2.sumQ.toString}) - ${t2.sum.toString}^2
            den = sqrt(${den1.toString}) * sqrt(${den2.toString})""")

      val t1Id = Tag(tagsMatch.tag1, None).id
      val t2Id = Tag(tagsMatch.tag2, None).id
      val json = Similarity(
        id = (t1Id + "-" + t2Id) /*tagsMatch._id*/ ,
        tag1 = t1Id /*tagsMatch.tag1*/ ,
        tag1Name = tagsMatch.tag1,
        tag2 = t2Id /*tagsMatch.tag2*/ ,
        tag2Name = tagsMatch.tag2,
        eq = if (den == 0) 0 else num / den)
      Similarity.update(Json.obj("id" -> (t1Id + "-" + t2Id)), json, upsert = true)
    }

    /** create a promise to serve */
    val pResult = Promise[String]
    /** Start elaborate */
    val a = tagsMatch(tagsSumIteratee).onComplete { case Success(a) => pResult.complete(Success("Ok"))
      play.Logger.debug("Finish Pearson")
      calculateCorrelation


    }
    /** return the future */

    pResult.future
  }
  
  def calculateCorrelation: Future[Boolean] = {
    def calculate(category: String, tag: String, similarity: List[Similarity]): Future[(Double, Int)] = {
      play.Logger.debug("Calculate correlation of " + category + " " + tag)
      val value = similarity.foldLeft(0.0, 0)( (r, c) => if(c.tag1Name.split(":")(0) == category && c.tag2Name == tag) (r._1 + c.eq, r._2 + 1)  else r
            ) //fold the tags
      val sum = value._1 
      val count = value._2
      val result = if(count > 0) (sum/count, count) else (0.0, 0)
      play.Logger.debug(
        s"""**** ITERATION ****
            category = ${category}
            product = ${tag}
            sum = ${sum}
            count = (${count})""")
      Future{result}
      
    }
    
  play.Logger.debug("Start Correlation")
  Similarity.all.toList flatMap { tagsMatch =>
    val tuples = 
    for {
      i<- tagsMatch
    } yield (i.tag1Name.split(":")(0), i.tag2Name) // create e List[(String, String)] containing the category and productName
    val res = tuples map { el =>
      calculate(el._1, el._2, tagsMatch) flatMap { value =>
        val correlation = Correlation(el._1, el._2, value._1, value._2) // create the correlation
        val query = Json.obj("category" -> el._1, "attribute" -> el._2)
        Correlations.find(query).one flatMap(element => element match {
          case Some(x) => Correlations.update(query, correlation) flatMap {status => status match {
            case LastError(ok, _, _, _, _, _, _) => Future{true}
            case _ => Future{false}
          }
           
          }
          case None => Correlations.save(correlation) flatMap {status => status match {
            case LastError(ok, _, _, _, _, _, _) => Future{true}
            case _ => Future{false}
          }
            
          }
        }
            )
        
      }

    }
   play.Logger.debug("Finish Correlation")
   val result = if(res.exists(_ equals false)) false else true
   Future{result}
  }

}
  
}
