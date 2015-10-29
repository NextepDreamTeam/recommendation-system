{
	"user": {"id":"uno"},
	"tags": [
		{"tag":"cat1:att1"},
		{"tag":"cat2:att2"},
		{"tag":"cat3:att3"}
	]
}
{
	"user": {"id":"due"},
	"tags": [
		{"tag":"cat1:att1"},
		{"tag":"cat3:att3"}
	]
}
{
	"user": {"id":"tre"},
	"tags": [
		{"tag":"cat1:att1"},
		{"tag":"cat2:att2"}
	]
}
{
	"user": {"id":"quattro"},
	"tags": [
		{"tag":"cat2:att2"},
		{"tag":"cat3:att3"}
	]
}
db.tagsMatch.remove()
db.tagsSums.remove()
db.tagsSimilarity.remove()
db.tagsMatchCounter.remove()
db.users.remove()

/**

db.users.find()
{ "_id" : ObjectId("52f107d755d1587982833bf4"), "id" : "uno", "tags" : [
    {   "tag" : "cat1:att1",    "weight" : 1,   "lastInsert" : 1391527895394 },
    {   "tag" : "cat2:att2",    "weight" : 1,   "lastInsert" : 1391527895394 },
    {   "tag" : "cat3:att3",    "weight" : 1,   "lastInsert" : 1391527895394 }
] 
}
{ "_id" : ObjectId("52f107e955d1587982833bf8"), "id" : "due", "tags" : [
    {   "tag" : "cat1:att1",    "weight" : 1,   "lastInsert" : 1391527913622 },
    {   "tag" : "cat3:att3",    "weight" : 1,   "lastInsert" : 1391527913622 }
] }
{ "_id" : ObjectId("52f107f155d1587982833bfb"), "id" : "tre", "tags" : [
    {   "tag" : "cat1:att1",    "weight" : 1,   "lastInsert" : 1391527921206 },
    {   "tag" : "cat2:att2",    "weight" : 1,   "lastInsert" : 1391527921206 }
] }
{ "_id" : ObjectId("52f107f755d1587982833bfe"), "id" : "quattro", "tags" : [
    {   "tag" : "cat2:att2",    "weight" : 1,   "lastInsert" : 1391527927875 },
    {   "tag" : "cat3:att3",    "weight" : 1,   "lastInsert" : 1391527927875 }
] }


db.tagsMatch.find()
{ "_id" : "cat1:att1-cat2:att2", "value" : { "tag1" : "cat1:att1", "tag2" : "cat2:att2", "sumProd" : 2, "count" : 2 } }
{ "_id" : "cat1:att1-cat3:att3", "value" : { "tag1" : "cat1:att1", "tag2" : "cat3:att3", "sumProd" : 2, "count" : 2 } }
{ "_id" : "cat2:att2-cat3:att3", "value" : { "tag1" : "cat2:att2", "tag2" : "cat3:att3", "sumProd" : 2, "count" : 2 } }

db.tagsSums.find()
{ "_id" : "cat1:att1", "value" : { "tagId" : "cat1:att1", "sum" : 3, "sumQ" : 3 } }
{ "_id" : "cat2:att2", "value" : { "tagId" : "cat2:att2", "sum" : 3, "sumQ" : 3 } }
{ "_id" : "cat3:att3", "value" : { "tagId" : "cat3:att3", "sum" : 3, "sumQ" : 3 } }

finish MapReduces
Start Pearson
[debug] application - den1: ( 2 * 3.0) - (3.0 ^ 2) = -3.0
[debug] application - den2: ( 2 * 3.0) - (3.0 ^ 2) = -3.0
[debug] application - num: -5.0, den1: -3.0, den2: -3.0, den: NaN
[debug] application - den1: ( 2 * 3.0) - (3.0 ^ 2) = -3.0
[debug] application - den2: ( 2 * 3.0) - (3.0 ^ 2) = -3.0
[debug] application - num: -5.0, den1: -3.0, den2: -3.0, den: NaN
[debug] application - den1: ( 2 * 3.0) - (3.0 ^ 2) = -3.0
[debug] application - den2: ( 2 * 3.0) - (3.0 ^ 2) = -3.0
[debug] application - num: -5.0, den1: -3.0, den2: -3.0, den: NaN

*/


