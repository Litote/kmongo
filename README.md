 [![KMongo logo](http://litote.org/kmongo/kmongo.png "KMongo")](http://litote.org/kmongo) &nbsp; [![Build Status](https://travis-ci.org/Litote/kmongo.png)](https://travis-ci.org/Litote/kmongo)
# KMongo 
 
## Mongo shell query & object mapping for Kotlin

Apache 2 license

**the current version is Alpha 1**  

***

## Roadmap

### Beta 1 / End of may

 * sync module
 * test suite done
 * write doc
 * build src & release plugin
 * deploy to repo

### Beta 2 / End of june 

 * @MongoId annotation to support id in other field than _id
 * @StoredToObjectId annotation to transform String to ObjectId 
 * allow mapper customization 
 
### Beta 3 / End of july 

 * check support of all extended bson types (dates)
 * support bulk operations
 * create/drop indexes
 
### RC & R / End of august 

 * discuss providing json to *Options classes mapping 
 * follow mongo driver versioning
 * simple benchmark
