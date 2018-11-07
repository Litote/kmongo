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

/**
 * Annotated classes will generate jackson serializer & deserializer for this class at compile time.
 * See [documentation](http://litote.org/kmongo/typed-queries/#kmongo-annotation-processor).
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
@Deprecated("will be removed in next version")
annotation class JacksonData(
    /**
     * Set to internal visibility the generated classes.
     */
    val internal: Boolean = false
)