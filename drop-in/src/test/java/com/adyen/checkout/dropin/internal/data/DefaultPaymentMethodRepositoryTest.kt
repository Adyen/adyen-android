/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/1/2026.
 */

package com.adyen.checkout.dropin.internal.data

import app.cash.turbine.test
import com.adyen.checkout.core.components.data.model.paymentmethod.GenericPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCardPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
internal class DefaultPaymentMethodRepositoryTest {

    @Test
    fun `when initialized with payment methods, then regulars are set`() {
        val paymentMethods = listOf(GenericPaymentMethod(type = "scheme", name = "Cards"))
        val repository = createRepository(paymentMethods = paymentMethods)

        assertEquals(paymentMethods, repository.paymentMethods)
    }

    @Test
    fun `when initialized with empty payment methods, then regulars are empty`() {
        val repository = createRepository()

        assertEquals(emptyList<GenericPaymentMethod>(), repository.paymentMethods)
    }

    @Test
    fun `when initialized with stored payment methods, then stored payment methods are set`() = runTest {
        val storedPaymentMethods = listOf(storedCard("123"))
        val repository = createRepository(storedPaymentMethods = storedPaymentMethods)

        assertEquals(storedPaymentMethods, repository.storedPaymentMethods.first())
    }

    @Test
    fun `when initialized with empty stored payment methods, then stored payment methods are empty`() = runTest {
        val repository = createRepository()

        assertEquals(emptyList<StoredCardPaymentMethod>(), repository.storedPaymentMethods.first())
    }

    @Test
    fun `when removeStoredPaymentMethod is called, then the stored payment method is passed to the remover`() =
        runTest {
            val removed = mutableListOf<StoredPaymentMethod>()
            val repository = createRepository(
                storedPaymentMethods = listOf(storedCard("1"), storedCard("2")),
                remover = {
                    removed += it
                    Result.success(Unit)
                },
                coroutineScope = backgroundScope,
            )

            repository.removeStoredPaymentMethod("1")

            assertEquals(listOf(storedCard("1")), removed)
        }

    @Test
    fun `when the removal succeeds, then success is returned and the item is no longer stored`() = runTest {
        val repository = createRepository(
            storedPaymentMethods = listOf(storedCard("1"), storedCard("2")),
            remover = { Result.success(Unit) },
            coroutineScope = backgroundScope,
        )

        val result = repository.removeStoredPaymentMethod("1")

        assertTrue(result.isSuccess)
        assertEquals(listOf(storedCard("2")), repository.storedPaymentMethods.first())
    }

    @Test
    fun `when the removal fails, then the failure is returned and stored payment methods remain unchanged`() = runTest {
        val error = IOException("network down")
        val repository = createRepository(
            storedPaymentMethods = listOf(storedCard("1"), storedCard("2")),
            remover = { Result.failure(error) },
            coroutineScope = backgroundScope,
        )

        val result = repository.removeStoredPaymentMethod("1")

        assertEquals(error, result.exceptionOrNull())
        assertEquals(listOf(storedCard("1"), storedCard("2")), repository.storedPaymentMethods.first())
    }

    @Test
    fun `when removeStoredPaymentMethod is called with unknown id, then failure is returned and nothing is removed`() =
        runTest {
            val removed = mutableListOf<StoredPaymentMethod>()
            val repository = createRepository(
                storedPaymentMethods = listOf(storedCard("1")),
                remover = {
                    removed += it
                    Result.success(Unit)
                },
                coroutineScope = backgroundScope,
            )

            val result = repository.removeStoredPaymentMethod("999")

            assertTrue(result.isFailure)
            assertEquals(emptyList<StoredPaymentMethod>(), removed)
            assertEquals(listOf(storedCard("1")), repository.storedPaymentMethods.first())
        }

