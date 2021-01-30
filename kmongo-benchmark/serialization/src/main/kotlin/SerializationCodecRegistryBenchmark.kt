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

package org.litote.serialization

import org.bson.Document
import org.litote.kmongo.Friend
import org.litote.kmongo.FriendWithBuddies
import org.litote.kmongo.KMongoBenchmark
import org.openjdk.jmh.annotations.Benchmark


/**
 *
 */
open class SerializationCodecRegistryBenchmark {

    @Benchmark
    fun driverFriendWithBuddies(): FriendWithBuddies {
        val dbo: Document = KMongoBenchmark.decode(KMongoBenchmark.defaultCodecRegistry)
        return KMongoBenchmark.parseFriendWithBuddies(dbo)
    }

    @Benchmark
    fun driverFriend(): Friend {
        val dbo: Document = KMongoBenchmark.decode(KMongoBenchmark.defaultCodecRegistry)
        return KMongoBenchmark.parseFriends(dbo)
    }

    @Benchmark
    fun serializationFriendWithBuddies(): FriendWithBuddies {
        return KMongoBenchmark.decode(KMongoBenchmark.kmongoCodecRegistry)
    }

    @Benchmark
    fun serializationFriend(): Friend {
        return KMongoBenchmark.decode(KMongoBenchmark.kmongoCodecRegistry)
    }


    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val b = SerializationCodecRegistryBenchmark()

            println(b.driverFriendWithBuddies())
            println(b.serializationFriendWithBuddies())
            println(b.driverFriend())
            println(b.serializationFriend())


            while (true) {
                KMongoBenchmark.decode<FriendWithBuddies>(KMongoBenchmark.kmongoCodecRegistry)
            }
        }
    }
}