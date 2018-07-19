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

package org.litote.kmongo.property

import com.mongodb.client.model.Filters
import org.bson.codecs.pojo.annotations.BsonId
import org.junit.Test
import org.litote.kmongo.colProperty
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.path
import java.math.BigDecimal
import java.util.Locale
import kotlin.test.assertEquals

/**
 *
 */
class KPropertyPathTest {

    data class Friend(
        val name: String,
        val i: Int,
        @BsonId val id: String,
        val gift: Gift,
        val allGifts: List<Gift>,
        val localeMap: Map<Locale, Gift>
    )

    data class Gift(val amount: BigDecimal)

    @Test
    fun `simple equals test`() {
        assertEquals(
            Filters.eq("name", "A").toString(),
            (Friend::name eq "A").toString()
        )
        assertEquals(
            Filters.eq("gift.amount", BigDecimal.ZERO).toString(),
            ((Friend::gift / Gift::amount) eq BigDecimal.ZERO).toString()
        )
    }

    @Test
    fun `test array positional operator`() {
        val p = Friend::allGifts.colProperty
        assertEquals("allGifts.\$", p.posOp.path)
        assertEquals("allGifts.amount", (p / Gift::amount).path())
        assertEquals("allGifts.\$.amount", (p.posOp / Gift::amount).path())
    }

    @Test
    fun `test array all positional operator`() {
        val p = Friend::allGifts.colProperty
        assertEquals("allGifts.\$[]", p.allPosOp.path)
        assertEquals("allGifts.amount", (p / Gift::amount).path())
        assertEquals("allGifts.\$[].amount", (p.allPosOp / Gift::amount).path())
    }

    @Test
    fun `test array filtered positional operator`() {
        val p = Friend::allGifts.colProperty
        assertEquals("allGifts.\$[a]", p.filteredPosOp("a").path)
        assertEquals("allGifts.amount", (p / Gift::amount).path())
        assertEquals("allGifts.\$[a].amount", (p.filteredPosOp("a") / Gift::amount).path())
    }

}