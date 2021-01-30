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

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonId
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.javaType

/**
 *
 */
internal class KMongoConvention(val serialization: PropertySerialization<Any>) : Convention2 {

    companion object {
        private val metadataFqName = "kotlin.Metadata"

        fun Class<*>.isKotlinClass(): Boolean {
            return this.declaredAnnotations.singleOrNull { it.annotationClass.java.name == metadataFqName } != null
        }

        private fun getTypeData(property: KProperty<*>): TypeData<Any> {
            return getTypeData(property.returnType)
        }

        private fun getTypeData(type: KType): TypeData<Any> {
            val returnType = type.javaType

            var c: Class<Any> = Any::class.java
            if (returnType is Class<*>) {
                @Suppress("UNCHECKED_CAST")
                c = returnType as Class<Any>
            } else {
                val classifier = type.classifier
                if (classifier is KClass<*>) {
                    @Suppress("UNCHECKED_CAST")
                    c = classifier.java as Class<Any>
                }
            }
            val result = TypeData.builder<Any>(c)
            type.arguments.forEach {
                if (it.type != null) {
                    result.addTypeParameter(getTypeData(it.type as KType))
                }
            }
            return result.build()
        }

        fun getDeclaredAnnotations(property: KProperty<*>, owner: KClass<*>): List<Annotation> {
            return listOfNotNull(
                    getInstantiator(owner)?.parameters?.find { it.name == property.name }?.annotations,
                    owner.primaryConstructor?.parameters?.find { it.name == property.name }?.annotations,
                    property.javaField?.declaredAnnotations?.toList(),
                    property.getter.javaMethod?.declaredAnnotations?.toList()
            ).flatMap { it }
        }

        fun getInstantiator(owner: KClass<*>): KFunction<*>? {
            return owner.constructors.find { it.findAnnotation<BsonCreator>() != null }
                    ?: owner.companionObject?.functions?.find { it.findAnnotation<BsonCreator>() != null }
                    ?: owner.primaryConstructor
        }
    }


    @Suppress("UNCHECKED_CAST")
    override fun apply(classModelBuilder: ClassModelBuilder2<*>) {
        val type = classModelBuilder.type
        if (!type.isArray
                && !type.isEnum
                && !type.isAssignableFrom(Collection::class.java)
                && type.isKotlinClass()) {
            val instanceCreatorFactory = KotlinInstanceCreatorFactory(type.kotlin as KClass<Any>)
            (classModelBuilder as ClassModelBuilder2<Any>).instanceCreatorFactory(instanceCreatorFactory)

            classModelBuilder.type.kotlin.memberProperties.forEach {
                it.isAccessible = true
                if (!it.returnType.isSubtypeOf(Collection::class.starProjectedType)
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
                    val declaredAnnotations = getDeclaredAnnotations(it, type.kotlin)
                    val propertyBuilder =
                            PropertyModel.builder<Any>()
                                    .propertyName(propertyName)
                                    .readName(propertyName)
                                    .writeName(propertyName)
                                    .typeData(typeData)
                                    .readAnnotations(propertyMetadata.readAnnotations + declaredAnnotations)
                                    .writeAnnotations(propertyMetadata.writeAnnotations + declaredAnnotations)
                                    .propertySerialization(serialization)
                                    .propertyAccessor(PropertyAccessorImpl<Any>(propertyMetadata))



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
    }
}