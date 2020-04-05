## Release Notes
 
 Available on [Github](https://github.com/Litote/kmongo/releases).
 
## Versioning scheme
 
 KMongo follows the Mongo java driver versioning. 
 
 So a KMongo 3.4.x version uses a 3.4+ Mongo java driver version, a 3.5.x uses a 3.5+ and so on.
 
 The .x is used for KMongo patch releases.
 
## Upgrading to 4.x from 3.12.x

The 4.0 release is a major release for the mongo java driver as per the definition of semantic versioning.
As such, users that upgrade to this release should expect breaking changes. 

Please consult the list of breaking changes of the java driver:  [https://mongodb.github.io/mongo-java-driver/4.0/upgrading/](https://mongodb.github.io/mongo-java-driver/4.0/upgrading/)

KMongo introduces also breaking changes on its own:

- The mongodb-driver-sync is used for synchronous versions, the mongodb-driver-legacy is not used anymore
- The reactive driver is used for asynchronous versions, as the java async driver is not available anymore
- All KMongo deprecated methods and classes in 3.12 are removed in 4.0
 
## KMongo dependencies
 
 - Mongo Java driver [sync](https://mongodb.github.io/mongo-java-driver/) or [reactive](https://mongodb.github.io/mongo-java-driver/4.0/driver-reactive/)
 - [Kotlin stdlib](https://kotlinlang.org/api/latest/jvm/stdlib/)
 - [Kotlin reflect lib](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/)
 
For the KMongo versions using the Jackson mapping engine:
 
 - [Jackson](https://github.com/FasterXML/jackson-databind)
 - [Jackson for Kotlin](https://github.com/FasterXML/jackson-module-kotlin)
 - [Bson4Jackson](https://www.michel-kraemer.com/binary-json-with-bson4jackson/)
 
For kotlinx serialization mapping:
 
 - [kbson](https://github.com/jershell/kbson)
