package org.litote.kmongo.coroutine

import com.mongodb.async.SingleResultCallback
import com.mongodb.async.client.ClientSession
import com.mongodb.async.client.MongoCollection
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.litote.kmongo.CoreCategory
import org.litote.kmongo.model.Friend
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@Category(CoreCategory::class)
@RunWith(MockitoJUnitRunner::class)
class MongoCollectionTest {

    @Mock
    lateinit var collection: MongoCollection<Friend>

    @Mock
    lateinit var clientSession: ClientSession

    @Test
    fun `should drop collection`() = runBlocking {
        // Given
        whenever(collection.drop(any())).thenAnswer {
            (it.arguments.first() as SingleResultCallback<Void>).onResult(null, null)
        }

        // When
        collection.drop()

        // Then
        verify(collection).drop(any())
    }

    @Test
    fun `should drop collection during clientSession`() = runBlocking {
        // Given
        whenever(collection.drop(eq(clientSession), any())).thenAnswer {
            (it.arguments.last() as SingleResultCallback<Void>).onResult(null, null)
        }

        // When
        collection.drop(clientSession)

        // Then
        verify(collection).drop(eq(clientSession), any())
    }
}
