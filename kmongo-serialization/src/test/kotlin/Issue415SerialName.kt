package org.litote.kmongo.issues

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.MongoOperator.set
import org.litote.kmongo.model.Friend
import org.litote.kmongo.pos
import org.litote.kmongo.setValue
import kotlin.test.assertEquals

@Serializable
data class BookEpisode(

    @SerialName("paragraphsZh")
    val baiduParagraphs: List<String>?,
    val youdaoParagraphs: List<String>? = null,
)

class Issue415SerialName : AllCategoriesKMongoBaseTest<Friend>() {

    @Test
    fun `test insert and load`() {
        assertEquals(
            "{\"$set\": {\"youdaoParagraphs.0\": \"test\"}}",
            setValue(BookEpisode::youdaoParagraphs.pos(0), "test").toBsonDocument().toString()
        )
        assertEquals(
            "{\"$set\": {\"paragraphsZh.0\": \"test\"}}",
            setValue(BookEpisode::baiduParagraphs.pos(0), "test").toBsonDocument().toString()
        )
    }
}