/**

db.users.find()
{ "_id" : ObjectId("52ef6b32870d1bcf7d939123"), "id" : "uno", "tags" : [
    {   "tag" : "cat1:att1", "weight" : 0.01, "lastInsert" : 1391422258775 },
    {   "tag" : "cat2:att2", "weight" : 0.01, "lastInsert" : 1391422258775 },
    {   "tag" : "cat3:att3", "weight" : 0.01, "lastInsert" : 1391422258775 } 
] }
{ "_id" : ObjectId("52ef6b55870d1bcf7d939126"), "id" : "due", "tags" : [
    {   "tag" : "cat1:att1", "weight" : 0.01, "lastInsert" : 1391422293107 },
    {   "tag" : "cat3:att3", "weight" : 0.01, "lastInsert" : 1391422293107 } 
] }
{ "_id" : ObjectId("52ef6b69870d1bcf7d939129"), "id" : "tre", "tags" : [
    {   "tag" : "cat1:att1", "weight" : 0.01, "lastInsert" : 1391422313786 },
    {   "tag" : "cat2:att2", "weight" : 0.01, "lastInsert" : 1391422313786 } 
] }
{ "_id" : ObjectId("52ef6b74870d1bcf7d93912d"), "id" : "quattro", "tags" : [
    {   "tag" : "cat2:att2", "weight" : 0.01, "lastInsert" : 1391422324425 },
    {   "tag" : "cat3:att3", "weight" : 0.01, "lastInsert" : 1391422324425 } 
] }

> db.tagsSums.find()
{ "_id" : "cat1:att1", "value" : { "tagId" : "cat1:att1", "sum" : 0.03, "sumQ" : 0.00030000000000000003 } }
{ "_id" : "cat2:att2", "value" : { "tagId" : "cat2:att2", "sum" : 0.03, "sumQ" : 0.00030000000000000003 } }
{ "_id" : "cat3:att3", "value" : { "tagId" : "cat3:att3", "sum" : 0.03, "sumQ" : 0.00030000000000000003 } }
> db.tagsMatch.find()
{ "_id" : "cat1:att1-cat2:att2", "value" : { "tag1" : "cat1:att1", "tag2" : "cat2:att2", "sumProd" : 0.0002, "count" : 2 } }
{ "_id" : "cat1:att1-cat3:att3", "value" : { "tag1" : "cat1:att1", "tag2" : "cat3:att3", "sumProd" : 0.0002, "count" : 2 } }
{ "_id" : "cat2:att2-cat3:att3", "value" : { "tag1" : "cat2:att2", "tag2" : "cat3:att3", "sumProd" : 0.0002, "count" : 2 } }


finish MapReduces
Start Pearson

[debug] application - den1: ( 2 * 3.0000000000000003E-4) - (0.03 ^ 2) = -2.999999999999999E-4
[debug] application - den2: ( 2 * 3.0000000000000003E-4) - (0.03 ^ 2) = -2.999999999999999E-4
[debug] application - num: -5.0E-4, den1: -2.999999999999999E-4, den2: -2.999999999999999E-4, den: NaN

[debug] application - den1: ( 2 * 3.0000000000000003E-4) - (0.03 ^ 2) = -2.999999999999999E-4
[debug] application - den2: ( 2 * 3.0000000000000003E-4) - (0.03 ^ 2) = -2.999999999999999E-4
[debug] application - num: -5.0E-4, den1: -2.999999999999999E-4, den2: -2.999999999999999E-4, den: NaN

[debug] application - den1: ( 2 * 3.0000000000000003E-4) - (0.03 ^ 2) = -2.999999999999999E-4
[debug] application - den2: ( 2 * 3.0000000000000003E-4) - (0.03 ^ 2) = -2.999999999999999E-4
[debug] application - num: -5.0E-4, den1: -2.999999999999999E-4, den2: -2.999999999999999E-4, den: NaN



*/


{ 
    "_id" : ObjectId("52a1fb197249544804268a82"), 
    "id" : "52ca7e93-1230-461c-b287-b50266ce39b7", 
    "email" : "mail@email.com", 
    "anag" : { "name" : "Mario", "lastName" : "Super" }, 
    "tags" : [  
                {   "tag" : "categoria1:attr11",    "weight" : 1,   "lastInsert" : 1386347289119 },     
                {   "tag" : "categoria1:attr12",    "weight" : 1,   "lastInsert" : 1386347289119 },     
                {   "tag" : "categoria2:attr21",    "weight" : 1,   "lastInsert" : 1386347289119 } 
            ] 
}




