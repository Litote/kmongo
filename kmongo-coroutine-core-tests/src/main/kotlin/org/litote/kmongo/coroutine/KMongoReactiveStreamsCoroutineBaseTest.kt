/*
 * Copyright (C) 2017/2018 Litote
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

package org.litote.kmongo.coroutine

import org.junit.Rule
import org.junit.experimental.categories.Category
import org.litote.kmongo.JacksonMappingCategory
import org.litote.kmongo.KMongoRootTest
import org.litote.kmongo.NativeMappingCategory
import org.litote.kmongo.SerializationMappingCategory
import org.litote.kmongo.reactivestreams.ReactiveStreamsFlapdoodleRule
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass

/**
 *
 */
@Category(JacksonMappingCategory::class, NativeMappingCategory::class, SerializationMappingCategory::class)
open class KMongoReactiveStreamsCoroutineBaseTest<T : Any> : KMongoRootTest() {

    @Suppress("LeakingThis")
    @Rule
    @JvmField
    val rule = ReactiveStreamsFlapdoodleRule(getDefaultCollectionClass())

    val mongoClient by lazy { rule.mongoClient.coroutine }

    val col by lazy { rule.col.coroutine }

    val database by lazy { rule.database.coroutine }

    inline fun <reified T : Any> getCollection(): CoroutineCollection<T> = rule.getCollection<T>().coroutine

    @Suppress("UNCHECKED_CAST")
    open fun getDefaultCollectionClass(): KClass<T> =
        ((this::class.java.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>).kotlin

}