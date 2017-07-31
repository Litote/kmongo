package org.litote.kmongo.issues

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.Test
import org.litote.kmongo.KMongoBaseTest
import org.litote.kmongo.findOneById
import org.litote.kmongo.json
import org.litote.kmongo.withDocumentClass
import kotlin.reflect.KClass
import kotlin.test.assertEquals

/**
 *
 */
class Issue30DBObjectInsertException : KMongoBaseTest<Issue30DBObjectInsertException.User>() {

    data class User(val _id: ObjectId,
                    val userId: String,
                    val account: DBObject)

    data class User2(val _id: ObjectId,
                     val userId: String,
                     val account: Document)

    data class Account(val test: String)

    override fun getDefaultCollectionClass(): KClass<User> {
        return User::class
    }

    @Test
    fun testSave() {
        val dbObject = BasicDBObject()
        dbObject.put("test", "t")
        dbObject.put("ob", BasicDBObject(mapOf("a" to 2)))
        val user = User(ObjectId(), "id", dbObject)
        col.insertOne(user)
        assertEquals(user, col.findOneById(user._id))
    }

    @Test
    fun testSave2() {
        val user = User2(ObjectId(), "id", Document.parse(Account("a").json))
        val c = col.withDocumentClass<User2>()
        c.insertOne(user)
        assertEquals(user, c.findOneById(user._id))
    }

}