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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import org.junit.Test
import org.litote.kmongo.AllCategoriesKMongoBaseTest
import org.litote.kmongo.Id
import org.litote.kmongo.MongoOperator
import org.litote.kmongo.MongoOperator.*
import org.litote.kmongo.eq
import org.litote.kmongo.id.toId
import org.litote.kmongo.json
import org.litote.kmongo.newId
import org.litote.kmongo.toId
import kotlin.test.assertEquals

@Serializable
data class UserSubscription(
    @SerialName("_id")
    @Contextual val id: Id<UserSubscription> = newId(),
    @Contextual val user: Id<User>
)

/**
 *
 */
class Issue328FieldIdQueryFilterSerialization : AllCategoriesKMongoBaseTest<UserSubscription>() {

    @Test
    fun testSerialization() {
        val json = (UserSubscription::user eq ObjectId("6244ba8b131ffc0267c9a10c").toId()).json
        assertEquals("""{"user": {"$oid": "6244ba8b131ffc0267c9a10c"}}""", json)

        val json2 = (UserSubscription::user eq "6244ba8b131ffc0267c9a10c".toId()).json
        assertEquals("""{"user": "6244ba8b131ffc0267c9a10c"}""", json2)
    }
}