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

package com.github.jershell.kbson

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeDecoder.Companion.READ_DONE
import kotlinx.serialization.CompositeDecoder.Companion.UNKNOWN_NAME
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicKind
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.StructureKind
import kotlinx.serialization.builtins.AbstractDecoder
import kotlinx.serialization.decode
import kotlinx.serialization.modules.SerialModule
import org.bson.AbstractBsonReader
import org.bson.AbstractBsonReader.State
import org.bson.BsonType

abstract class FlexibleDecoder(
    val reader: AbstractBsonReader,
    override val context: SerialModule,
    val configuration: Configuration
) : AbstractDecoder() {

    override fun beginStructure(descriptor: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
        return when (descriptor.kind) {
            StructureKind.CLASS -> {
                val current = reader.currentBsonType
                if (current == null || current == BsonType.DOCUMENT) {
                    reader.readStartDocument()
                }
                BsonFlexibleDecoder(reader, context, configuration)
            }
            StructureKind.MAP -> {
                reader.readStartDocument()
                MapDecoder(reader, context, configuration)
            }
            StructureKind.LIST -> {
                reader.readStartArray()
                ListDecoder(reader, context, configuration)
            }
            is PolymorphicKind -> {
                reader.readStartDocument()
                PolymorphismDecoder(reader, context, configuration)
            }
            else -> super.beginStructure(descriptor, *typeParams)
        }
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        when (descriptor.kind) {
            is StructureKind.LIST -> reader.readEndArray()
            is StructureKind.MAP, StructureKind.CLASS, StructureKind.OBJECT -> reader.readEndDocument()
        }
    }

    override fun decodeNotNullMark(): Boolean {
        return reader.currentBsonType != BsonType.NULL
    }

    override fun decodeBoolean(): Boolean {
        return reader.readBoolean()
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        val name = reader.readString()
        val value = enumDescriptor.getElementIndex(name)
        return if (value == UNKNOWN_NAME) {
            throw SerializationException("Enum has unknown value $name")
        } else {
            value
        }
    }

    override fun decodeByte(): Byte {
        return reader.readInt32().toByte()
    }

    override fun decodeNull(): Nothing? {
        reader.readNull()
        return null
    }

    override fun decodeChar(): Char {
        return reader.readSymbol().first()
    }

    override fun decodeDouble(): Double {
        return reader.readDouble()
    }

    override fun decodeInt(): Int {
        return reader.readInt32()
    }

    override fun decodeShort(): Short {
        return reader.readInt32().toShort()
    }

    override fun decodeLong(): Long {
        return reader.readInt64()
    }

    override fun decodeFloat(): Float {
        return reader.readDouble().toFloat()
    }

    override fun decodeString(): String {
        return reader.readString()
    }

    override fun decodeUnit() {
        reader.readUndefined()
    }
}

class BsonFlexibleDecoder(
    reader: AbstractBsonReader,
    context: SerialModule,
    configuration: Configuration
) : FlexibleDecoder(reader, context, configuration) {

    //to handle not optional nullable properties
    private var indexesSet: BooleanArray? = null
    private var containsNotOptionalNullable: Boolean? = null
    private var checkNotOptionalNullable = false

    private fun initNotOptionalProperties(desc: SerialDescriptor) {
        if (containsNotOptionalNullable == null) {
            for (i in 0 until desc.elementsCount) {
                if (!desc.isElementOptional(i)) {
                    val nullable =
                        try {
                            desc.getElementDescriptor(i).isNullable
                        } catch (e: Exception) {
                            true
                        }
                    if (nullable) {
                        containsNotOptionalNullable = true
                        break
                    }
                }
            }
            if (containsNotOptionalNullable == null) {
                containsNotOptionalNullable = false
            } else {
                indexesSet = BooleanArray(desc.elementsCount)
            }
        }
    }

    private fun checkNotOptionalProperties(desc: SerialDescriptor): Int {
        //set to null not optional nullable properties if any
        if (containsNotOptionalNullable!!) {
            for (i in 0 until desc.elementsCount) {
                if (indexesSet?.get(i) != true
                    && desc.getElementDescriptor(i).isNullable
                    && !desc.isElementOptional(i)
                ) {
                    checkNotOptionalNullable = true
                    indexesSet!![i] = true
                    return i
                }
            }
        }
        return READ_DONE
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        initNotOptionalProperties(descriptor)

        if (reader.state == State.TYPE) {
            reader.readBsonType()
        }
        return when (reader.state) {
            State.NAME -> {
                val currentName = reader.readName()
                val index = descriptor.getElementIndex(currentName)
                if (index == UNKNOWN_NAME) {
                    reader.skipValue()
                    decodeElementIndex(descriptor)
                } else {
                    if (containsNotOptionalNullable!!) {
                        indexesSet!![index] = true
                    }
                    index
                }
            }
            else -> {
                checkNotOptionalProperties(descriptor)
            }
        }
    }

    override fun decodeNotNullMark(): Boolean {
        return !checkNotOptionalNullable && reader.currentBsonType != BsonType.NULL
    }

    override fun decodeNull(): Nothing? {
        if (!checkNotOptionalNullable) {
            reader.readNull()
        }
        return null
    }
}

