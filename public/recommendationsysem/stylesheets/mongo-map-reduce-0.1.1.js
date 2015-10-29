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
    return reducedObject; 
};
/** In collection ottengo: 
 *  {
 *       tagId: "id",
 *       sum: n,
 *       sumQ: m
 *   }
 * { "_id" : "categoria1:attr11", "value" : { "tagId" : "categoria1:attr11", "sum" : 2, "sumQ" : 4 } }
 * { "_id" : "categoria1:attr12", "value" : { "tagId" : "categoria1:attr12", "sum" : 2, "sumQ" : 4 } }
 * { "_id" : "categoria2:attr21", "value" : { "tagId" : "categoria2:attr21", "sum" : 2, "sumQ" : 4 } }
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
                        prod: rowThis.weight * rowThat.weight,
                        count: 1
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
        reducedObject.sumProd += value.prod;
        reducedObject.count += value.count;
    });
    return reducedObject; 
};
/** In collection ottengo: 
 *{ "_id" : "categoria1:attr11-categoria1:attr12", "value" : { "tag1" : "categoria1:attr11", "tag2" : "categoria1:attr12", "prod" : 4, "count" : 1 } }
 *{ "_id" : "categoria1:attr11-categoria2:attr21", "value" : { "tag1" : "categoria1:attr11", "tag2" : "categoria2:attr21", "prod" : 4, "count" : 1 } }
 *{ "_id" : "categoria1:attr12-categoria2:attr21", "value" : { "tag1" : "categoria1:attr12", "tag2" : "categoria2:attr21", "prod" : 4, "count" : 1 } }
 *
 */
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
                        prod: rowThis.weight * rowThat.weight,
                        count: 1
                    }
                    emit(key, value);
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
};
/** In collection ottengo: 
 * { "_id" : "categoria1:attr11-categoria1:attr12", "value" : { "tag1" : "categoria1:attr11", "tag2" : "categoria1:attr12", "count" : 3 } }
 * { "_id" : "categoria1:attr11-categoria2:attr21", "value" : { "tag1" : "categoria1:attr11", "tag2" : "categoria2:attr21", "count" : 3 } }
 * { "_id" : "categoria1:attr12-categoria2:attr21", "value" : { "tag1" : "categoria1:attr12", "tag2" : "categoria2:attr21", "count" : 3 } }
 */
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
