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

package org.litote.kmongo.issues

import com.fasterxml.jackson.annotation.JsonProperty
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import kotlinx.serialization.Serializable
import org.bson.Document
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.Id
import org.litote.kmongo.MongoOperator
import org.litote.kmongo.findOneAndUpdate
import org.litote.kmongo.json
import kotlin.test.assertEquals
import org.junit.Test
import org.litote.kmongo.findOne
import org.litote.kmongo.save
import org.litote.kmongo.withDocumentClass
import kotlin.test.Ignore

@Serializable
data class Test1(val isClosed:String = "a")

@Serializable
data class Test2(
    @get:JsonProperty("isClosed") @get:JvmName("funIsClosed") val isClosed:String = "a",
    @get:JsonProperty("closed") val closed:String = "b"
)



class MyFindOneAndUpdateOptions(private var myReturnDocument: ReturnDocument = ReturnDocument.AFTER) : FindOneAndUpdateOptions() {
    override fun getReturnDocument(): ReturnDocument = myReturnDocument

    override fun returnDocument(returnDocument: ReturnDocument): FindOneAndUpdateOptions {
        myReturnDocument = returnDocument
        return this
    }
}
/**
 *
 */
class Issue374IsClosed: AllCategoriesKMongoBaseTest<Test1>() {

    @Ignore
    @Test
    fun `test insert and load`() {
        val test = Test1()
        col.save(test)
        val doc = col.withDocumentClass<Document>().findOne()
        assertEquals("a", doc?.getString("isClosed"))
        assertEquals(test, col.findOne())
    }

    @Ignore
    @Test
    fun `test insert and load 2`() {
        val test = Test2()
        col.withDocumentClass<Test2>().save(test)
        val doc = col.withDocumentClass<Document>().findOne()
        assertEquals("a", doc?.getString("isClosed"))
        assertEquals("b", doc?.getString("closed"))
        assertEquals(test, col.withDocumentClass<Test2>().findOne())
    }

}