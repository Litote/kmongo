## Release Notes
 
 Available on [Github](https://github.com/Litote/kmongo/releases).
 
## Versioning scheme
 
 KMongo follows the Mongo java driver versioning. 
 
 So a KMongo 3.4.x version uses a 3.4+ Mongo java driver version, a 3.5.x uses a 3.5+ and so on.
 
 The .x is used for KMongo patch releases.
 
## KMongo dependencies
 
 - Mongo Java driver [sync](https://mongodb.github.io/mongo-java-driver/) or [async](https://mongodb.github.io/mongo-java-driver/3.7/driver-async/)
 - [Kotlin stdlib](https://kotlinlang.org/api/latest/jvm/stdlib/)
 - [Kotlin reflect lib](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/)
 
For the KMongo versions using the Jackson mapping engine:
 
 - [Jackson](https://github.com/FasterXML/jackson-databind)
 - [Jackson for Kotlin](https://github.com/FasterXML/jackson-module-kotlin)
 - [Bson4Jackson](https://www.michel-kraemer.com/binary-json-with-bson4jackson/)
 
For kotlinx serialization mapping:
 
 - [kbson](https://github.com/jershell/kbson)
