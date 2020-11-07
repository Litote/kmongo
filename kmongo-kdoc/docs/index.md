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