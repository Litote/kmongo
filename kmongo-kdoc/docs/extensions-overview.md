# Extensions Overview

This overview presents the extensions for the synchronous driver. 

Equivalent methods are available for async drivers.

## Insert, Update & Delete

### Insert
Note that by default, KMongo serializes null values during insertion of instances with the Jackson mapping engine. This is not the case with the "native" POJO engine.

```kotlin
class TFighter(val version: String, val pilot: Pilot?)

database.getCollection<TFighter>().insertOne(TFighter("v1", null))
val col = database.getCollection("tfighter")
//collection of org.bson.Document is returned when no generic used

println(col.findOne()) //print: Document{{_id=(...), version=v1, pilot=null}}
```

The query shell format is supported:

```kotlin
database.getCollection<TFighter>().insertOne("{'version':'v1'}")

```

### Save

KMongo provides a convenient [```MongoCollection<T>.save(target: T)```](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/com.mongodb.client.-mongo-collection/save.html) method. If the instance passed as argument has no id, or a null id, it is inserted (and the id generated on server side or client side, respectively). Otherwise, a ```replaceOneById``` with ```upsert=true``` is performed.

### Update

KMongo provides various [```MongoCollection<T>.updateOne```](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/com.mongodb.client.-mongo-collection/update-one.html) methods.

- you can perform an update using shell query format:

```kotlin
col.updateOne("{name:'Paul'}", "{$set:{name:'John'}}")
```

- or with typed queries:

```kotlin
col.updateOne(Friend::name eq "Paul", set(Friend::name, "John"))
//or with annotation processor ->
col.updateOne(Name eq "Paul", set(Name, "John"))

```

- You can also update an instance by its _id (all properties of the instance are updated):

```kotlin
//explicitly
col.updateOneById(friend._id, newFriend)
//or implicitly
//note that if newFriend does not contains an _id value, the operation fails
col.updateOne(newFriend)

```

- Null properties are taken into account during the update (they are set to null in the document in MongoDb).
If you prefer to ignore null properties during the update, you can use the `updateOnlyNotNullProperties` parameter:

```kotlin
col.updateOne(newFriend, updateOnlyNotNullProperties = true)
```

If you think it should be the default behaviour for all updates:

```kotlin
UpdateConfiguration.updateOnlyNotNullProperties = true
```


[```updateMany```](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/com.mongodb.client.-mongo-collection/update-many.html)
is also supported.

### Replace

The [```replaceOne```](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/com.mongodb.client.-mongo-collection/replace-one.html)
and [```replaceOneById```](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/com.mongodb.client.-mongo-collection/replace-one-by-id.html) 
methods remove automatically the _id of the replacement when using shell query format, and looks like update methods:

```kotlin
//ObjectId of friend replacement does not matter 
col.replaceOne("{name:'Peter'}}", Friend(ObjectId(), "John"))
//explicit id filter ->
col.replaceOneById(friend._id, Friend(ObjectId(), "John"))
//implicit id filter ->
col.replaceOne(Friend(friend._id, "John"))

``` 
But for typed queries you need to use [```replaceOneWithFilter```](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/com.mongodb.client.-mongo-collection/replace-one-with-filter.html)
as the default java driver method does not remove the _id:

```kotlin
//ObjectId of friend replacement is removed from the replace query 
//before it is sent to mongo 
col.replaceOneWithFilter(Name eq "Peter", Friend(ObjectId(), "John"))
// if the mongo java driver method is used, it will fail at runtime
// as the _id is updated ->
col.replaceOne(Name eq "Peter", Friend(ObjectId(), "John"))
```

### Delete

[```deleteOne```](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/com.mongodb.client.-mongo-collection/delete-one.html)
and [```deleteOneById```](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/com.mongodb.client.-mongo-collection/delete-one-by-id.html)
work as expected:

```kotlin
col.deleteOne("{name:'John'}")
col.deleteOne(Friend::name eq "John")
```

As does [```deleteMany```](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/com.mongodb.client.-mongo-collection/delete-many.html).

### FindOneAnd...

The java driver variants are also supported:

- [```findOneAndUpdate```](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/com.mongodb.client.-mongo-collection/find-one-and-update.html)
- [```findOneAndReplace```](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/com.mongodb.client.-mongo-collection/find-one-and-replace.html)
- [```findOneAndDelete```](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/com.mongodb.client.-mongo-collection/find-one-and-delete.html) 

### Bulk Write

