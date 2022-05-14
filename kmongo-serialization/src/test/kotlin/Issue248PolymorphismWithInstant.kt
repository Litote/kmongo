/*
 * Copyright (C) 2016/2021 Litote
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

import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.Id
import org.litote.kmongo.findOne
import org.litote.kmongo.newId
import org.litote.kmongo.serialization.registerModule
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals

enum class Action {
    UNMUTE
}

interface SchedulerJob {
    val id: Id<SchedulerJob>
    val action: Action

    val startTime: Instant
    val duration: Long
    val targetUserId: String
    val guildId: String

    fun shouldBePersisted(): Boolean {
        return duration >= Duration.ofMinutes(15).toMillis()
    }

}

@Serializable
data class UnmuteSchedulerJob(
    @Contextual @SerialName("_id") override val id: Id<SchedulerJob> = newId(),
    @Contextual override val startTime: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS),
    override val duration: Long = 10L,
    override val targetUserId: String = "id",
    override val guildId: String = "id",
    val reason: String? = null
) : SchedulerJob {
    override val action: Action = Action.UNMUTE
}

/**
 *
 */
class Issue248PolymorphismWithInstant : AllCategoriesKMongoBaseTest<SchedulerJob>() {

    @ExperimentalSerializationApi
    @InternalSerializationApi
    @Test
    fun `test insert and load`() {
        registerModule(
            SerializersModule {
                polymorphic(baseClass = SchedulerJob::class) {
                    subclass(UnmuteSchedulerJob::class)
                }
            }
        )
        val job = UnmuteSchedulerJob()
        col.insertOne(job)
        assertEquals(
            job,
            col.findOne()
        )
    }
}