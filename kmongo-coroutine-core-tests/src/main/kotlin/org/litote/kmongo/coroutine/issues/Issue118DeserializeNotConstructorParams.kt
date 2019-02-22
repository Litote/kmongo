/*
 * Copyright (C) 2017/2018 Litote
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

package org.litote.kmongo.coroutine.issues

import kotlinx.coroutines.runBlocking
import org.bson.codecs.pojo.annotations.BsonId
import org.junit.Test
import org.litote.kmongo.coroutine.KMongoReactiveStreamsCoroutineBaseTest
import org.litote.kmongo.coroutine.issues.Issue118DeserializeNotConstructorParams.Document2
import kotlin.test.assertEquals

/**
 *
 */
class Issue118DeserializeNotConstructorParams : KMongoReactiveStreamsCoroutineBaseTest<Document2>() {

    data class Document2(@BsonId val id: Int) {
        var field1: String? = null
        var field2: String? = null
    }

    @Test
    fun canFindOne() = runBlocking {
        col.insertOne(
            Document2(1)
                .apply {
                    field1 = "1"
                    field2 = "2"
                }
        )
        assertEquals(1, col.findOne()?.id)
        assertEquals("1", col.findOne()?.field1)
        assertEquals("2", col.findOne()?.field2)
    }
}