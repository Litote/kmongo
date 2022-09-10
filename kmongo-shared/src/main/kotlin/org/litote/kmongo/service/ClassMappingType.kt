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

package org.litote.kmongo.service

import java.util.ServiceLoader

private val mappingTypeProvider
        by lazy {
            ServiceLoader.load(ClassMappingTypeService::class.java)
        }

private val defaultService by lazy {
    var priority = Integer.MIN_VALUE
    var current: ClassMappingTypeService? = null
    mappingTypeProvider.iterator().apply {
        while (hasNext()) {
            val n = next()
            if (n.priority() > priority) {
                current = n
                priority = n.priority()
            }
        }
    }
    current ?: error("Service ClassMappingTypeService not found")
}

@Volatile
private var selectedService: ClassMappingTypeService? =
    (System.getProperty("org.litote.mongo.mapping.service")
    ?: System.getProperty("org.litote.mongo.test.mapping.service"))
        ?.let {
            Class.forName(it).getConstructor().newInstance() as ClassMappingTypeService
        }

private var currentService: ClassMappingTypeService
    get() = selectedService ?: defaultService
    set(value) {
        selectedService = value
    }

/**
 * [ClassMappingTypeService] currently used retrieved via [java.util.ServiceLoader].
 */
object ClassMappingType : ClassMappingTypeService by currentService {

    /**
     * Do not use the [ClassMappingTypeService.priority] method do define the service used,
     * and force the use of the specified service.
     * Use this method at startup only.
     */
    fun forceService(service: ClassMappingTypeService) {
        selectedService = service
    }
}