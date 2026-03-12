/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/3/2026.
 */

package com.adyen.checkout.dropin.internal.helper

import android.os.Parcel
import com.adyen.checkout.core.components.data.model.paymentmethod.CardPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCardPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PaymentMethodSupportCheckTest {

    private val paymentMethodSupportCheck = PaymentMethodSupportCheck()

    @Test
    fun `when payment method type is supported, then isSupported returns true`() {
        // GIVEN
        val paymentMethod = CardPaymentMethod(
            type = PaymentMethodTypes.SCHEME,
            name = "Cards",
            brands = emptyList(),
            fundingSource = null,
        )

        // WHEN
        val isSupported = paymentMethodSupportCheck.isSupported(paymentMethod)

        // THEN
        assertTrue(isSupported)
    }

    @Test
    fun `when payment method type is unsupported, then isSupported returns false`() {
        // GIVEN
        val paymentMethod = UnsupportedPaymentMethod()

        // WHEN
        val isSupported = paymentMethodSupportCheck.isSupported(paymentMethod)

        // THEN
        assertFalse(isSupported)
    }

    @Test
    fun `when stored payment method is valid, then isSupported returns true`() {
        // GIVEN
        val storedPaymentMethod = StoredCardPaymentMethod(
            type = PaymentMethodTypes.SCHEME,
            id = "123",
            name = "Cards",
            brand = "visa",
            lastFour = "1234",
            expiryMonth = "12",
            expiryYear = "2025",
            holderName = "Test",
            fundingSource = null,
            supportedShopperInteractions = listOf("Ecommerce"),
        )

        // WHEN
        val isSupported = paymentMethodSupportCheck.isSupported(storedPaymentMethod)

        // THEN
        assertTrue(isSupported)
    }

    @Test
    fun `when stored payment method id is empty, then isSupported returns false`() {
        // GIVEN
        val storedPaymentMethod = StoredCardPaymentMethod(
            type = PaymentMethodTypes.SCHEME,
            id = "",
            name = "Cards",
            brand = "visa",
            lastFour = "1234",
            expiryMonth = "12",
            expiryYear = "2025",
            holderName = "Test",
            fundingSource = null,
            supportedShopperInteractions = listOf("Ecommerce"),
        )

        // WHEN
        val isSupported = paymentMethodSupportCheck.isSupported(storedPaymentMethod)

        // THEN
        assertFalse(isSupported)
    }

    @Test
    fun `when stored payment method type is not supported, then isSupported returns false`() {
        // GIVEN
        val storedPaymentMethod = UnsupportedStoredPaymentMethod(
            supportedShopperInteractions = listOf("Ecommerce"),
        )

        // WHEN
        val isSupported = paymentMethodSupportCheck.isSupported(storedPaymentMethod)

        // THEN
        assertFalse(isSupported)
    }

    @Test
    fun `when stored payment method does not support Ecommerce, then isSupported returns false`() {
        // GIVEN
        val storedPaymentMethod = StoredCardPaymentMethod(
            type = PaymentMethodTypes.SCHEME,
            id = "123",
            name = "Cards",
            brand = "visa",
            lastFour = "1234",
            expiryMonth = "12",
            expiryYear = "2025",
            holderName = "Test",
            fundingSource = null,
            supportedShopperInteractions = listOf("ContAuth"),
        )

        // WHEN
        val isSupported = paymentMethodSupportCheck.isSupported(storedPaymentMethod)

        // THEN
        assertFalse(isSupported)
    }

    private class UnsupportedPaymentMethod : PaymentMethod() {
        override val type: String = PaymentMethodTypes.UNSUPPORTED_PAYMENT_METHODS.first()
        override val name: String = "Unknown"

        override fun writeToParcel(p0: Parcel, p1: Int) = Unit
    }

    private class UnsupportedStoredPaymentMethod(
        override val supportedShopperInteractions: List<String> = emptyList()
    ) : StoredPaymentMethod() {
        override val type: String = "unknown_type"
        override val name: String = "Unknown"
        override val id: String = "unknown"

        override fun writeToParcel(p0: Parcel, p1: Int) = Unit
    }
}