    @Test
    fun `when the remover throws, then the failure is returned and stored payment methods remain unchanged`() = runTest {
        val error = IllegalStateException("unexpected")
        val repository = createRepository(
            storedPaymentMethods = listOf(storedCard("1")),
            remover = { throw error },
            coroutineScope = backgroundScope,
        )

        val result = repository.removeStoredPaymentMethod("1")

        assertEquals(error, result.exceptionOrNull())
        assertEquals(listOf(storedCard("1")), repository.storedPaymentMethods.first())
    }

    @Test
    fun `when the caller is cancelled during the removal, then the removal still completes`() = runTest {
        val response = CompletableDeferred<Result<Unit>>()
        val repository = createRepository(
            storedPaymentMethods = listOf(storedCard("1"), storedCard("2")),
            remover = { response.await() },
            coroutineScope = backgroundScope,
        )
        val caller = launch { repository.removeStoredPaymentMethod("1") }
        runCurrent()

        // Stands in for the shopper leaving the management screen while the call is running.
        caller.cancel()
        response.complete(Result.success(Unit))
        runCurrent()

        assertEquals(listOf(storedCard("2")), repository.storedPaymentMethods.first())
    }

    @Test
    fun `when the same item is removed twice at once, then the remover is called once and both get its result`() =
        runTest {
            val response = CompletableDeferred<Result<Unit>>()
            var removerCalls = 0
            val repository = createRepository(
                storedPaymentMethods = listOf(storedCard("1")),
                remover = {
                    removerCalls++
                    response.await()
                },
                coroutineScope = backgroundScope,
            )
            val first = async { repository.removeStoredPaymentMethod("1") }
            val second = async { repository.removeStoredPaymentMethod("1") }
            runCurrent()

            response.complete(Result.success(Unit))

            assertTrue(first.await().isSuccess)
            assertTrue(second.await().isSuccess)
            assertEquals(1, removerCalls)
        }

    @Test
    fun `when a removal is running, then its id is being removed until it finishes`() = runTest {
        val response = CompletableDeferred<Result<Unit>>()
        val repository = createRepository(
            storedPaymentMethods = listOf(storedCard("1"), storedCard("2")),
            remover = { response.await() },
            coroutineScope = backgroundScope,
        )

        repository.storedPaymentMethodIdsBeingRemoved.test {
            assertEquals(emptySet<String>(), awaitItem())

            launch { repository.removeStoredPaymentMethod("1") }
            assertEquals(setOf("1"), awaitItem())

            response.complete(Result.failure(IOException("network down")))
            assertEquals(emptySet<String>(), awaitItem())
        }
    }

    @Test
    fun `when a removal failed, then the same item can be removed again`() = runTest {
        var result = Result.failure<Unit>(IOException("network down"))
        var removerCalls = 0
        val repository = createRepository(
            storedPaymentMethods = listOf(storedCard("1")),
            remover = {
                removerCalls++
                result
            },
            coroutineScope = backgroundScope,
        )
        repository.removeStoredPaymentMethod("1")
        result = Result.success(Unit)

        val retry = repository.removeStoredPaymentMethod("1")

        assertTrue(retry.isSuccess)
        assertEquals(2, removerCalls)
        assertEquals(emptyList<StoredPaymentMethod>(), repository.storedPaymentMethods.first())
    }

    private fun createRepository(
        paymentMethods: List<GenericPaymentMethod> = emptyList(),
        storedPaymentMethods: List<StoredPaymentMethod> = emptyList(),
        remover: StoredPaymentMethodRemover = StoredPaymentMethodRemover { Result.success(Unit) },
        coroutineScope: CoroutineScope = TestScope(),
    ) = DefaultPaymentMethodRepository(
        paymentMethods = paymentMethods,
        storedPaymentMethods = storedPaymentMethods,
        storedPaymentMethodRemover = remover,
        coroutineScope = coroutineScope,
    )

    private fun storedCard(id: String) = StoredCardPaymentMethod(
        type = "scheme",
        id = id,
        name = "MasterCard",
        supportedShopperInteractions = emptyList(),
        brand = "mc",
        lastFour = "1234",
        expiryMonth = "01",
        expiryYear = "2030",
        holderName = null,
        fundingSource = null,
    )
}