If you want to do many operations with only one query (for performance reasons),
[```bulkWrite```](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/com.mongodb.client.-mongo-collection/bulk-write.html)
is supported.

With query shell format : 

```kotlin
val friend = Friend("John", "22 Wall Street Avenue")
val result = col.bulkWrite(
   """ [
      { insertOne : { "document" : ${friend.json} } },
      { updateOne : {
                      "filter" : {name:"Fred"},
                      "update" : {$set:{address:"221B Baker Street"}},
                      "upsert" : true
                    }
      },
      { updateMany : {
                          "filter" : {},
                          "update" : {$set:{address:"nowhere"}}
                      }
      },
      { replaceOne : {
                          "filter" : {name:"Max"},
                          "replacement" : {name:"Joe"},
                          "upsert" : true
                      }
      },
      { deleteOne :  { "filter" : {name:"Joe"} }},
      { deleteMany :  { "filter" : {} } }
      ] """
 )
 
 assertEquals(1, result.insertedCount)
 assertEquals(2, result.matchedCount)
 assertEquals(3, result.deletedCount)
 assertEquals(2, result.modifiedCount)
 assertEquals(2, result.upserts.size)
 assertEquals(0, col.count())
```

or typed queries :

```kotlin
with(friend) {
        col.bulkWrite(
            insertOne(friend),
            updateOne(
                ::name eq "Fred",
                set(::address, "221B Baker Street"),
                upsert()
            ),
            updateMany(
                EMPTY_BSON,
                set(::address, "nowhere")
            ),
            replaceOne(
                ::name eq "Max",
                Friend("Joe"),
                upsert()
            ),
            deleteOne(::name eq "Max"),
            deleteMany(EMPTY_BSON)
        )
```

## Retrieve Data

### find

[```findOne```](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/com.mongodb.client.-mongo-collection/find-one.html),
[```findOneById```](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/com.mongodb.client.-mongo-collection/find-one-by-id.html)
and [```find```](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/com.mongodb.client.-mongo-collection/find.html)
works as expected.

With the shell query format:

```kotlin
col.findOne("{name:'John'}")
```

or the type-safe query format:

```kotlin
col.findOne(Friend::name eq "John")
//implicit $and is used if more than one criterion is set ->
col.findOne(Name eq "John", Address eq "22 Wall Street Avenue")
```

There are also extensions for [```FindIterable```](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/com.mongodb.client.-find-iterable/index.html).
The most used is usually the [```toList```](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/com.mongodb.client.-mongo-iterable/to-list.html)
extension on ```MongoIterable```:

```kotlin
val list : List<Friend> = col.find(Friend::name eq "John").toList()
```

### count

[```count```](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/com.mongodb.client.-mongo-collection/count.html)
is supported:

```kotlin
col.count("{name:{$exists:true}}")
```

### distinct

As is [```distinct```](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/com.mongodb.client.-mongo-collection/distinct.html):

With shell query format:

```kotlin
col.distinct<String>("address")
```

Or type-safe queries:

```kotlin
col.distinct(Friend::address)
```

### projection

You can use [```MongoCollection.projection```](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/com.mongodb.client.-mongo-collection/projection.html)
extension functions in order to retrieve only one, two or three fields. This is a "kmongo sync driver only feature" for now:

```kotlin
col.bulkWrite(
    insertOne(Friend("Joe")),
    insertOne(Friend("Bob"))
    )
val result: List<String?> = col.projection(Friend::name).toList()
```

If you need to retrieve more than three fields - or if you don't use the sync driver - you have two options:
 
- Use a custom dedicated class with [```FindIterable.projection```](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/com.mongodb.client.-find-iterable/projection.html) functions.
- Use projection with org.bson.Document and use `findValue` extension function:

```kotlin
col.insertOne(Friend("Joe", Coordinate(1, 2)))
val (name, lat, lng) =
   col.withDocumentClass<Document>()
       .find()
       .descendingSort(Friend::name)
       .projection(
           fields(
               include(
                   Friend::name,
                   Friend::coordinate / Coordinate::lat,
                   Friend::coordinate / Coordinate::lng
               ), 
               excludeId()
           )
       )
       .map {
           Triple(
               //classic Document.getString method  
               it.getString("name"),
               //get two levels property value
               it.findValue<Int>("coordinate.lat"),
               //use typed values
               it.findValue(Friend::coordinate / Coordinate::lng)
           )
       }
       .first()!!    
```


