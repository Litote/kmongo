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

package org.litote.kmongo

import org.bson.Document
import org.litote.kmongo.KMongoBenchmark.decode
import org.litote.kmongo.KMongoBenchmark.defaultCodecRegistry
import org.litote.kmongo.KMongoBenchmark.kmongoCodecRegistry
import org.litote.kmongo.KMongoBenchmark.parseFriendWithBuddies
import org.litote.kmongo.KMongoBenchmark.parseFriends
import org.litote.kmongo.util.ObjectMappingConfiguration
import org.openjdk.jmh.annotations.Benchmark

open class CodecRegistryBenchmark {

    @Benchmark
    fun driverFriendWithBuddies(): FriendWithBuddies {
        val dbo: Document = decode(defaultCodecRegistry)
        return parseFriendWithBuddies(dbo)
    }

    @Benchmark
    fun driverFriend(): Friend {
        val dbo: Document = decode(defaultCodecRegistry)
        return parseFriends(dbo)
    }

    @Benchmark
    fun jacksonFriendWithBuddies(): FriendWithBuddies {
        return decode(kmongoCodecRegistry)
    }

    @Benchmark
    fun jacksonFriend(): Friend {
        return decode(kmongoCodecRegistry)
    }

    @Benchmark
    fun jacksonFriendWithCustomDeserializer(): FriendWithCustomDeserializer {
        return decode(kmongoCodecRegistry)
    }


    @Benchmark
    fun jacksonFriendWithBuddiesWithCustomDeserializer(): FriendWithBuddiesWithCustomDeserializer {
        return decode(kmongoCodecRegistry)
    }

    @Benchmark
    fun jacksonFriendWithBuddiesCodec(): FriendWithCustomCodecWithBuddies {
        return decode(kmongoCodecRegistry)
    }

    @Benchmark
    fun jacksonFriendData(): FriendData {
        return decode(kmongoCodecRegistry)
    }

    @Benchmark
    fun jacksonFriendWithBuddiesData(): FriendDataWithBuddies {
        return decode(kmongoCodecRegistry)
    }

    @Benchmark
    fun jacksonFriendSimpleData(): FriendSimpleData {
        return decode(kmongoCodecRegistry)
    }


    companion object {

        init {
            ObjectMappingConfiguration.customCodecProviders.add(FriendWithBuddiesCodec)
            ObjectMappingConfiguration.customCodecProviders.add(CoordinateCodec)
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val b = CodecRegistryBenchmark()

            println(b.driverFriendWithBuddies())
            println(b.jacksonFriendWithBuddies())
            println(b.jacksonFriendWithBuddiesWithCustomDeserializer())
            println(b.jacksonFriendWithBuddiesCodec())
            println(b.driverFriend())
            println(b.jacksonFriend())
            println(b.jacksonFriendWithCustomDeserializer())

            println(b.jacksonFriendData())
            println(b.jacksonFriendWithBuddiesData())
            println(b.jacksonFriendSimpleData())

            while (true) {
                decode<FriendWithCustomDeserializer>(kmongoCodecRegistry)
            }
        }
    }

}
