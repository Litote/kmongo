package org.litote.kmongo.issues

import org.junit.Test
import org.litote.kmongo.KMongoBaseTest
import org.litote.kmongo.MongoOperator.set
import org.litote.kmongo.findOneAndDelete
import org.litote.kmongo.findOneAndReplace
import org.litote.kmongo.findOneAndUpdate
import org.litote.kmongo.model.Friend
import kotlin.test.assertNull

/**
 *
 */
class Issue34FindOneAndUpdateOrReplaceOrDeleteCouldReturnNull : KMongoBaseTest<Friend>() {

    @Test
    fun findOneAndReplaceCouldReturnNull() {
        assertNull(col.findOneAndReplace("{}", Friend("test")))
    }

    @Test
    fun findOneAndUpdateCouldReturnNull() {
        assertNull(col.findOneAndUpdate("{}", "{$set:{name:'John'}}"))
    }

    @Test
    fun findOneAndDeleteCouldReturnNull() {
        assertNull(col.findOneAndDelete("{}"))
    }

}