/***************************************************************************************************************/
/** per ogni utente per ogni suo tag sparo (tag.id, {tag.id, tag.score, tag.score^2}) */
var mapFunction1 = function() {
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
};
/** ricevo (tag.id, {tag.id, tag.score, tag.score^2}), 
 *  raggruppo per tag.id e sommo i tag.score e tag.score^2
 */
var reduceFunction1 = function(key, values) {
    var reducedObject = {
        tagId: key,
        sum: 0,
        sumQ: 0
    }
    values.forEach( function(value) {
        reducedObject.sum += value.sum;
        reducedObject.sumQ += value.sumQ;
    });
    return reducedObject 
};
/** In collection ottengo: 
 *  {
 *       tagId: "id",
 *       sum: n,
 *       sumQ: m
 *   }
 *
 */
 db.users.mapReduce( 
    mapFunction1,
    reduceFunction1,
       {
         //query: { ts: { $gt: ISODate('2011-11-05 00:00:00') } },
         //out: { reduce: "tagsSums" },
         out: "tagsSums",
         //finalize: finalizeFunction
       }
);
/***************************************************************************************************************/


/***************************************************************************************************************/
/** per ogni utente confronto i suoi n tag con gli altri (n - 1) 
 *  e sparo: 
 *   {
 *       tagMatchId: "t1.id-t2.id",
 *       prod: t1.weight * t2.weight,
 *       count: 1
 *   }
 */
var mapFunction2 = function() {
    var rows = this.tags;
    /** ordino i tag in ordine crescente */
    rows = rows.sort(function(a, b){return a.tag > b.tag})     

    if (rows != undefined) {
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
                        prod: rowThis.weight * rowThat.weight,
                        count: 1,
                    }
                    emit(key, value);
                });
            }
        });
    }
};
/** ricevo (tagMatchId, {tagMatchId, prod, count}), 
 *  raggruppo per tagMatchId e sommo i prod e count
 */
var reduceFunction2 = function(key, values) {
    var reducedObject = {
        tag1: "",
        tag2: "",
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
        reducedObject.sumProd += value.prod;
        reducedObject.count += value.count;
    });
    return reducedObject; 
};

db.users.mapReduce( 
    mapFunction2,
    reduceFunction2,
       {
         //query: { ts: { $gt: ISODate('2011-11-05 00:00:00') } },
         out: "tagsMatch"
         //out: { reduce: "tagsMatch" },
         //finalize: finalizeFunction
       }
);






/*********************************** CONTA FREQUENZA COPPIE TAG *************************************************/
{ 
    "_id" : ObjectId("52a1b8907249544804268a80"), 
    "user" : { "id" : "e9a1b371-456b-4b79-af95-377372aff8b6", "email" : "mail@email.com", "anag" : { "name" : "Mario", "lastName" : "Super" } }, 
    "tags" : [     
                {   "tag" : "categoria1:attr11" },  
                {   "tag" : "categoria1:attr12" },  
                {   "tag" : "categoria2:attr21" } 
            ], 
    "date" : 1386330255658 
}


/***************************************************************************************************************/
/** per ogni richiesta confronto in tag con gli altri (n - 1) 
 *  e sparo: 
 *   {
 *       tagMatchId: "t1.id-t2.id",
 *       count: 1
 *   }
 */
var mapFunction3 = function() {
    var rows = this.tags;
    if (rows != undefined) {
        rows.forEach( function(rowThis) {
            if (rows != undefined) {
                rows.forEach( function(rowThat) {
                    // ovviamente non lo confronto con se stesso
                    if (rowThis.tag != rowThat.tag) {
                        var key = rowThis.tag + "-" + rowThat.tag;
                        var value = {
                            tagMatchId: key,
                            count: 1
                        }
                        emit(key, value);
                    }
                });
            }
        });
    }
};
/** ricevo (tagMatchId, {tagMatchId, count}), 
 *  raggruppo per tagMatchId e sommo i count
 */
var reduceFunction3 = function(key, values) {
    var reducedObject = {
        tagMatchId: key,
        count: 0
    }
    values.forEach( function(value) {
        reducedObject.count += value.count;
    });
    return reducedObject; 
};

db.requests.mapReduce( 
    mapFunction3,
    reduceFunction3,
       {
         //query: { ts: { $gt: ISODate('2011-11-05 00:00:00') } },
         out: "tagsMatchCounter"
         //out: { reduce: "tagsMatch" },
         //finalize: finalizeFunction
       }
);

