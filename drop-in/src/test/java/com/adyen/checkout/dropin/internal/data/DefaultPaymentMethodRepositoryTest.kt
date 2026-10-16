/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/1/2026.
 */

package com.adyen.checkout.dropin.internal.data

import com.adyen.checkout.core.components.data.model.paymentmethod.GenericPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCardPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.IOException

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
    fun `when initialized with stored payment methods, then favorites are set`() = runTest {
        val storedPaymentMethods = listOf(storedCard("123"))
        val repository = createRepository(storedPaymentMethods = storedPaymentMethods)

        assertEquals(storedPaymentMethods, repository.storedPaymentMethods.first())
    }

    @Test
    fun `when initialized with empty stored payment methods, then favorites are empty`() = runTest {
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
            )

            repository.removeStoredPaymentMethod("1")

            assertEquals(listOf(storedCard("1")), removed)
        }

    @Test
    fun `when the removal succeeds, then success is returned and the item is removed from favorites`() = runTest {
        val repository = createRepository(
            storedPaymentMethods = listOf(storedCard("1"), storedCard("2")),
            remover = { Result.success(Unit) },
        )

        val result = repository.removeStoredPaymentMethod("1")

        assertTrue(result.isSuccess)
        assertEquals(listOf(storedCard("2")), repository.storedPaymentMethods.first())
    }

    @Test
    fun `when the removal fails, then the failure is returned and favorites remain unchanged`() = runTest {
        val error = IOException("network down")
        val repository = createRepository(
            storedPaymentMethods = listOf(storedCard("1"), storedCard("2")),
            remover = { Result.failure(error) },
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
            )

            val result = repository.removeStoredPaymentMethod("999")

            assertTrue(result.isFailure)
            assertEquals(emptyList<StoredPaymentMethod>(), removed)
            assertEquals(listOf(storedCard("1")), repository.storedPaymentMethods.first())
        }

    private fun createRepository(
        paymentMethods: List<GenericPaymentMethod> = emptyList(),
        storedPaymentMethods: List<StoredPaymentMethod> = emptyList(),
        remover: StoredPaymentMethodRemover = StoredPaymentMethodRemover { Result.success(Unit) },
    ) = DefaultPaymentMethodRepository(
        paymentMethods = paymentMethods,
        storedPaymentMethods = storedPaymentMethods,
        storedPaymentMethodRemover = remover,
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
