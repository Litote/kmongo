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

package org.bson.codecs.pojo

import org.bson.codecs.configuration.CodecRegistry
import kotlin.LazyThreadSafetyMode.PUBLICATION

/**
 *
 */
internal object KMongoPojoCodecService {

    val codecProvider: KMongoPojoCodecProvider by lazy(PUBLICATION) { KMongoPojoCodecProvider() }
    val codecRegistry: CodecRegistry by lazy(PUBLICATION) { codecProvider.codecRegistry }

    val codecProviderWithNullSerialization: KMongoPojoCodecProvider by lazy(PUBLICATION) {
        KMongoPojoCodecProvider(PropertySerialization { true })
    }
    val codecRegistryWithNullSerialization: CodecRegistry by lazy(PUBLICATION) { codecProviderWithNullSerialization.codecRegistry }
}