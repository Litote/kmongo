# Extensions Overview

## Insert, Save, Update and Replace operations

### Insert
Note that by default, KMongo serializes null values during insertion of instances with the Jackson mapping engine. This is not the case with the "native" POJO engine.

```kotlin
class TFighter(val version: String, val pilot: Pilot?)

database.getCollection<TFighter>().insertOne(TFighter("v1", null))
val col = database.getCollection("tfighter")
//collection of org.bson.Document is returned when no generic used

println(col.findOne()) //print: Document{{_id=(...), version=v1, pilot=null}}
```

### Save

KMongo provides a convenient ```MongoCollection<T>.save(target: T)``` method. If the instance passed as argument has no id, or a null id, it is inserted (and the id generated on server side or client side, respectively). Otherwise, a ```replaceOneById``` with ```upsert=true``` is performed.

### Update

KMongo provides ```MongoCollection<T>.updateOne(target: T)``` method - note that if the target does not contains an _id value, the operation fails. All properties of the instance are updated.

There are also ```updateOneById``` methods.

### Replace

Replace operations have the same signature than update. Please consult [KDoc](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/index.html).

