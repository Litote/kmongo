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

Then declare the annotation processor

- for Maven:

```xml   
<plugin>
    <executions>
        <execution>
            <id>kapt</id>
            <goals>
                <goal>kapt</goal>
            </goals>
            <configuration>
                <annotationProcessorPaths>
                    <annotationProcessorPath>
                        <groupId>org.litote.kmongo</groupId>
                        <artifactId>kmongo-annotation-processor</artifactId>
                        <version>${kmongo.version}</version>
                    </annotationProcessorPath>
                </annotationProcessorPaths>
            </configuration>
        </execution>

        <execution>
            <id>compile</id>
            <phase>compile</phase>
            <goals>
                <goal>compile</goal>
            </goals>
        </execution>

    </executions>
</plugin>
```

- or for Gradle

```gradle
plugins {
    id "org.jetbrains.kotlin.kapt" version "${kotlin.version}"
}

dependencies {
    kapt 'org.litote.kmongo:kmongo-annotation-processor:${kmongo.version}'
}
```

(See the dedicated [Kotlin page](http://kotlinlang.org/docs/reference/kapt.html) for more information about annotation processing.)


Now you can write:

```kotlin
import Friend_.Coor

class Friend(val coor: Coordinate?)
class Coordinate(val lat: Int, val lng : Int)

val col : Collection<Friend>
col.findOne(Coor.lat lt 0, Coor.lng gt 0)
```  

### @DataRegistry

You can use this annotation (that annotates a dedicated *object* for example) 
if you can't or don't want to annotate directly with @Data the target classes.

### Limitations

For now, the annotation processor has the following known limitations:

- all ```@Data``` annotated classes must have public visibility and public properties. 
If your classes are internal, you can use @Data(internal = true) but then classes that reference the target class must be internal also.
- Collections of Nullable types (ie Collection<Any?\>) are not yet supported

