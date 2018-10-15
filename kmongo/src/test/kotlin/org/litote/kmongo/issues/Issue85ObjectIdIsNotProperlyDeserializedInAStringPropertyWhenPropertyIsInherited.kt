/*
 * Copyright (C) 2017 Litote
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

package org.litote.kmongo.issues

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.bson.Document
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.Id
import org.litote.kmongo.MongoOperator.oid
import org.litote.kmongo.findOne
import org.litote.kmongo.withDocumentClass
import kotlin.test.assertEquals

/**
 *
 */
class Issue85ObjectIdIsNotProperlyDeserializedInAStringPropertyWhenPropertyIsInherited :
    AllCategoriesKMongoBaseTest<Issue85ObjectIdIsNotProperlyDeserializedInAStringPropertyWhenPropertyIsInherited.I>() {

    data class MainData(@get:BsonId override val myId: String? = null) : I()

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    @JsonSubTypes(
        JsonSubTypes.Type(value = MainData::class, name = "test")
    )
    abstract class I {
        abstract val myId: String?
    }

    data class MainData2(@get:BsonId override val myId: Id<I2>? = null) : I2()

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    @JsonSubTypes(
        JsonSubTypes.Type(value = MainData2::class, name = "test")
    )
    abstract class I2 {
        abstract val myId: Id<I2>?
    }

    data class MainData3(@get:BsonId override val myId: ObjectId? = null) : I3()

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    @JsonSubTypes(
        JsonSubTypes.Type(value = MainData3::class, name = "test")
    )
    abstract class I3 {
        abstract val myId: ObjectId?
    }

    @Test
    fun `serialization and deserialization is ok`() {
        //insert an object id
        val document =
            Document.parse("""{ "_id" : { "$oid": "5bb4d3a8d20c290001ac67e9" },"@type":"test"}""")
        col.withDocumentClass<Document>().insertOne(document)

        //load a string
        assertEquals("5bb4d3a8d20c290001ac67e9", col.findOne()!!.myId)
    }

    @Test
    fun `serialization and deserialization is ok with Id`() {
        //insert an object id
        val document =
            Document.parse("""{ "_id" : { "$oid": "5bb4d3a8d20c290001ac67e9" },"@type":"test"}""")
        col.withDocumentClass<Document>().insertOne(document)

        //load a string
        assertEquals("5bb4d3a8d20c290001ac67e9", col.withDocumentClass<I2>().findOne()!!.myId.toString())
    }

    @Test
    fun `serialization and deserialization is ok with ObjectId`() {
        //insert an object id
        val document =
            Document.parse("""{ "_id" : { "$oid": "5bb4d3a8d20c290001ac67e9" },"@type":"test"}""")
        col.withDocumentClass<Document>().insertOne(document)

        //load a string
        assertEquals(ObjectId("5bb4d3a8d20c290001ac67e9"), col.withDocumentClass<I3>().findOne()!!.myId)
    }


}