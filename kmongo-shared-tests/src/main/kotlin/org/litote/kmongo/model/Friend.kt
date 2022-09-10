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
package org.litote.kmongo.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import java.time.Instant

/**
 *
 */
@Serializable
data class Friend(
    var name: String?,
    val address: String?,
    @Contextual
    val _id: ObjectId? = null,
    val coordinate: Coordinate? = null,
    val tags: List<String> = emptyList(),
    @Contextual
    val creationDate: Instant? = null
) {

    constructor(name: String) : this(name, null, null)

    constructor(name: String?, coordinate: Coordinate) : this(name, null, null, coordinate)

    constructor(_id: ObjectId, name: String) : this(name, null, _id)
}