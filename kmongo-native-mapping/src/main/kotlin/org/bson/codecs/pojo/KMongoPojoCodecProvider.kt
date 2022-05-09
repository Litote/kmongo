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

import com.mongodb.DBRefCodecProvider
import com.mongodb.DocumentToDBRefTransformer
import com.mongodb.client.gridfs.codecs.GridFSFileCodecProvider
import com.mongodb.client.model.geojson.codecs.GeoJsonCodecProvider
import org.bson.codecs.BsonValueCodecProvider
import org.bson.codecs.Codec
import org.bson.codecs.DocumentCodecProvider
import org.bson.codecs.IterableCodecProvider
import org.bson.codecs.MapCodecProvider
import org.bson.codecs.ValueCodecProvider
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import kotlin.reflect.KClass

/**
 *
 */
internal class KMongoPojoCodecProvider(serialization: PropertySerialization<Any> = PropertyModelSerializationImpl()) :
    CodecProvider {

    private val pojoCodecProvider =
        PojoCodecProvider2
            .builder()
            .conventions(
                listOf(
                    KMongoConvention(serialization),
                    ConventionDefaultsImpl2(),
                    KMongoAnnotationConvention,
                    EmptyObjectConvention()
                )
            )
            .register(
                PairPropertyCodecProvider,
                TriplePropertyCodecProvider,
                KeyObjectMapPropertyCodecProvider
            )
            .automatic(true)
            .build()


    private val defaultProviders: List<CodecProvider> =
        listOf(
            ValueCodecProvider(),
            BsonValueCodecProvider(),
            DBRefCodecProvider(),
            DocumentCodecProvider(DocumentToDBRefTransformer()),
            IterableCodecProvider(DocumentToDBRefTransformer()),
            MapCodecProvider(DocumentToDBRefTransformer()),
            GeoJsonCodecProvider(),
            GridFSFileCodecProvider(),

            JavaTimeCodecProvider,
            UtilClassesCodecProvider
        )

    val codecRegistry: CodecRegistry =
        CodecRegistries.fromProviders(
            defaultProviders + this
        )

    fun getClassModel(type: KClass<*>): ClassModel<*> {
        return (codecRegistry.get(type.java) as PojoCodec<*>).classModel
    }


    override fun <T : Any?> get(clazz: Class<T>, registry: CodecRegistry): Codec<T>? {
        return if (clazz.isEnum) {
            @Suppress("UPPER_BOUND_VIOLATED", "UNCHECKED_CAST")
            EnumCodec(clazz as Class<Enum<in Enum<*>>>) as Codec<T>
        } else {
            pojoCodecProvider.get(clazz, registry)?.let {
                KMongoPojoCodec(it as PojoCodec<T>)
            }
        }
    }
}