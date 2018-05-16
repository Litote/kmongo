# Typed Queries

## Type-safe Queries

KMongo provides a type-safe query framework. 

It has currently a **beta** flag. 
We use it in production, but it is not yet complete.

[Kotlin property references](https://kotlinlang.org/docs/reference/reflection.html#property-references) are used
to build mongo queries.
 
For example, here ```eq``` and ```regexp``` are infix functions provided by KMongo:

```kotlin
data class Jedi(val name: String)

val yoda = col.findOne(Jedi::name eq "Yoda")

//compile error as 2 is not a String ->
val error = col.findOne(Jedi::name eq 2)

//you can use property reference with instances
val yoda2 = col.findOne(yoda::name regex "Yo.*")
```

### eq & contains

Mongo uses the eq operator both for document value and array value.
In order to be type-safe, KMongo introduces the ```contains``` function for arrays:

```kotlin
data class Article(val title: String,val tags: List<String>)

col.aggregate<Article>(match(Article::tags contains "virus")) (...)

```

### Nested properties

To define nested properties, KMongo introduces the ```/``` operator.

So for example:

```kotlin
class Friend(val coor: Coordinate?)
class Coordinate(val lat: Int, val lng : Int)

// the generated query is {"coor.lat":{$lt:0}}
col.findOne(Friend::coor / Coordinate::lat lt 0)
```

## KMongo Annotation processor

Specifying the property references can be cumbersome, especially for nested properties.

KMongo provides an annotation processor to generate these property references.

You just have to annotate the data class with the ```@Data``` annotation.

Then you can write:

```kotlin
import Friend_.Coor

class Friend(val coor: Coordinate?)
class Coordinate(val lat: Int, val lng : Int)

col.findOne(Coor.lat lt 0)
```  

### Limitations

For now, the annotation processor has the following known limitations:

- works only with jdk8 at build time (but generates java9/10 compliant code). See [KT-24311](https://youtrack.jetbrains.com/issue/KT-24311)
- all ```@Data``` annotated classes must have public visibility and public properties
- Collections of Nullable types (ie Collection<Any?\>) are not yet well supported
- Map fields are not yet supported

