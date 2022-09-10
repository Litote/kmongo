/*
 * Copyright (C) 2016/2022 Litote
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

package org.litote.kmongo

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import org.litote.kmongo.model.Coordinate
import org.litote.kmongo.service.ClassMappingType
import org.litote.kmongo.util.ObjectMappingConfiguration
import java.time.Instant
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Serializable
data class NullableFriend(
    var name: String?,
    val address: String?,
    @Contextual
    val _id: ObjectId? = null,
    val coordinate: Coordinate? = null,
    val tags: List<String> = emptyList(),
    @Contextual
    val creationDate: Instant? = null
) {

    constructor(name: String) : this(name, null, null)
}

/**
 *
 */
class PersistingNotNullTest : KMongoBaseTest<NullableFriend>() {

    @Before
    fun before() {
        ObjectMappingConfiguration.serializeNull = !ClassMappingType.defaultNullSerialization
        ClassMappingType.resetConfiguration()
    }

    @After
    fun after() {
        ObjectMappingConfiguration.serializeNull = ClassMappingType.defaultNullSerialization
        ClassMappingType.resetConfiguration()
    }

    @Category(JacksonMappingCategory::class)
    @Test
    fun testInsertNullFieldForJacksonMapping() {
        col.insertOne(NullableFriend("Joe"))
        val doc = database.getCollection("nullableFriend").findOne()!!

        assertTrue(doc.containsKey("name"))
        assertFalse(doc.containsKey("address"))
    }

    @Category(JacksonMappingCategory::class)
    @Test
    fun testSaveNullFieldForJacksonMapping() {
        val document = NullableFriend("Joe")
        col.save(document)
        val doc = database.getCollection("nullableFriend").findOne()!!

        assertTrue(doc.containsKey("name"))
        assertFalse(doc.containsKey("address"))

        col.save(document)
        val doc2 = database.getCollection("nullableFriend").findOne()!!

        assertTrue(doc2.containsKey("name"))
        assertFalse(doc2.containsKey("address"))
    }

    @Category(NativeMappingCategory::class)
    @Test
    fun testInsertNullFieldForNativeMapping() {
        col.insertOne(NullableFriend("Joe"))
        val doc = database.getCollection("nullableFriend").findOne()!!

        assertTrue(doc.containsKey("name"))
        assertTrue(doc.containsKey("address"))
    }

    @Category(NativeMappingCategory::class)
    @Test
    fun testSaveNullFieldForNativeMapping() {
        val document = NullableFriend("Joe")
        col.save(document)
        val doc = database.getCollection("nullableFriend").findOne()!!

        assertTrue(doc.containsKey("name"))
        assertTrue(doc.containsKey("address"))

        col.save(document)
        val doc2 = database.getCollection("nullableFriend").findOne()!!

        assertTrue(doc2.containsKey("name"))
        assertTrue(doc2.containsKey("address"))
    }

    @Category(SerializationMappingCategory::class)
    @Test
    fun testInsertNullFieldForKotlinxSerializationMapping() {
        col.insertOne(NullableFriend("Joe"))
        val doc = database.getCollection("nullableFriend").findOne()!!

        assertTrue(doc.containsKey("name"))
        assertFalse(doc.containsKey("address"))
    }

    @Category(SerializationMappingCategory::class)
    @Test
    fun testSaveNullFieldForKotlinxSerializationMapping() {
        val document = NullableFriend("Joe")
        col.save(document)
        val doc = database.getCollection("nullableFriend").findOne()!!

        assertTrue(doc.containsKey("name"))
        assertFalse(doc.containsKey("address"))

        col.save(document)
        val doc2 = database.getCollection("nullableFriend").findOne()!!

        assertTrue(doc2.containsKey("name"))
        assertFalse(doc2.containsKey("address"))
    }
}