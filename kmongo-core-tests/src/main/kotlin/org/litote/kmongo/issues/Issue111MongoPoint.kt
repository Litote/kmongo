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

import com.mongodb.client.model.geojson.Point
import com.mongodb.client.model.geojson.Position
import org.junit.Test
import org.junit.experimental.categories.Category
import org.litote.kmongo.KMongoBaseTest
import org.litote.kmongo.NativeMappingCategory
import org.litote.kmongo.findOne
import org.litote.kmongo.issues.Issue111MongoPoint.Data
import kotlin.test.assertEquals

/**
 *
 */
class Issue111MongoPoint : KMongoBaseTest<Data>() {

    data class Data(val location: Point)

    @Category(NativeMappingCategory::class)
    @Test
    fun `deserializing is ok`() {
        val data = Data(Point(Position(20.0, 20.0)))
        col.insertOne(data)
        assertEquals(data, col.findOne())
    }

}