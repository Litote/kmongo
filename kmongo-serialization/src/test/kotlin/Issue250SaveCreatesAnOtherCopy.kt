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

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.Id
import org.litote.kmongo.findOne
import org.litote.kmongo.newId
import java.time.Instant
import java.time.Instant.now
import kotlin.test.assertTrue
import org.litote.kmongo.save
import kotlin.test.assertEquals

@Serializable
sealed class Case {
    abstract val action: Action

    abstract val targetUserId: String

    /**
     * The moderator that created the case
     */
    abstract val initialModeratorId: String

    /**
     * The moderator that last touched the case
     */
    abstract var lastModeratorId: String?

    @Contextual
    abstract val createdAt: Instant

    @Contextual
    abstract var lastChangedAt: Instant?

    abstract val guildId: String

    abstract var resolved: Boolean

    abstract val caseId: Int

    abstract var reason: String?

    abstract var modLogMessageId: String?

    @Contextual
    @SerialName("_id")
    abstract val mongoId: Id<Case>

    fun isChanged(): Boolean {
        return lastChangedAt != null
    }

    abstract fun isLogged(): Boolean

}

@Serializable
data class UnmuteCase(
    override val targetUserId: String = "",
    override val initialModeratorId: String = "",
    override var lastModeratorId: String? = null,
    @Contextual override val createdAt: Instant = now(),
    @Contextual override var lastChangedAt: Instant? = null,
    override val guildId: String = "",
    override var resolved: Boolean = false,
    override val caseId: Int = 1,
    override var reason: String? = null,
    override var modLogMessageId: String? = null,
    @Contextual @SerialName("_id") override val mongoId: Id<Case> = newId()
) : Case() {
    override val action = Action.UNMUTE

    override fun isLogged(): Boolean = true

}


/**
 *
 */
class Issue250SaveCreatesAnOtherCopy : AllCategoriesKMongoBaseTest<UnmuteCase>() {

    @Test
    fun `test insert and load`() {
        col.insertOne(UnmuteCase())
        val loaded = col.findOne()!!
        loaded.resolved = true
        loaded.lastChangedAt = now()
        col.save(loaded)
        assertEquals(1, col.countDocuments())
    }
}