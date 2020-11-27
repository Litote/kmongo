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
package org.litote.kmongo.issues

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.junit.Test
import org.litote.kmongo.Id
import org.litote.kmongo.coroutine.KMongoReactiveStreamsCoroutineBaseTest
import org.litote.kmongo.newId
import kotlin.test.assertEquals

@Serializable
data class Tag(
    @Contextual
    @SerialName("_id") val id: Id<Tag>? = newId(),
    val displayName: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 *
 */
class Issue228WrappedObject : KMongoReactiveStreamsCoroutineBaseTest<Tag>() {

    @Test
    fun testSaveAndLoad() = runBlocking {
        val tag = Tag(displayName = "foobar")
        col.insertOne(tag)
        assertEquals(tag, col.findOne())
    }

}