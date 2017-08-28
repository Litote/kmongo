package org.litote.kmongo.issues

import org.junit.Test
import org.litote.kmongo.KMongoBaseTest
import org.litote.kmongo.MongoOperator.set
import org.litote.kmongo.findOneAndReplace
import org.litote.kmongo.findOneAndUpdate
import org.litote.kmongo.model.Friend
import kotlin.test.assertNull

/**
 *
 */
class Issue34FindOneAndUpdateOrReplaceCouldReturnNull : KMongoBaseTest<Friend>() {

    @Test
    fun findOneAndReplaceCouldReturnsNull() {
        assertNull(col.findOneAndReplace("{}", Friend("test")))
    }

    @Test
    fun findOneAndUpdateCouldReturnsNull() {
        assertNull(col.findOneAndUpdate("{}", "{$set:{name:'John'}}"))
    }

}