## Map-Reduce

[```mapReduce```](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/com.mongodb.client.-mongo-collection/map-reduce.html)
and equivalent method (to avoid name conflict with java driver) [```mapReduceWith```](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/com.mongodb.client.-mongo-collection/map-reduce-with.html)
are available:

```kotlin
data class KeyValue(val _id:String, val value:Long)

col.mapReduce<KeyValue>(
            """
                function() {
                       emit("name", this.name.length);
                   };
            """,
            """
                 function(name, l) {
                          return Array.sum(l);
                      };
            """
        ).first()

``` 

## Aggregate

The [aggregation framework](https://docs.mongodb.com/manual/aggregation/) is supported.

It works with shell query format, with [several limitations](mongo-shell-support/index.html):

```kotlin
col.aggregate<Article>("[{$match:{tags:'virus'}},{$limit:1}]").toList()
```

But this is an area or type-safe style shines:

```kotlin
val r = col.aggregate<Result>(
            match(
                Article::tags contains "virus"
            ),
            project(
                Article::title from Article::title,
                Article::ok from cond(Article::ok, 1, 0),
                Result::averageYear from year(Article::date)
            ),
            group(
                Article::title,
                Result::count sum Article::ok,
                Result::averageYear avg Result::averageYear
            ),
            sort(
                ascending(
                    Result::title
                )
            )
        )
        .toList()             
```

## Indexes

KMongo provides several methods to manage indexes.

### ensureIndex

[```ensureIndex```](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/com.mongodb.client.-mongo-collection/ensure-index.html)
and [```ensureUniqueIndex```](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/com.mongodb.client.-mongo-collection/ensure-unique-index.html) 
ensure that index is created even if it's structure has changed.

You can create an index with shell query format:

```kotlin 
col.ensureUniqueIndex("{'id':1,'type':1}")
```

or with type-safe style:

```kotlin
col.ensureUniqueIndex(Id, Type)
```

### dropIndex

With [```dropIndexOfKeys```](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/com.mongodb.client.-mongo-collection/drop-index-of-keys.html),
 you can drop index, based on their keys:
 
```kotlin 
col.dropIndexOfKeys("{'id':1,'type':1}")
```  

## MongoIterable

The [MongoIterable](http://mongodb.github.io/mongo-java-driver/3.8/javadoc/com/mongodb/client/MongoIterable.html) interface 
 of the java synchronous driver has a major flaw: it extends [Iterable](https://docs.oracle.com/javase/10/docs/api/java/lang/Iterable.html?is-external=true)
 with this declaration:
 
```java
MongoCursor<TResult> iterator();
``` 
 
The problem is that you need to close each [MongoCursor](http://mongodb.github.io/mongo-java-driver/3.8/javadoc/com/mongodb/client/MongoCursor.html)
you create from a [MongoIterable](http://mongodb.github.io/mongo-java-driver/3.8/javadoc/com/mongodb/client/MongoIterable.html), or you get a potential memory leak.
This is really error prone. 
 
```kotlin
for(r in col.find()) println(r)
//you have a memory leak - with java or kotlin!
``` 
 
This is especially problematic for Kotlin, as it is a common practise to use the Kotlin Iterable extensions:

```kotlin
// without KMongo, a memory leak at each line!
col.find().firstOrNull()
col.find().mapIndexed{ ... }
col.find().toList()
col.find().map {it.a to it.b}.toMap()
col.find().forEach{println(it)}
```  

KMongo does not fix the "for" issue, but **does solve automatically memory leaks for all other patterns** by providing
[MongoIterable extensions](https://litote.org/kmongo/dokka/kmongo/kotlin.collections/com.mongodb.client.-mongo-iterable/index.html).

You have nothing to change in your code - just compile it with the KMongo dependency in the classpath! 

## withKMongo

The recommended method, in order to setup KMongo, is to use `KMongo.createClient()`
to get a `MongoClient` instance.

However you can also get a MongoClient instance directly from the java driver and then call
`MongoDatabase#withKMongo` or `MongoCollection#withKMongo` extensions to enable KMongo object mapping support.     

This is especially useful if you have already a java project and you want to migrate progressively
to Kotlin.

## Coroutine

If you use the kmongo-coroutine library, use the ```coroutine``` extension method first to get KMongo extensions. 

## KDoc

Please consult [KDoc](http://litote.org/kmongo/dokka/kmongo/)
for an exhaustive list of the KMongo extensions.

