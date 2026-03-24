/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/1/2026.
 */

package com.adyen.checkout.dropin.internal.data

import com.adyen.checkout.core.components.data.model.paymentmethod.InstantPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCardPaymentMethod
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DefaultPaymentMethodRepositoryTest {

    @Test
    fun `when initialized with payment methods, then regulars are set`() {
        val paymentMethods = listOf(InstantPaymentMethod(type = "scheme", name = "Cards"))
        val response = PaymentMethods(paymentMethods = paymentMethods)

        val repository = DefaultPaymentMethodRepository(response)

        assertEquals(paymentMethods, repository.paymentMethods)
    }

    @Test
    fun `when initialized with null payment methods, then regulars are empty`() {
        val response = PaymentMethods(paymentMethods = null)

        val repository = DefaultPaymentMethodRepository(response)

        assertEquals(emptyList<InstantPaymentMethod>(), repository.paymentMethods)
    }

    @Test
    fun `when initialized with stored payment methods, then favorites are set`() = runTest {
        val storedPaymentMethods = listOf(
            StoredCardPaymentMethod(
                type = "scheme",
                id = "123",
                name = "MasterCard",
                supportedShopperInteractions = emptyList(),
                brand = "mc",
                lastFour = "1234",
                expiryMonth = "01",
                expiryYear = "2030",
                holderName = null,
                fundingSource = null,
            ),
        )
        val response = PaymentMethods(storedPaymentMethods = storedPaymentMethods)

        val repository = DefaultPaymentMethodRepository(response)

        assertEquals(storedPaymentMethods, repository.storedPaymentMethods.first())
    }

    @Test
    fun `when initialized with null stored payment methods, then favorites are empty`() = runTest {
        val response = PaymentMethods(storedPaymentMethods = null)

        val repository = DefaultPaymentMethodRepository(response)

        assertEquals(emptyList<StoredCardPaymentMethod>(), repository.storedPaymentMethods.first())
    }

    @Test
    fun `when removeStoredPaymentMethod is called, then item is removed from favorites`() = runTest {
        val storedPaymentMethod1 = StoredCardPaymentMethod(
            type = "scheme",
            id = "1",
            name = "MasterCard",
            supportedShopperInteractions = emptyList(),
            brand = "mc",
            lastFour = "1234",
            expiryMonth = "01",
            expiryYear = "2030",
            holderName = null,
            fundingSource = null,
        )
        val storedPaymentMethod2 = StoredCardPaymentMethod(
            type = "scheme",
            id = "2",
            name = "MasterCard",
            supportedShopperInteractions = emptyList(),
            brand = "mc",
            lastFour = "5678",
            expiryMonth = "01",
            expiryYear = "2030",
            holderName = null,
            fundingSource = null,
        )
        val response = PaymentMethods(
            storedPaymentMethods = listOf(storedPaymentMethod1, storedPaymentMethod2),
        )
        val repository = DefaultPaymentMethodRepository(response)

        repository.removeStoredPaymentMethod("1")

        val favorites = repository.storedPaymentMethods.first()
        assertEquals(listOf(storedPaymentMethod2), favorites)
    }

    @Test
    fun `when removeStoredPaymentMethod is called with unknown id, then favorites remain unchanged`() = runTest {
        val storedPaymentMethod1 = StoredCardPaymentMethod(
            type = "scheme",
            id = "1",
            name = "MasterCard",
            supportedShopperInteractions = emptyList(),
            brand = "mc",
            lastFour = "1234",
            expiryMonth = "01",
            expiryYear = "2030",
            holderName = null,
            fundingSource = null,
        )
        val response = PaymentMethods(
            storedPaymentMethods = listOf(storedPaymentMethod1),
        )
        val repository = DefaultPaymentMethodRepository(response)

        repository.removeStoredPaymentMethod("999")

        val favorites = repository.storedPaymentMethods.first()
        assertEquals(listOf(storedPaymentMethod1), favorites)
    }
}
