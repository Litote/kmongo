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

package org.litote.kmongo.reactor

import com.mongodb.reactivestreams.client.MongoCollection
import org.junit.Rule
import org.junit.experimental.categories.Category
import org.litote.kmongo.JacksonMappingCategory
import org.litote.kmongo.KMongoRootTest
import org.litote.kmongo.NativeMappingCategory
import org.litote.kmongo.SerializationMappingCategory
import org.litote.kmongo.model.Friend
import org.litote.kmongo.reactivestreams.ReactiveStreamsFlapdoodleRule
import kotlin.reflect.KClass

/**
 *
 */
@Category(JacksonMappingCategory::class, NativeMappingCategory::class, SerializationMappingCategory::class)
open class KMongoReactiveStreamsReactorBaseTest<T : Any> : KMongoRootTest() {

    @Suppress("LeakingThis")
    @Rule
    @JvmField
    val rule = ReactiveStreamsFlapdoodleRule(getDefaultCollectionClass())

    val col by lazy { rule.col }

    val database by lazy { rule.database }

    inline fun <reified T : Any> getCollection(): MongoCollection<T> = rule.getCollection<T>()

    @Suppress("UNCHECKED_CAST")
    open fun getDefaultCollectionClass(): KClass<T> = Friend::class as KClass<T>

}
