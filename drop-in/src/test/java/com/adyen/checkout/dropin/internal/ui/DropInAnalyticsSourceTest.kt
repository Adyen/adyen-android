/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 29/10/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import com.adyen.checkout.core.analytics.internal.AnalyticsSource
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.TestCheckoutContext
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.paymentmethod.GenericPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCardPaymentMethod
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DropInAnalyticsSourceTest {

    @Test
    fun `when created, then the source is Drop-in with the regular payment method types in order`() {
        val checkoutContext = createCheckoutContext(
            paymentMethods = listOf(
                GenericPaymentMethod(type = PaymentMethodTypes.SCHEME, name = "Cards"),
                GenericPaymentMethod(type = PaymentMethodTypes.IDEAL, name = "iDEAL"),
            ),
            storedPaymentMethods = listOf(STORED_CARD),
        )

        val source = checkoutContext.dropInAnalyticsSource()

        assertEquals(AnalyticsSource.DropIn(listOf(PaymentMethodTypes.SCHEME, PaymentMethodTypes.IDEAL)), source)
    }

    @Test
    fun `when there are only stored payment methods, then the source lists no payment methods`() {
        val checkoutContext = createCheckoutContext(
            paymentMethods = emptyList(),
            storedPaymentMethods = listOf(STORED_CARD),
        )

        val source = checkoutContext.dropInAnalyticsSource()

        assertEquals(AnalyticsSource.DropIn(emptyList()), source)
    }

    private fun createCheckoutContext(
        paymentMethods: List<GenericPaymentMethod>,
        storedPaymentMethods: List<StoredCardPaymentMethod>,
    ) = TestCheckoutContext.advanced(
        paymentMethods = PaymentMethods(
            paymentMethods = paymentMethods,
            storedPaymentMethods = storedPaymentMethods,
        ),
        checkoutConfiguration = CheckoutConfiguration(environment = Environment.TEST, clientKey = CLIENT_KEY),
    )

    private companion object {
        private const val CLIENT_KEY = "test_client_key"

        private val STORED_CARD = StoredCardPaymentMethod(
            type = PaymentMethodTypes.SCHEME,
            name = "Visa",
            id = "stored-id-1",
            supportedShopperInteractions = listOf("Ecommerce"),
            brand = "visa",
            lastFour = "1234",
            expiryMonth = "01",
            expiryYear = "2030",
            holderName = null,
            fundingSource = null,
        )
    }
}
