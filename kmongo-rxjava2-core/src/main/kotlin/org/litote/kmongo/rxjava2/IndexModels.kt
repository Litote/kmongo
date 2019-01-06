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

package org.litote.kmongo.rxjava2

import com.mongodb.client.model.IndexModel
import com.mongodb.client.model.IndexOptions
import org.litote.kmongo.util.KMongoUtil

/**
 * Construct an instance with the given keys and options.
 *
 * @param keys the index keys
 * @param options the index options
 */
@Deprecated("use org.litote.kmongo.IndexModel instead - will be removed in 4.0")
fun IndexModel.IndexModel(keys: String, options: IndexOptions = IndexOptions()): IndexModel =
    IndexModel(KMongoUtil.toBson(keys), options)