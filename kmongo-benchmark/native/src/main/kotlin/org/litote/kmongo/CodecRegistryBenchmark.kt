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

package org.litote.kmongo

import org.bson.Document
import org.litote.kmongo.KMongoBenchmark.decode
import org.litote.kmongo.KMongoBenchmark.defaultCodecRegistry
import org.litote.kmongo.KMongoBenchmark.kmongoCodecRegistry
import org.litote.kmongo.KMongoBenchmark.parseFriendWithBuddies
import org.litote.kmongo.KMongoBenchmark.parseFriends
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
    fun nativeFriendWithBuddies(): FriendWithBuddies {
        return decode(kmongoCodecRegistry)
    }

    @Benchmark
    fun nativeFriend(): Friend {
        return decode(kmongoCodecRegistry)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val b = CodecRegistryBenchmark()
            println(b.driverFriendWithBuddies())
            println(b.nativeFriendWithBuddies())
            println(b.driverFriend())
            println(b.nativeFriend())

            while (true) {
                decode<FriendWithBuddies>(kmongoCodecRegistry)
            }
        }
    }

}
