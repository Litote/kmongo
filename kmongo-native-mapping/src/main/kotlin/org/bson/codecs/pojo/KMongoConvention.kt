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

package org.bson.codecs.pojo

import org.bson.codecs.pojo.annotations.BsonId
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.javaType

/**
 *
 */
internal class KMongoConvention(val serialization: PropertySerialization<Any>) : Convention {

    private fun getTypeData(property: KProperty<*>): TypeData<Any> {
        @Suppress("UNCHECKED_CAST")
        return TypeData.builder<Any>(property.returnType.javaType as Class<Any>).build()
    }

    @Suppress("UNCHECKED_CAST")
    override fun apply(classModelBuilder: ClassModelBuilder<*>) {
        val type = classModelBuilder.type
        if (!type.isArray && !type.isEnum && !type.isAssignableFrom(Collection::class.java)) {
            val instanceCreatorFactory = KotlinInstanceCreatorFactory<Any>(type.kotlin as KClass<Any>)
            (classModelBuilder as ClassModelBuilder<Any>).instanceCreatorFactory(instanceCreatorFactory)

            classModelBuilder.type.kotlin.memberProperties.forEach {
                it.isAccessible = true
                if (it.returnType.javaType is Class<*>
                        && it.javaField?.run { !Modifier.isTransient(modifiers) } ?: true) {
                    classModelBuilder.removeProperty(it.name)
                    val typeData = getTypeData(it)
                    val propertyMetadata = PropertyMetadata<Any>(it.name, classModelBuilder.type.simpleName, typeData)
                    propertyMetadata.getter = it.getter.javaMethod
                    propertyMetadata.field(it.javaField)
                    if (it is KMutableProperty<*>) {
                        propertyMetadata.setter = it.setter.javaMethod
                    }

                    val propertyName = it.name
                    val propertyBuilder =
                            PropertyModel.builder<Any>()
                                    .propertyName(propertyName)
                                    .readName(propertyName)
                                    .writeName(propertyName)
                                    .typeData(typeData)
                                    .readAnnotations(propertyMetadata.readAnnotations)
                                    .writeAnnotations(propertyMetadata.writeAnnotations)
                                    .propertySerialization(serialization)
                                    .propertyAccessor(PropertyAccessorImpl<Any>(propertyMetadata));

                    classModelBuilder.addProperty(propertyBuilder)

                    if (classModelBuilder.idPropertyName == null) {
                        if (propertyName == "_id"
                                || propertyName == "id"
                                || it.javaField?.isAnnotationPresent(BsonId::class.java) == true
                                || it.getter.javaMethod?.isAnnotationPresent(BsonId::class.java) == true) {
                            classModelBuilder.idPropertyName(propertyName)
                        }
                    }
                }
            }
        }
        /*if (type is Pair<*, *>) {
            classModelBuilder
                    .propertyNameToTypeParameterMap(
                            mapOf(
                                    "first" to TypeParameterMap.builder().addIndex(0, 0).build(),
                                    "second" to TypeParameterMap.builder().addIndex(1, 1).build()
                            )
                    )
        } */
    }
}