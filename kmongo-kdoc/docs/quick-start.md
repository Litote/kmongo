# Quick Start

## Choose the java driver

Do you need to use the sync or the async driver?

If you don't know, start with the sync driver and add this dependency to your project:

- with Maven

```xml
<dependency>
  <groupId>org.litote.kmongo</groupId>
  <artifactId>kmongo</artifactId>
  <version>3.10.2</version>
</dependency>
```

- or Gradle

```
compile 'org.litote.kmongo:kmongo:3.10.2'
```

And [start coding](#lets-start-coding)

### Async driver support

KMongo can use the synchronous or the asynchronous java driver. 
For the asynchronous driver, reactive streams style, [Kotlin Coroutines](https://kotlinlang.org/docs/reference/coroutines.html) or [RxJava2](http://reactivex.io/) are supported.

#### Reactive Streams style

- with Maven

```xml
<dependency>
  <groupId>org.litote.kmongo</groupId>
  <artifactId>kmongo-async</artifactId>
  <version>3.10.2</version>
</dependency>
```

- or Gradle

```
compile 'org.litote.kmongo:kmongo-async:3.10.2'
```

#### Kotlin Coroutines

- with Maven

```xml
<dependency>
  <groupId>org.litote.kmongo</groupId>
  <artifactId>kmongo-coroutine</artifactId>
  <version>3.10.2</version>
</dependency>
```

- or Gradle

```
compile 'org.litote.kmongo:kmongo-coroutine:3.10.2'
```

#### RxJava2

- with Maven

```xml
<dependency>
  <groupId>org.litote.kmongo</groupId>
  <artifactId>kmongo-rxjava2</artifactId>
  <version>3.10.2</version>
</dependency>
```

- or Gradle

```
compile 'org.litote.kmongo:kmongo-rxjava2:3.10.2'
```

## Object Mapping Engine

By default, [Jackson engine](https://github.com/FasterXML/jackson) is used.
But you can use [POJO Codec engine](https://mongodb.github.io/mongo-java-driver/3.5/bson/pojos/) 
by adding a ```-native``` suffix to the artifactId.

For example, replace ```kmongo``` by ```kmongo-native```, or ```kmongo-async``` by ```kmongo-async-native```.
You can read more about the mapping engine in the [dedicated chapter](object-mapping/index.html#how-to-choose-the-mapping-engine). 

## Let's Start Coding

### With the sync driver

```kotlin
import org.litote.kmongo.* //NEEDED! import KMongo extensions

data class Jedi(val name: String, val age: Int)

val client = KMongo.createClient() //get com.mongodb.MongoClient new instance
val database = client.getDatabase("test") //normal java driver usage
val col = database.getCollection<Jedi>() //KMongo extension method
//here the name of the collection by convention is "jedi"
//you can use getCollection<Jedi>("otherjedi") if the collection name is different

col.insertOne(Jedi("Luke Skywalker", 19))

val yoda : Jedi? = col.findOne(Jedi::name eq "Yoda")

(...)
```

### Asynchronously with Coroutines

```kotlin
import org.litote.kmongo.reactivestreams.*  //NEEDED! import KMongo reactivestreams extensions
import org.litote.kmongo.coroutine.* //NEEDED! import KMongo coroutine extensions

data class Jedi(val name: String, val age: Int)

val client = KMongo.createClient().coroutine //use coroutine extension
val database = client.getDatabase("test") //normal java driver usage
val col = database.getCollection<Jedi>() //KMongo extension method

//async now
runBlocking {
    col.insertOne(Jedi("Luke Skywalker", 19))

    val yoda : Jedi? = col.findOne(Jedi::name eq "Yoda")

    (...)
}

```

## KDoc

The KMongo API documentation in KDoc format is available:

- [KMongo](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/index.html)
- [KMongo async](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo.async/index.html)
- [KMongo coroutine](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo.coroutine/index.html)
- [KMongo RXJava2](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo.rxjava2/index.html)
