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
package org.litote.kmongo.async

import java.io.IOException
import java.net.DatagramSocket
import java.net.ServerSocket
import java.net.Socket
import java.net.UnknownHostException

/**
 *
 */
object RandomPortNumberGenerator {

    private val MAX_PORT_NUMBER = 61000
    private val MIN_PORT_NUMBER = 49152

    /**
     * Pick a random available port number in the ephemeral port range,
     * i.e. between {@value #MIN_PORT_NUMBER} inclusively and
     * {@value #MAX_PORT_NUMBER} exclusively.

     * @return a random available port number in the ephemeral port range
     */
    fun pickAvailableRandomEphemeralPortNumber(): Int {
        return pickAvailableRandomPortNumber(MIN_PORT_NUMBER, MAX_PORT_NUMBER)
    }


    /**
     * Pick a random available port number between
     * `min` inclusively and `max` exclusively.

     * @return a random available port number between `min` inclusively and
     * * `max` exclusively
     */
    fun pickAvailableRandomPortNumber(min: Int, max: Int): Int {
        while (true) {
            val port = pickRandomPortNumber(min, max)
            if (isPortAvailable(port)) {
                return port
            }
        }
    }

    /**
     * Pick a random port number between `min` inclusively and
     * `max` exclusively.

     * @return a random port number between `min` inclusively and
     * * `max` exclusively
     */
    fun pickRandomPortNumber(min: Int, max: Int): Int {
        return (Math.random() * (max - min)).toInt() + min
    }

    /**
     * Test whether a port number is available (i.e. not bound).

     * @return a random port number between `min` inclusively and
     * * `max` exclusively
     */
    fun isPortAvailable(port: Int): Boolean {
        var ss: ServerSocket? = null
        var ds: DatagramSocket? = null
        try {
            ss = ServerSocket(port)
            ss.reuseAddress = true
            ds = DatagramSocket(port)
            ds.reuseAddress = true
        } catch (e: IOException) {
            return false
        } finally {
            if (ds != null) {
                ds.close()
            }
            if (ss != null) {
                try {
                    ss.close()
                } catch (e: IOException) {
                }

            }
        }
        try {
            Socket("localhost", port)
        } catch (e: UnknownHostException) {
            return false
        } catch (e: IOException) {
            return true
        }

        return false
    }
}