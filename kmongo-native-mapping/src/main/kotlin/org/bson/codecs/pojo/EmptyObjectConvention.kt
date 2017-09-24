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

/**
 *
 */
internal class EmptyObjectConvention() : Convention {

    override fun apply(classModelBuilder: ClassModelBuilder<*>) {
        //if there is no property, the pojo model is not usable. This is a mongo driver bug, but until they fix it...
        if (classModelBuilder.propertyModelBuilders.isEmpty()) {
            val name = "toString"
            val typeData = TypeData.builder(String::class.java).build()
            val propertyMetadata = PropertyMetadata(name, classModelBuilder.type.simpleName, typeData)

            classModelBuilder.addProperty(
                    PropertyModel.builder<String>()
                            .propertyName(name)
                            .readName(name)
                            .typeData(typeData)
                            .propertySerialization(PropertyModelSerializationImpl())
                            .propertyAccessor(PropertyAccessorImpl(propertyMetadata))
            )
        }
    }
}