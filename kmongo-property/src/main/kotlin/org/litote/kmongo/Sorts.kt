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

package org.litote.kmongo

import com.mongodb.client.model.Sorts
import org.bson.BsonDocument
import org.bson.BsonInt32
import org.bson.conversions.Bson
import kotlin.reflect.KProperty

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

/**
 * Create a sort specification for an ascending sort on the given properties.
 *
 * @param properties the properties, which must contain at least one
 * @return the sort specification
 * @mongodb.driver.manual reference/operator/meta/orderby Sort
 */
fun ascending(vararg properties: KProperty<*>): Bson = ascending(properties.toList())

/**
 * Create a sort specification for an ascending sort on the given properties.
 *
 * @param properties the properties, which must contain at least one
 * @return the sort specification
 * @mongodb.driver.manual reference/operator/meta/orderby Sort
 */
fun ascending(properties: List<KProperty<*>>): Bson = Sorts.ascending(properties.map { it.path() })

/**
 * Create a sort specification for a descending sort on the given properties.
 *
 * @param properties the properties, which must contain at least one
 * @return the sort specification
 * @mongodb.driver.manual reference/operator/meta/orderby Sort
 */
fun descending(vararg properties: KProperty<*>): Bson = descending(properties.toList())

/**
 * Create a sort specification for a descending sort on the given properties.
 *
 * @param properties the properties, which must contain at least one
 * @return the sort specification
 * @mongodb.driver.manual reference/operator/meta/orderby Sort
 */
fun descending(properties: List<KProperty<*>>): Bson = Sorts.descending(properties.map { it.path() })

/**
 * Create a sort specification for the text score meta getProjection on the given property.
 *
 * @return the sort specification
 * @mongodb.driver.manual reference/operator/getProjection/meta/#sort textScore
 */
fun <T> KProperty<T>.sortByMetaTextScore(): Bson = Sorts.metaTextScore(path())

/**
 * Combine multiple sort specifications.  If any properties are repeated, the last one takes precedence.
 *
 * @param sorts the sort specifications
 * @param ascending if the sort is ascending
 * @return the combined sort specification
 */
fun orderBy(vararg sorts: KProperty<*>, ascending: Boolean = true): Bson =
    orderBy(sorts.toList(), ascending)

/**
 * Combine multiple sort specifications.  If any properties are repeated, the last one takes precedence.
 *
 * @param sorts the sort specifications
 * @param ascending if the sort is ascending
 * @return the combined sort specification
 */
fun orderBy(sorts: List<KProperty<*>>, ascending: Boolean = true): Bson =
    orderBy(sorts.map { it to ascending }.toMap())

/**
 * Combine multiple sort specifications.  If any properties are repeated, the last one takes precedence.
 *
 * @param sorts the sort specifications
 * @return the combined sort specification
 */
fun orderBy(sorts: Map<KProperty<*>, Boolean>): Bson {
    val document = BsonDocument()
    sorts.entries.forEach {
        document.append(it.key.path(), BsonInt32(if (it.value) 1 else -1))
    }
    return document
}

