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
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.bson.Document
import org.junit.Test
import org.litote.kmongo.coroutine.KMongoReactiveStreamsCoroutineBaseTest
import org.litote.kmongo.serialization.registerModule
import kotlin.test.assertEquals

interface SchedulerJob3

@Serializable
data class UnmuteSchedulerJob3(
    val guildId: String = "id",
) : SchedulerJob3

@Serializable
data class Container(val job: SchedulerJob3)

/**
 *
 */
class Issue248ContainerWithSimplePolymorphism : KMongoReactiveStreamsCoroutineBaseTest<Container>() {

    @ExperimentalSerializationApi
    @InternalSerializationApi
    @Test
    fun `test insert and load`() = runBlocking {
        registerModule(
            SerializersModule {
                polymorphic(baseClass = SchedulerJob3::class) {
                    subclass(UnmuteSchedulerJob3::class)
                }
            }
        )
        val job = Container(UnmuteSchedulerJob3())
        col.insertOne(job)
        println(col.withDocumentClass<Document>().findOne())
        assertEquals(job, col.findOne())
    }
}