private class PolymorphismDecoder(
    reader: AbstractBsonReader,
    context: SerialModule,
    configuration: Configuration
) : FlexibleDecoder(reader, context, configuration) {
    private var decodeCount = 0

    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
        if (deserializer !is PolymorphicSerializer<*>) {
            return deserializer.deserialize(this)
        }
        reader.readName()
        val type = reader.readString()

        @Suppress("UNCHECKED_CAST")
        val actualSerializer = deserializer.findPolymorphicSerializer(this, type) as KSerializer<T>
        return BsonFlexibleDecoder(reader, context, configuration).decode(actualSerializer)
    }

    override fun decodeSequentially(): Boolean {
        return true
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (decodeCount < 2)
            return decodeCount++

        return READ_DONE
    }
}

private class MapDecoder(
    reader: AbstractBsonReader,
    context: SerialModule,
    configuration: Configuration
) : FlexibleDecoder(reader, context, configuration) {

    private var index = 0
    private var key: Boolean = false

    override fun decodeBoolean(): Boolean {
        return if (key) {
            reader.readName()!!.toBoolean()
        } else {
            super.decodeBoolean()
        }
    }

    override fun decodeEnum(enumDescription: SerialDescriptor): Int {
        return if (key) {
            enumDescription.getElementIndex(reader.readName()!!)
        } else {
            super.decodeEnum(enumDescription)
        }
    }

    override fun decodeByte(): Byte {
        return if (key) {
            reader.readName()!!.toByte()
        } else {
            super.decodeByte()
        }
    }

    override fun decodeChar(): Char {
        return if (key) {
            reader.readName()!!.first()
        } else {
            super.decodeChar()
        }
    }

    override fun decodeDouble(): Double {
        return if (key) {
            reader.readName()!!.toDouble()
        } else {
            super.decodeDouble()
        }
    }

    override fun decodeInt(): Int {
        return if (key) {
            reader.readName()!!.toInt()
        } else {
            super.decodeInt()
        }
    }

    override fun decodeShort(): Short {
        return if (key) {
            reader.readName()!!.toShort()
        } else {
            super.decodeShort()
        }
    }

    override fun decodeLong(): Long {
        return if (key) {
            reader.readName()!!.toLong()
        } else {
            super.decodeLong()
        }
    }

    override fun decodeFloat(): Float {
        return if (key) {
            reader.readName()!!.toFloat()
        } else {
            super.decodeFloat()
        }
    }

    override fun decodeString(): String {
        return if (key) {
            reader.readName()
        } else {
            super.decodeString()
        }
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (!key) {
            key = true
            val nextType = reader.readBsonType()
            if (nextType == BsonType.END_OF_DOCUMENT) return READ_DONE
        } else {
            key = false
        }
        return index++
    }
}

private class ListDecoder(
    reader: AbstractBsonReader,
    context: SerialModule,
    configuration: Configuration
) : FlexibleDecoder(reader, context, configuration) {
    private var index = 0
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        val nextType = reader.readBsonType()
        return if (nextType == BsonType.END_OF_DOCUMENT) READ_DONE else index++
    }
}