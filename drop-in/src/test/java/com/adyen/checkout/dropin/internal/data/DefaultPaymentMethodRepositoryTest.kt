/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/1/2026.
 */

package com.adyen.checkout.dropin.internal.data

import com.adyen.checkout.core.components.data.model.PaymentMethod
import com.adyen.checkout.core.components.data.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.components.data.model.StoredPaymentMethod
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DefaultPaymentMethodRepositoryTest {

    @Test
    fun `when initialized with payment methods, then regulars are set`() {
        val paymentMethods = listOf(PaymentMethod(type = "scheme", name = "Cards"))
        val response = PaymentMethodsApiResponse(paymentMethods = paymentMethods)

        val repository = DefaultPaymentMethodRepository(response)

        assertEquals(paymentMethods, repository.paymentMethods)
    }

    @Test
    fun `when initialized with null payment methods, then regulars are empty`() {
        val response = PaymentMethodsApiResponse(paymentMethods = null)

        val repository = DefaultPaymentMethodRepository(response)

        assertEquals(emptyList<PaymentMethod>(), repository.paymentMethods)
    }

    @Test
    fun `when initialized with stored payment methods, then favorites are set`() = runTest {
        val storedPaymentMethods = listOf(StoredPaymentMethod(type = "scheme", id = "123", name = "MasterCard"))
        val response = PaymentMethodsApiResponse(storedPaymentMethods = storedPaymentMethods)

        val repository = DefaultPaymentMethodRepository(response)

        assertEquals(storedPaymentMethods, repository.storedPaymentMethods.first())
    }

    @Test
    fun `when initialized with null stored payment methods, then favorites are empty`() = runTest {
        val response = PaymentMethodsApiResponse(storedPaymentMethods = null)

        val repository = DefaultPaymentMethodRepository(response)

        assertEquals(emptyList<StoredPaymentMethod>(), repository.storedPaymentMethods.first())
    }

    @Test
    fun `when removeStoredPaymentMethod is called, then item is removed from favorites`() = runTest {
        val storedPaymentMethod1 = StoredPaymentMethod(type = "scheme", id = "1", name = "MasterCard")
        val storedPaymentMethod2 = StoredPaymentMethod(type = "scheme", id = "2", name = "MasterCard")
        val response = PaymentMethodsApiResponse(
            storedPaymentMethods = listOf(storedPaymentMethod1, storedPaymentMethod2),
        )
        val repository = DefaultPaymentMethodRepository(response)

        repository.removeStoredPaymentMethod("1")

        val favorites = repository.storedPaymentMethods.first()
        assertEquals(listOf(storedPaymentMethod2), favorites)
    }

    @Test
    fun `when removeStoredPaymentMethod is called with unknown id, then favorites remain unchanged`() = runTest {
        val storedPaymentMethod1 = StoredPaymentMethod(type = "scheme", id = "1", name = "MasterCard")
        val response = PaymentMethodsApiResponse(
            storedPaymentMethods = listOf(storedPaymentMethod1),
        )
        val repository = DefaultPaymentMethodRepository(response)

        repository.removeStoredPaymentMethod("999")

        val favorites = repository.storedPaymentMethods.first()
        assertEquals(listOf(storedPaymentMethod1), favorites)
    }
}
