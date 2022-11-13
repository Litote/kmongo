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

import org.bson.codecs.pojo.annotations.BsonId
import org.junit.Test
import org.litote.kmongo.util.KMongoUtil
import java.util.UUID
import kotlin.test.assertFalse

data class DocumentWithId(
    @BsonId val id: UUID,
    val name: String
)

data class DocumentWithUnderscoreId(
    @BsonId val _id: UUID,
    val name: String
)

/**
 *
 */
class Issue383FilterId() {

    @Test
    fun `filterIdToBson should filter id property`() {
        val id = UUID.randomUUID()
        val bson = KMongoUtil.filterIdToBson(DocumentWithId(id = id, name = "test"), false)
        assertFalse(bson.toBsonDocument().contains("_id"))
    }

    @Test
    fun `filterIdToBson should filter _id property`() {
        val id = UUID.randomUUID()
        val bson = KMongoUtil.filterIdToBson(DocumentWithUnderscoreId(_id = id, name = "test"), false)
        assertFalse(bson.toBsonDocument().contains("_id"))
    }
}