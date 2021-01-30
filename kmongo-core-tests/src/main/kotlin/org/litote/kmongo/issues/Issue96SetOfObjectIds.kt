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

import org.junit.Test
import org.junit.experimental.categories.Category
import org.litote.kmongo.Id
import org.litote.kmongo.JacksonMappingCategory
import org.litote.kmongo.KMongoBaseTest
import org.litote.kmongo.findOne
import org.litote.kmongo.issues.Issue96SetOfObjectIds.Task
import org.litote.kmongo.newId
import org.litote.kmongo.withDocumentClass
import kotlin.test.assertEquals

/**
 *
 */
@Category(JacksonMappingCategory::class)
class Issue96SetOfObjectIds : KMongoBaseTest<Task>() {

    data class Task(val ids: List<Id<Any>>)
    data class TaskWithStringIds(val ids: List<String>)

    @Test
    fun insertSetOfIds() {
        val task = Task(listOf(newId(), newId()))
        col.insertOne(task)
        assertEquals(task, col.findOne())
        assertEquals<Collection<String>>(
            task.ids.map { it.toString() },
            col.withDocumentClass<TaskWithStringIds>().findOne()!!.ids
        )
    }
}