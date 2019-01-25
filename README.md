 [![Gitter](https://badges.gitter.im/kmongoo/Lobby.svg)](https://gitter.im/kmongoo/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=body_badge)
 [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.litote.kmongo/kmongo/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.litote.kmongo/kmongo)
 [![Apache2 license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)
 [![Build Status](https://travis-ci.org/Litote/kmongo.png)](https://travis-ci.org/Litote/kmongo)
 [![codebeat badge](https://codebeat.co/badges/ed919223-2b9a-4b60-97d5-695b460fcbb7)](https://codebeat.co/projects/github-com-litote-kmongo-master)
 [![codecov](https://codecov.io/gh/Litote/kmongo/branch/master/graph/badge.svg)](https://codecov.io/gh/Litote/kmongo)
 [![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://github.com/KotlinBy/awesome-kotlin)
 [![Pure Kotlin](https://img.shields.io/badge/100%25-kotlin-blue.svg)](https://kotlinlang.org/)
 
# [KMongo](https://litote.org/kmongo) 
[![KMongo logo](https://litote.org/kmongo/kmongo.png "KMongo")](https://litote.org/kmongo)
 
## A Kotlin toolkit for Mongo

Documentation: [https://litote.org/kmongo](https://litote.org/kmongo)

Forum: [https://groups.google.com/forum/#!forum/kmongo](https://groups.google.com/forum/#!forum/kmongo)

### Native and Lightweight

KMongo features are available via [Kotlin extensions](https://kotlinlang.org/docs/reference/extensions.html) -
you use transparently [the core MongoDB java driver API](https://docs.mongodb.com/ecosystem/drivers/java/)
(both sync and reactive streams (ie async) drivers are supported)

#### With complete reactive streams & async support

You can use extensions for reactive streams style, [Kotlin Coroutines](https://kotlinlang.org/docs/reference/coroutines.html) or [RxJava2](http://reactivex.io/).

### Built-in Object Mapping

Object oriented programming is usually better - use Objects, not Maps. Powered by the native
[POJO Codec](https://mongodb.github.io/mongo-java-driver/3.5/bson/pojos/) or by the
[Jackson library](https://github.com/FasterXML/jackson).

### Type-safe queries

Have you already queried an ```Int``` field with a ```String``` value? 
With KMongo type-safe queries, avoid the type errors.
Provided with an optional annotation processor. 

### Mongo shell queries

You can copy/paste your queries from the Mongo shell in your IDE. Write readable source code!

## Contributors

* [Deny Prasetyo](https://github.com/jasoet)  (kmongo-coroutine)
* [Dilius](https://github.com/diliuskh) (kmongo-rxjava2)
* [Denis Kilchichakov](https://github.com/augur) (kmongo-coroutine)