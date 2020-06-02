# Object Mapping

Query results are automatically mapped to objects.

## Set your _id

To manage Mongo ```_id```, a class must have one ```_id``` property
 or a property annotated with the ```@BsonId``` annotation.
 
> For **kotlinx serialization**, ```@BsonId``` is not supported. Use ```@SerialName("_id")``` on the id property.
 
If there is no such field in your class, an ```ObjectId``` _id is generated on server side.

### KMongo Id type

KMongo provides a optional dedicated [Id](http://litote.org/kmongo/dokka/kmongo/org.litote.kmongo/-id.html) type.
With this Id class, the document identifier is typed. 

So for example:  
 
```kotlin
class LightSaber(val _id: Id<LightSaber> = newId())
class Jedi(
    @BsonId val key: Id<Jedi> = newId(), 
    //set of typed ids, now I see what it is!
    val sabers:Set<Id<LightSaber>> = emptySet()
)
``` 

It is easy to transform an ObjectId in Id<> with the *toId()* extension:

```kotlin
LightSaber(ObjectId("myId").toId())
``` 

> For **kotlinx serialization** add ```@ContextualSerialization``` annotation on ```Id``` properties.

#### KMongo Id does not depend of Mongo nor KMongo lib
                     
This is useful if you need to transfer your data object from a mongo backend 
to a frontend that does not need to know your storage engine.

You just have to add the ```kmongo-id``` dependency in the frontend to compile. 

- with Maven

```xml
<dependency>
  <groupId>org.litote.kmongo</groupId>
  <artifactId>kmongo-id</artifactId>
  <version>4.0.2</version>
</dependency>
```

- or Gradle

```
compile 'org.litote.kmongo:kmongo-id:4.0.2'
```

#### Id <> Json Jackson serialization

If you use Jackson to serialize your objects to json (in order to transfer your data between frontend and backend),
add the ```kmongo-id-jackson``` dependency and register the ```IdJacksonModule``` module:

```kotlin 
mapper.registerModule(IdJacksonModule())
```

#### Id <> Json Gson serialization

If you prefer to use Gson, you need to use this code:

```kotlin 
val gsonBuilder = GsonBuilder();
gsonBuilder.registerTypeAdapter(Id::class.java,
        JsonSerializer<Id<Any>> { id, _, _ -> JsonPrimitive(id?.toString()) })
gsonBuilder.registerTypeAdapter(Id::class.java,
        JsonDeserializer<Id<Any>> { id, _, _ -> id.asString.toId() })
val gson = gsonBuilder.create()
```

### Other _id types

The Id type is optional, 
you can use ```String```, ```org.bson.types.ObjectId``` of whatever you need as _id type.

## Date support

KMongo provides built-in support for these "Date" classes:

- java.util.Date
- java.util.Calendar
- java.time.LocalDateTime
- java.time.LocalDate
- java.time.LocalTime
- java.time.ZonedDateTime
- java.time.OffsetDateTime
- java.time.OffsetTime
- java.time.Instant
- java.time.ZoneId

Dates are always stored in Mongo in UTC. For ```Calendar```, ```ZonedDateTime```, ```OffsetDateTime``` and ```OffsetTime```,
whatever is the timezone of the saved date, you will get an UTC date when loading the date from Mongo.

So, in this case, the loaded date will not usually be equals to the saved date - the Instant part of the date of course is the same. If you need to check equality, use a method like ```OffsetDateTime#withOffsetSameInstant``` on the loaded date.

## Register a custom mongo Codec

Use  [ObjectMappingConfiguration.addCustomCodec](https://litote.org/kmongo/dokka/kmongo/org.litote.kmongo.util/-object-mapping-configuration/index.html) function in order to register a custom codec.
You have also the option to use [custom Jackson modules](https://litote.org/kmongo/object-mapping/#register-a-custom-jackson-module) or custom [kotlinx.serialization modules](https://litote.org/kmongo/object-mapping/#additional-modules-and-serializers).

## How to choose the mapping engine

### The Jackson choice

With the [Jackson library](https://github.com/FasterXML/jackson), you get full support of one of the most powerful data-binding java engine, in order to map your data objects.

If you need custom serialization or deserialization, you will not be blocked, you can implement it.

And you can use all [Jackson annotations](https://github.com/FasterXML/jackson-annotations/wiki/Jackson-Annotations).

Also, if you already use Jackson for json serialization (for rest services for example), the library is already in your list of dependencies.

However, if you don't use already Jackson, you add a new quite complex library to your dependencies.

#### Register a custom Jackson module

Use [registerBsonModule](http://litote.org/kmongo/dokka/kmongo/org.litote.kmongo.util/-k-mongo-configuration/register-bson-module.html) function.

### The POJO Codec "native" choice

Started in 2.5.0 (July 2017), the java driver introduces a new [POJO mapping framework](https://mongodb.github.io/mongo-java-driver/3.5/bson/pojos/).

KMongo uses it to provide object mapping for Kotlin. No other dependency than the core java mongo driver is required.

All the common cases are covered. However, there are some limitations. For example, top level POJO cannot contain any type parameters.

### The kotlinx serialization choice

Starting with 3.11.2 version, KMongo also supports [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) mapping.

The main advantage of this kind of mapping is that **almost no (slow) reflection** is involved.

#### Additional Modules and Serializers

Use ```registerModule``` or ```registerSerializer``` functions in order to register new 
(for example polymorphic) kotlinx Module or Serializer.

#### Avoid completely reflection

By default, there is still a little bit of reflection involved when persisting a document, in order to retrieve dynamically 
the id of the document instance. You can declare your own ```IdController```,
using the ```changeIdController``` function to avoid completely reflection. Then you can exclude the kotlin-reflect
dependency!

### Conclusion

- We expect that, for most of the projects, the "native" bindings will be perfectly OK. 

- For complex projects, or if you have already Jackson skills, the Jackson object mapping is a really nice choice.

- Kotlin experts will prefer kotlinx serialization, as it should provide the best performance.

Choose your poison! :)

See also [Performance section](../performance).