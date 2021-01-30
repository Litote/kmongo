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

package org.litote.kmongo.coroutine.issues

import kotlinx.coroutines.runBlocking
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.pojo.annotations.BsonId
import org.junit.Test
import org.junit.experimental.categories.Category
import org.litote.kmongo.Id
import org.litote.kmongo.JacksonMappingCategory
import org.litote.kmongo.NativeMappingCategory
import org.litote.kmongo.coroutine.KMongoReactiveStreamsCoroutineBaseTest
import org.litote.kmongo.coroutine.updateOne
import org.litote.kmongo.newId
import org.litote.kmongo.util.ObjectMappingConfiguration
import java.util.Currency

object CurrencyCodec : Codec<Currency> {

    override fun getEncoderClass(): Class<Currency> =
        Currency::class.java

    override fun encode(writer: BsonWriter, value: Currency, encoderContext: EncoderContext) {
        writer.writeString(value.currencyCode)
    }

    override fun decode(reader: BsonReader, decoderContext: DecoderContext): Currency =
        Currency.getInstance(reader.readString())
}

data class Account(
    @BsonId
    val id: Id<Account> = newId(),
    val currency: Currency
)

/**
 *
 */
@Category(JacksonMappingCategory::class, NativeMappingCategory::class)
class Issue182CustomCodec : KMongoReactiveStreamsCoroutineBaseTest<Account>() {

    @Test
    fun testCustomCodec() = runBlocking {
        ObjectMappingConfiguration.addCustomCodec(CurrencyCodec)

        val account = Account(currency = Currency.getInstance("USD"))

        col.insertOne(account)

        assert(col.findOneById(account.id)?.currency == Currency.getInstance("USD"))

        //check this does not fail
        col.updateOne(
            account.copy(
                currency = Currency.getInstance("EUR")
            )
        )
        Unit
    }
}