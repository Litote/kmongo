package org.litote.kmongo.coroutine

import com.mongodb.MongoException
import com.mongodb.async.SingleResultCallback
import com.mongodb.async.client.ClientSession
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.litote.kmongo.CoreCategory
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertFailsWith

@Category(CoreCategory::class)
@RunWith(MockitoJUnitRunner::class)
class ClientSessionTest {

    @Mock
    lateinit var clientSession: ClientSession

    @Test
    fun `should commit transaction successfully`() = runBlocking {
        // Given
        whenever(clientSession.commitTransaction(any())).thenAnswer {
            (it.arguments.first() as SingleResultCallback<Void>).onResult(null, null)
        }

        // When
        clientSession.commitTransaction()

        // Then
        verify(clientSession).commitTransaction(any())
    }

    @Test
    fun `should raise exception on commit transaction`() {
        // Given
        whenever(clientSession.commitTransaction(any())).thenAnswer {
            (it.arguments.first() as SingleResultCallback<Void>).onResult(null, MongoException(""))
        }

        // When
        assertFailsWith<MongoException> { runBlocking { clientSession.commitTransaction() } }

        // Then
        verify(clientSession).commitTransaction(any())
    }

    @Test
    fun `should abort transaction successfully`() = runBlocking {
        // Given
        whenever(clientSession.abortTransaction(any())).thenAnswer {
            (it.arguments.first() as SingleResultCallback<Void>).onResult(null, null)
        }

        // When
        clientSession.abortTransaction()

        // Then
        verify(clientSession).abortTransaction(any())
    }

    @Test
    fun `should raise exception on abort transaction`() {
        // Given
        whenever(clientSession.abortTransaction(any())).thenAnswer {
            (it.arguments.first() as SingleResultCallback<Void>).onResult(null, MongoException(""))
        }

        // When
        assertFailsWith<MongoException> { runBlocking { clientSession.abortTransaction() } }

        // Then
        verify(clientSession).abortTransaction(any())
    }
}