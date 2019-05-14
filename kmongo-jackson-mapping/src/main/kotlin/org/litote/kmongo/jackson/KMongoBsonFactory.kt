/*
 * Copyright (C) 2016 Litote
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
package org.litote.kmongo.jackson

import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.core.io.IOContext
import de.undercouch.bson4jackson.BsonConstants
import de.undercouch.bson4jackson.BsonFactory
import de.undercouch.bson4jackson.BsonGenerator
import de.undercouch.bson4jackson.BsonParser
import de.undercouch.bson4jackson.io.ByteOrderUtil
import de.undercouch.bson4jackson.types.Timestamp
import org.bson.BsonTimestamp
import org.bson.types.Binary
import org.bson.types.ObjectId
import org.litote.kmongo.jackson.BsonModule.KMongoObjectId
import java.io.InputStream
import java.io.OutputStream

internal class KMongoBsonFactory : BsonFactory() {

    init {
        enable(BsonParser.Feature.HONOR_DOCUMENT_LENGTH)
        enable(BsonGenerator.Feature.WRITE_BIGDECIMALS_AS_DECIMAL128)
    }

    internal class KMongoBsonGenerator(jsonFeatures: Int, bsonFeatures: Int, out: OutputStream) :
        BsonGenerator(jsonFeatures, bsonFeatures, out) {

        override fun canWriteObjectId(): Boolean {
            return true;
        }

        override fun writeObjectId(objectId: Any) {
            if (objectId !is ObjectId) {
                //generation with @JsonIdentityInfo - see https://github.com/Litote/kmongo/issues/135
                if (objectId is String) {
                    writeFieldName("_id")
                    writeString(objectId)
                } else {
                    error("$objectId has to be ${ObjectId::class}")
                }
            } else {
                _writeArrayFieldNameIfNeeded()
                _verifyValueWrite("write object id")
                _buffer.putByte(_typeMarker, BsonConstants.TYPE_OBJECTID)
                objectId.toByteArray().forEach { _buffer.putByte(it) }
                flushBuffer()
            }
        }

        fun writeBinary(binary: Binary) {
            _writeArrayFieldNameIfNeeded()
            _verifyValueWrite("write binary")
            val bytes = binary.data
            _buffer.putByte(_typeMarker, BsonConstants.TYPE_BINARY)
            _buffer.putInt(bytes.size)
            _buffer.putByte(binary.type)
            _buffer.putBytes(*binary.data)
            flushBuffer()
        }

        fun writeBsonTimestamp(timestamp: BsonTimestamp) {
            _writeArrayFieldNameIfNeeded()
            _verifyValueWrite("write timestamp")
            _buffer.putByte(_typeMarker, BsonConstants.TYPE_TIMESTAMP)
            _buffer.putInt(timestamp.getInc())
            _buffer.putInt(timestamp.getTime())
            flushBuffer()
        }

        fun writeMinKey() {
            _writeArrayFieldNameIfNeeded()
            _verifyValueWrite("write min key")
            _buffer.putByte(_typeMarker, BsonConstants.TYPE_MINKEY)
            flushBuffer()
        }

        fun writeMaxKey() {
            _writeArrayFieldNameIfNeeded()
            _verifyValueWrite("write max key")
            _buffer.putByte(_typeMarker, BsonConstants.TYPE_MAXKEY)
            flushBuffer()
        }
    }

    private class KMongoBsonParser(ctxt: IOContext, jsonFeatures: Int, bsonFeatures: Int, inputStream: InputStream) :
        BsonParser(ctxt, jsonFeatures, bsonFeatures, inputStream) {

        override fun getEmbeddedObject(): Any {
            val embedded = super.getEmbeddedObject()
            if (embedded is de.undercouch.bson4jackson.types.ObjectId) {
                return convertToNativeObjectId(embedded)
            }
            if (embedded is Timestamp) {
                return convertToBSONTimestamp(embedded)
            }
            return embedded
        }

        private fun convertToBSONTimestamp(ts: Timestamp): Any {
            return BsonTimestamp(ts.time, ts.inc)
        }

        private fun convertToNativeObjectId(id: de.undercouch.bson4jackson.types.ObjectId): org.bson.types.ObjectId {
            return org.bson.types.ObjectId.createFromLegacyFormat(id.time, id.machine, id.inc)
        }

        override fun readObjectId(): de.undercouch.bson4jackson.types.ObjectId {
            val time = ByteOrderUtil.flip(_in.readInt())
            val machine = ByteOrderUtil.flip(_in.readInt())
            val inc = ByteOrderUtil.flip(_in.readInt())
            return KMongoObjectId(time, machine, inc)
        }
    }


    override fun createGenerator(outputStream: OutputStream, enc: JsonEncoding): BsonGenerator {
        var out = outputStream
        val ctxt = _createContext(out, true)
        ctxt.setEncoding(enc)
        if (enc == JsonEncoding.UTF8 && _outputDecorator != null) {
            out = _outputDecorator.decorate(ctxt, out)
        }
        val g = KMongoBsonGenerator(_generatorFeatures, _bsonGeneratorFeatures, out)
        val codec = codec
        if (codec != null) {
            g.codec = codec
        }
        if (_characterEscapes != null) {
            g.characterEscapes = _characterEscapes
        }
        return g
    }

    override fun _createParser(inputStream: InputStream, ctxt: IOContext): BsonParser {
        val p = KMongoBsonParser(ctxt, _parserFeatures, _bsonParserFeatures, inputStream)
        val codec = codec
        if (codec != null) {
            p.codec = codec
        }
        return p
    }

    override fun copy(): BsonFactory {
        return KMongoBsonFactory()
    }
}


