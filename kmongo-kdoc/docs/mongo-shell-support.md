# Mongo Shell Queries

In Kotlin the $ sign is used to define expressions in string templates. 
Unfortunately $ is also used by MongoDB to define operators. 
This means we need workarounds to play with mongo shell queries in Kotlin. 

## The MongoOperator enum class
The first workaround is easy et quite transparent. You just have to add an import org.litote.kmongo.MongoOperator.* and then use these operators in the string template. To reuse a previous example :

```kotlin
import org.litote.kmongo.MongoOperator.lt
import org.litote.kmongo.MongoOperator.match
import org.litote.kmongo.MongoOperator.regex
import org.litote.kmongo.MongoOperator.sample

val yoda = col.findOne("{name: {$regex: 'Yo.*'}}")!! //mongo shell query format is supported
//raw strings and string templates too
val luke = col.aggregate<Jedi>("""[ {$match:{age:{$lt : ${yoda.age}}}},
                                    {$sample:{size:1}}
                                 ]""").first()
```

## The json extension property

Now you want to use also real template expressions.
Except for very simple type (like Int), the values of these expressions have to be json encoded. 
So KMongo provides a json extension property for all classes. For example :

```kotlin
data class Jedi(val name: String, val age: Int)

val luke = Jedi("Luke Skywalker", 19)

//Real life example
col.findOne("{jediName:${luke.name.json}}")

//The json property works with all objects, using Mongo Extended Json format
println("${ObjectId().json}") //print {"$oid":"575b17284ce9b17526c26e46"}
println("${luke.json}") //print {"name":"Luke Skywalker","age":19}
```

## Advanced hacks

Unfortunately there are others corner cases. 
Note that you always have the option to escape the $ sign (ie \$) for single line strings, but NOT for raw strings (see [KT-2425](https://youtrack.jetbrains.com/issue/KT-2425)).

### The in keyword
in is a Kotlin reserved keyword and $in an operator in Mongo shell. As a workaround, KMongo provides an escaping `in` MongoOperator.

```kotlin
import org.litote.kmongo.MongoOperator.`in`

println("""$`in`""") //print $in
```

### $ projection operator and formatJson() extension function

The MongoDB $ projection operator handling is the trickiest issue. With the help of KMongo, you can combine the fact that Kotlin allows $ followed by space(s) in raw strings, with the provided formatJson() KMongo extension function, to write nice raw string queries. For example:

```kotlin
import org.litote.kmongo.*
import org.litote.kmongo.MongoOperator.*

// Note the space between the $ and "cust_id", and between the $ before "amount"
// The expression then compiles, and the formatJson() extension function will remove these spaces at runtime
// You get a valid mongo query!
"""
[
  { $match: { status: "A" } },
  { $group: { _id: "$ cust_id", total: { $sum: "$ amount" } } },
  { $sort: { total: -1 } }
]
""".formatJson()
```
