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

package org.bson.codecs.pojo

import org.bson.codecs.pojo.KMongoConvention.Companion.isKotlinClass
import org.bson.codecs.pojo.annotations.BsonDiscriminator
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bson.codecs.pojo.annotations.BsonProperty
import java.util.ArrayList

/**
 * Copied from [ConventionAnnotationImpl]
 */
internal object KMongoAnnotationConvention : Convention {

    override fun apply(classModelBuilder: ClassModelBuilder<*>) {
        val type = classModelBuilder.type
        if (!type.isArray
                && !type.isEnum
                && !type.isAssignableFrom(Collection::class.java)
                && type.isKotlinClass()) {

            for (annotation in classModelBuilder.annotations) {
                processClassAnnotation(classModelBuilder, annotation)
            }

            for (propertyModelBuilder in classModelBuilder.propertyModelBuilders) {
                processPropertyAnnotations(classModelBuilder, propertyModelBuilder)
            }

            @Suppress("UNCHECKED_CAST")
            (classModelBuilder as ClassModelBuilder<Any>).instanceCreatorFactory(KotlinInstanceCreatorFactory(type.kotlin) as InstanceCreatorFactory<Any>)

            cleanPropertyBuilders(classModelBuilder)
        } else {
            Conventions.ANNOTATION_CONVENTION.apply(classModelBuilder)
        }
    }

    private fun processClassAnnotation(classModelBuilder: ClassModelBuilder<*>, annotation: Annotation) {
        if (annotation is BsonDiscriminator) {
            val key = annotation.key
            if (key != "") {
                classModelBuilder.discriminatorKey(key)
            }

            val name = annotation.value
            if (name != "") {
                classModelBuilder.discriminator(name)
            }
            classModelBuilder.enableDiscriminator(true)
        }
    }

    private fun processPropertyAnnotations(classModelBuilder: ClassModelBuilder<*>,
                                           propertyModelBuilder: PropertyModelBuilder<*>) {
        for (annotation in propertyModelBuilder.readAnnotations) {
            if (annotation is BsonProperty) {
                if ("" != annotation.value) {
                    propertyModelBuilder.readName(annotation.value)
                }
                propertyModelBuilder.discriminatorEnabled(annotation.useDiscriminator)
            } else if (annotation is BsonId) {
                classModelBuilder.idPropertyName(propertyModelBuilder.name)
            } else if (annotation is BsonIgnore) {
                propertyModelBuilder.readName(null)
            }
        }

        for (annotation in propertyModelBuilder.writeAnnotations) {
            if (annotation is BsonProperty) {
                if ("" != annotation.value) {
                    propertyModelBuilder.writeName(annotation.value)
                }
            } else if (annotation is BsonIgnore) {
                propertyModelBuilder.writeName(null)
            }
        }
    }

    private fun cleanPropertyBuilders(classModelBuilder: ClassModelBuilder<*>) {
        val propertiesToRemove = ArrayList<String>()
        for (propertyModelBuilder in classModelBuilder.propertyModelBuilders) {
            if (!propertyModelBuilder.isReadable && !propertyModelBuilder.isWritable) {
                propertiesToRemove.add(propertyModelBuilder.name)
            }
        }
        for (propertyName in propertiesToRemove) {
            classModelBuilder.removeProperty(propertyName)
        }
    }
}