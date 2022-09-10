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

package org.litote.kmongo.id

import org.bson.types.ObjectId
import org.litote.kmongo.Id
import kotlin.reflect.KClass

/**
 * [ObjectId] based String generator.
 */
object ObjectIdToStringGenerator : IdGenerator {

    /**
     * Generates a new StringId.
     */
    fun <T> newStringId(): StringId<T> = ObjectIdToStringGenerator.generateNewId()

    override val idClass: KClass<out Id<*>> = StringId::class

    override val wrappedIdClass: KClass<out Any> = String::class

    override fun <T> generateNewId(): StringId<T> = StringId(ObjectId().toHexString())
}