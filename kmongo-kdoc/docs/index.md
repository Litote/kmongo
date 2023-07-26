# <span style="color:red">Deprecation notice</span>

KMongo was created in *2016*, when there was no official MongoDb kotlin driver.

On *June 28, 2023*, a kotlin driver, whose development I was able to follow, was [made available by MongoDb](https://www.mongodb.com/developer/languages/kotlin/).

KMongo is therefore officially deprecated in favor of this driver. What does this mean concretely?

- If you are starting a new project, use the official kotlin driver. You will get support from the MongoDb team.

- If you have an existing project using KMongo, there's no rush to switch to the official driver: I'll maintain KMongo for several more years for my own projects.
  But no more evolutions will be added.
  You will therefore have to plan a migration from KMongo to the MongoDb driver within a few months/years.

## How to switch from KMongo to kotlin mongo driver ?

- The MongoDb kotlin driver supports "native" object mapping and also kotlinx-serialization.
  If you use one of these two mappings in KMongo, the transition will therefore be relatively simple.
  If you use jackson mapping, there will be a little more effort.

- The kotlin MongoDb driver has a synchronous version and a coroutine version. 
If you are using one of these versions in KMongo, you should have no problem.

- The missing feature currently with the official kotlin driver is the construction of queries which is a [little less intuitive](https://docs-mongodbcom-staging.corp.mongodb.com/kotlin/docsworker-xlarge/docsp-29260-migrate/migrate-kmongo/).

However, you can still use KMongo's query system coupled with the official driver. 
As I migrate my own projects, I plan to release an isolated utility library
that will simplify the transition from KMongo query system to the new kotlin driver.

Many thanks to the MongoDB team who are now giving kotlin the support it deserves!

# ![KMongo logo](assets/images/kmongo.png) **KMongo** - a Kotlin toolkit for Mongo  

```kotlin

data class Jedi(val name: String, val age: Int)

col.insertOne(Jedi("Luke Skywalker", 19)) //java driver method
col.insertOne("{name:'Yoda',age:896}")    //KMongo extension function

//object mapping & mongo shell query format is supported ->
val yoda : Jedi? = col.findOne("{name: {$regex: 'Yo.*'}}")

//type-safe query style is recommended for complex apps ->
val luke = col.aggregate<Jedi>(
            match(Jedi::age lt yoda?.age),
            sample(1)
        ).first()   
```



## Native and Lightweight

KMongo features are available via [Kotlin extensions](https://kotlinlang.org/docs/reference/extensions.html) -
you use transparently [the core MongoDB java driver API](https://docs.mongodb.com/ecosystem/drivers/java/)
(both sync and reactive streams (ie async) drivers are supported)

### With complete reactive streams & async support

You can use extensions for reactive streams style, 
[Kotlin Coroutines](https://kotlinlang.org/docs/reference/coroutines.html),
[Reactor](https://projectreactor.io/)
 or [RxJava2](http://reactivex.io/).

## Built-in Object Mapping

Object oriented programming is usually better - use Objects, not Maps. Powered by the native
[POJO Codec](https://mongodb.github.io/mongo-java-driver/3.5/bson/pojos/), the
[Jackson library](https://github.com/FasterXML/jackson) or
[Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization).

## Type-safe queries

Have you already queried an ```Int``` field with a ```String``` value? 
KMongo type-safe query system removes the query type errors.
Provided with an optional annotation processor. 

## Mongo shell queries

You can copy/paste your queries from the Mongo shell in your IDE. Write readable source code!