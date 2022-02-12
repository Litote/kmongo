# Quick Start

## Choose the java driver

Do you need to use the sync or the async driver?

If you don't know, start with the sync driver and add this dependency to your project:

- with Maven

```xml
<dependency>
  <groupId>org.litote.kmongo</groupId>
  <artifactId>kmongo</artifactId>
  <version>4.5.0</version>
</dependency>
```

- or Gradle 

*(Kotlin)* 
```kotlin
implementation("org.litote.kmongo:kmongo:4.5.0")
``` 
*(Groovy)* 
```groovy
implementation 'org.litote.kmongo:kmongo:4.5.0'
```   

> Starting from 4.0, minimum supported jvm is now 1.8 (was 1.6).
> You have to set the property **jvmTarget** to 1.8 (or more) in your gradle or maven descriptor

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
  <version>4.5.0</version>
</dependency>
```

- or Gradle

*(Kotlin)* 
```kotlin
implementation("org.litote.kmongo:kmongo-async:4.5.0")
``` 
*(Groovy)* 
```groovy
implementation 'org.litote.kmongo:kmongo-async:4.5.0'
```

#### Kotlin Coroutines

- with Maven

```xml
<dependency>
  <groupId>org.litote.kmongo</groupId>
  <artifactId>kmongo-coroutine</artifactId>
  <version>4.5.0</version>
</dependency>
```

- or Gradle (Kotlin)

*(Kotlin)* 
```kotlin
implementation("org.litote.kmongo:kmongo-coroutine:4.5.0")
``` 
*(Groovy)* 
```groovy
implementation 'org.litote.kmongo:kmongo-coroutine:4.5.0'
```

#### Reactor

```xml
<dependency>
  <groupId>org.litote.kmongo</groupId>
  <artifactId>kmongo-reactor</artifactId>
  <version>4.5.0</version>
</dependency>
```

- or Gradle

*(Kotlin)* 
```kotlin
implementation("org.litote.kmongo:kmongo-reactor:4.5.0")
``` 
*(Groovy)* 
```groovy
implementation 'org.litote.kmongo:kmongo-reactor:4.5.0'
```

#### RxJava2

- with Maven

```xml
<dependency>
  <groupId>org.litote.kmongo</groupId>
  <artifactId>kmongo-rxjava2</artifactId>
  <version>4.5.0</version>
</dependency>
```

- or Gradle

*(Kotlin)* 
```kotlin
implementation("org.litote.kmongo:kmongo-rxjava2:4.5.0")
``` 
*(Groovy)* 
```groovy
implementation 'org.litote.kmongo:kmongo-rxjava2:4.5.0'
```

## Object Mapping Engine

By default, [Jackson engine](https://github.com/FasterXML/jackson) is used.
You can use [POJO Codec engine](https://mongodb.github.io/mongo-java-driver/3.5/bson/pojos/) 
by adding a ```-native``` suffix to the artifactId, or
[Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)
by adding a ```-serialization``` suffix to the artifactId.

For example, replace ```kmongo``` by ```kmongo-native``` or ```kmongo-serialization``` for the sync driver
For the coroutine driver, replace ```kmongo-coroutine``` by ```kmongo-coroutine-native```  or ```kmongo-coroutine-serialization``` .
You can read more about the mapping engine in the [dedicated chapter](../object-mapping#how-to-choose-the-mapping-engine). 

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

### With kotlinx.serialization

```kotlin
import org.litote.kmongo.* //NEEDED! import KMongo extensions
       
@Serializable //you need to annotate each class you want to persist
data class Jedi(val name: String, val age: Int, val firstAppearance: StarWarsFilm)   

@Serializable
data class StarWarsFilm(
val name: String,             
//annotate with @Contextual the types that have already serializers - look at kotlinx.serialization documentation
@Contextual val date: LocalDate
)

val client = KMongo.createClient() //get com.mongodb.MongoClient new instance
val database = client.getDatabase("test") //normal java driver usage
val col = database.getCollection<Jedi>() //KMongo extension method
//here the name of the collection by convention is "jedi"
//you can use getCollection<Jedi>("otherjedi") if the collection name is different

col.insertOne(Jedi("Luke Skywalker", 19, StarWarsFilm("A New Hope", LocalDate.of(1977, Month.MAY, 25))))

val yoda : Jedi? = col.findOne(Jedi::name eq "Yoda")

(...)
```

## KDoc

The KMongo API documentation in KDoc format is available:

- [KMongo](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/index.html)
- [KMongo coroutine](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo.coroutine/index.html)
- [KMongo RXJava2](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo.rxjava2/index.html)
