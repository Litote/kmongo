package org.litote.kmongo.issues

import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.countDocuments
import org.litote.kmongo.json
import org.litote.kmongo.model.Friend
import org.litote.kmongo.regex
import kotlin.test.assertEquals

data class NewUser(val username: String)

class Issue409RegexpOption : AllCategoriesKMongoBaseTest<Friend>() {

    @Test
    fun `querying with regexoption is ok`() {
        col.insertOne(Friend("Joe"))

        assertEquals(1, col.countDocuments("{name:/J/}"))
        assertEquals(0, col.countDocuments("{name:/System#.*R/}"))

        val regex1 = "J".toRegex(setOf(RegexOption.IGNORE_CASE))
        val regex2 = "System#.*R".toRegex(setOf(RegexOption.IGNORE_CASE))

        assertEquals(1, col.countDocuments("{name:${regex1.json}}"))
        assertEquals(0, col.countDocuments("{name:${regex2.json}}"))

        assertEquals(1, col.countDocuments("{name:${regex1.toPattern().json}}"))
        assertEquals(0, col.countDocuments("{name:${regex2.toPattern().json}}"))

        assertEquals(1, col.countDocuments(Friend::name regex regex1))
        assertEquals(0, col.countDocuments(Friend::name regex regex2))

        val bson = (NewUser::username regex "^test$".toRegex(setOf(RegexOption.IGNORE_CASE))).toBsonDocument()

        assertEquals("{\"username\": {\"\$regularExpression\": {\"pattern\": \"^test\$\", \"options\": \"iu\"}}}", bson.toString())
    }


}
