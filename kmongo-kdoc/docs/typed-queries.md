# Typed Queries

## Type-safe Queries

KMongo provides a type-safe query framework. 

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

### Samples

#### Queries with and & or

```kotlin
col.findOne(or(Jedi::name eq "Yoda", Jedi::age gt 25))  
col.findOne(and(Jedi::name eq "Yoda", Jedi::age gt 25))

//The and is implicit
col.findOne(Jedi::name eq "Yoda", Jedi::age gt 25)
```  

#### Update

```kotlin
//Both are equivalent
col.updateOne(friend::name eq "Paul", setValue(friend::name, "John"))   
col.updateOne(friend::name eq "Paul", Friend::name setTo "John")

//Multi fields update 
col.updateOne(friend::name eq "Paul", set( Friend::name setTo "John", Friend::age setTo 25))

//other operations are supported
col.updateOne(Friend::name eq "John", pull(Friend::tags, "t2"))
```                                                            

#### Array operators

You can use [positional array operators](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/kotlin.reflect.-k-property1/index.html):

```kotlin        

data class EvaluationAnswer(val answers:List<MyAnswer>)
data class MyAnswer(val _id:String, val alreadyUsed: Boolean)

//Both are equivalent
col.updateMany( "{ \"answers._id\": { \$in: answerIds } }, { $set:{ \"answers.\$[].alreadyUsed\": true}}")
 
col.updateMany(
            (EvaluationAnswer::answers / MyAnswer::_id) `in` answerIds,
            setValue(EvaluationAnswer::answers.allPosOp / MyAnswer::alreadyUsed, true)
        )

```    

#### Map operators

```kotlin 
data class Friend(val localeMap: Map<Locale, Gift>)
data class Gift(val amount: BigDecimal)
         
//returns true
assertEquals("localeMap.en.amount", (Friend::localeMap.keyProjection(Locale.ENGLISH) / Gift::amount).path())
```

#### Aggregation

You can chain aggregation operators:

```kotlin
data class Article(
        val title: String,
        val author: String,
        val tags: List<String>,
        val date: Instant = Instant.now(),
        val count: Int = 1,
        val ok: Boolean = true
    )

data class Result(
        @BsonId val title: String,
        val averageYear: Double = 0.0,
        val count: Int = 0,
        val friends: List<Friend> = emptyList()
    )

val r = col.aggregate<Result>(
            match(
                Article::tags contains "virus"
            ),
            group(
                Article::title, Result::friends.push(Friend::name from Article::author)
            ),
            sort(
                ascending(
                    Result::title
                )
            )
        )
```

Other example:

```kotlin
val result = col.aggregate<Result>(
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
```   

Lookup sample:

```kotlin
data class Answer(val evaluator: String, val alreadyUsed: Boolean, val answerDate: Instant)

data class EvaluationsForms(val questions: List<String>)

data class EvaluationsFormsWithResults(val questions: List<String>, val results: List<EvaluationRequest>)

data class EvaluationsAnswers(val questionId: String, val evaluated: String, val answers: List<Answer>)

data class EvaluationRequest(val userId: String, val evaluationDate: String) 

val bson = lookup(
            "evaluationsAnswers",
            listOf(EvaluationsForms::questions.variableDefinition()),
            EvaluationsFormsWithResults::results,
            match(
                expr(
                    and from listOf(
                        `in` from listOf(EvaluationsAnswers::questionId, EvaluationsForms::questions.variable),
                        eq from listOf(EvaluationsAnswers::evaluated, "id")
                    )
                )
            ),
            EvaluationsAnswers::answers.unwind(),
            match(
                expr(
                    and from listOf(
                        eq from listOf(EvaluationsAnswers::answers / Answer::alreadyUsed, false),
                        gte from listOf(EvaluationsAnswers::answers / Answer::answerDate, Instant.now())
                    )
                )
            ),
            group(
                fields(
                    EvaluationRequest::userId from (EvaluationsAnswers::answers / Answer::evaluator),
                    EvaluationRequest::evaluationDate from (
                            dateToString from (
                                    combine(
                                        "format" from "%Y-%m-%d",
                                        "date" from (EvaluationsAnswers::answers / Answer::answerDate)
                                    )
                                    )
                            )
                )
            ),
            replaceRoot("_id".projection)
        )
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

