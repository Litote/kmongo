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

import org.bson.codecs.pojo.annotations.BsonId
import org.junit.experimental.categories.Category
import org.litote.kmongo.Id
import org.litote.kmongo.JacksonMappingCategory
import org.litote.kmongo.KMongoBaseTest
import org.litote.kmongo.NativeMappingCategory
import org.litote.kmongo.findOneById
import org.litote.kmongo.issues.Issue195BsonInvalidIdException.TaskEntity
import org.litote.kmongo.newId
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 *
 */
@Category(JacksonMappingCategory::class, NativeMappingCategory::class)
class Issue195BsonInvalidIdException : KMongoBaseTest<TaskEntity>() {

    data class TaskEntity(
        @BsonId
        override val id: Id<TaskEntity>
    ) : HasId<TaskEntity>

    interface HasId<T> {
        val id: Id<T>
    }

    @Test
    fun test() {
        val task1 = TaskEntity(
            id = newId()
        )
        col.insertOne(task1)

        assertEquals(task1, col.findOneById(task1.id))
    }
}