/*
 * Copyright (C) 2018 Litote
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
package org.litote.kmongo.coroutine

import com.mongodb.ClientSessionOptions
import com.mongodb.MongoException
import com.mongodb.async.SingleResultCallback
import com.mongodb.async.client.ClientSession
import com.mongodb.async.client.MongoClient
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.litote.kmongo.CoreCategory
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Category(CoreCategory::class)
@RunWith(MockitoJUnitRunner::class)
class MongoClientTest {

    @Mock
    lateinit var client: MongoClient

    @Mock
    lateinit var clientSession: ClientSession

    // Unable to mock, final class
    lateinit var sessionOptions: ClientSessionOptions

    @Before
    fun setup() {
        sessionOptions = ClientSessionOptions.builder().build()
    }

    @Test
    fun `should succesfully start session`() = runBlocking {
        // Given
        whenever(client.startSession(any())).thenAnswer {
            (it.arguments.first() as SingleResultCallback<ClientSession>).onResult(clientSession, null)
        }

        // When
        val actual = client.startSession()

        // Then
        assertEquals(clientSession, actual)
        verify(client).startSession(any())
    }

    @Test
    fun `should fail to start session`() {
        // Given
        whenever(client.startSession(any())).thenAnswer {
            (it.arguments.first() as SingleResultCallback<ClientSession>).onResult(null, MongoException(""))
        }

        // When
        assertFailsWith<MongoException> { runBlocking { client.startSession() } }

        // Then
        verify(client).startSession(any())
    }

    @Test
    fun `should raise exception on null session`() {
        // Given
        whenever(client.startSession(any())).thenAnswer {
            (it.arguments.first() as SingleResultCallback<ClientSession>).onResult(null, null)
        }

        // When
        assertFailsWith<IllegalStateException> { runBlocking { client.startSession() } }

        // Then
        verify(client).startSession(any())
    }

    @Test
    fun `should succesfully start session with options`() = runBlocking {
        // Given
        whenever(client.startSession(eq(sessionOptions), any())).thenAnswer {
            (it.arguments.last() as SingleResultCallback<ClientSession>).onResult(clientSession, null)
        }

        // When
        val actual = client.startSession(sessionOptions)

        // Then
        assertEquals(clientSession, actual)
        verify(client).startSession(eq(sessionOptions), any())
    }

    @Test
    fun `should fail to start session with options`() {
        // Given
        whenever(client.startSession(eq(sessionOptions), any())).thenAnswer {
            (it.arguments.last() as SingleResultCallback<ClientSession>).onResult(null, MongoException(""))
        }

        // When
        assertFailsWith<MongoException> { runBlocking { client.startSession(sessionOptions) } }

        // Then
        verify(client).startSession(eq(sessionOptions), any())
    }

    @Test
    fun `should raise exception on null session with options`() {
        // Given
        whenever(client.startSession(eq(sessionOptions), any())).thenAnswer {
            (it.arguments.last() as SingleResultCallback<ClientSession>).onResult(null, null)
        }

        // When
        assertFailsWith<IllegalStateException> { runBlocking { client.startSession(sessionOptions) } }

        // Then
        verify(client).startSession(eq(sessionOptions), any())
    }
}