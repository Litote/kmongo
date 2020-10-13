/*
 * Copyright (C) 2016/2020 Litote
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.Document
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.findOne
import org.litote.kmongo.withDocumentClass
import kotlin.test.assertEquals


@Serializable
data class SealedContainer(val v: SealedValue)

@Serializable
sealed class SealedValue

@Serializable
@SerialName("int-value")
data class IntValue(val value: Int) : SealedValue()

/**
 *
 */
class Issue229SealedClass : AllCategoriesKMongoBaseTest<SealedContainer>() {

    @Test
    fun insertAndLoad() {
        val value = SealedContainer(IntValue(1))
        col.insertOne(value)
        val doc = col.withDocumentClass<Document>().findOne()
        assertEquals("int-value", doc?.getEmbedded(listOf("v", "___type"), ""))
        assertEquals(1, doc?.getEmbedded(listOf("v", "value"), 0))

        assertEquals(value, col.findOne())
    }

    @Test
    fun insertAndLoadNotEmbedded() {
        val value = IntValue(1)
        col.withDocumentClass<SealedValue>().insertOne(value)
        val doc = col.withDocumentClass<Document>().findOne()
        assertEquals("int-value", doc?.getString("___type"))
        assertEquals(1, doc?.getInteger("value", 0))

        assertEquals(value, col.withDocumentClass<SealedValue>().findOne())
    }

}