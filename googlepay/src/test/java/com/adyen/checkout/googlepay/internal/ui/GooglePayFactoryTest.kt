/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 */

package com.adyen.checkout.googlepay.internal.ui

import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.components.data.model.paymentmethod.GenericPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.GooglePayPaymentMethod
import com.adyen.checkout.core.components.internal.AnalyticsParams
import com.adyen.checkout.core.components.internal.AnalyticsParamsLevel
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.googlepay.internal.helper.GooglePayAvailabilityCheck
import com.adyen.checkout.googlepay.internal.ui.model.GooglePayComponentParams
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.util.Locale

internal class GooglePayFactoryTest {

    @Test
    fun `when the availability check passes then isAvailable returns true`() = runTest {
        val factory = GooglePayFactory { mockAvailabilityCheck(true) }

        assertTrue(factory.isAvailable(createPaymentMethod(), createCheckoutParams()))
    }

    @Test
    fun `when the availability check fails then isAvailable returns false`() = runTest {
        val factory = GooglePayFactory { mockAvailabilityCheck(false) }

        assertFalse(factory.isAvailable(createPaymentMethod(), createCheckoutParams()))
    }

    @Test
    fun `when isAvailable runs then the checkout params are mapped like component creation`() =
        runTest {
            var capturedParams: GooglePayComponentParams? = null
            val factory = GooglePayFactory { componentParams ->
                capturedParams = componentParams
                mockAvailabilityCheck(true)
            }

            factory.isAvailable(createPaymentMethod(), createCheckoutParams())

            assertEquals(TEST_GATEWAY_MERCHANT_ID, capturedParams?.gatewayMerchantId)
        }

    @Test
    fun `when the payment method is not a Google Pay method then isAvailable throws`() = runTest {
        val factory = GooglePayFactory { mockAvailabilityCheck(true) }

        assertThrows<IllegalArgumentException> {
            factory.isAvailable(
                GenericPaymentMethod(type = "not_google_pay", name = "Other"),
                createCheckoutParams(),
            )
        }
    }

    private fun mockAvailabilityCheck(isAvailable: Boolean): GooglePayAvailabilityCheck {
        return mock {
            onBlocking { isAvailable() } doReturn isAvailable
        }
    }

    private fun createPaymentMethod() = GooglePayPaymentMethod(
        type = PaymentMethodTypes.GOOGLE_PAY,
        name = "Google Pay",
        brands = emptyList(),
        configuration = null,
    )

    private fun createCheckoutParams() = CheckoutParams(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
        analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
        amount = null,
        showSubmitButton = true,
        publicKey = null,
        additionalConfigurations = mapOf(
            GooglePayConfiguration::class.java.name to
                GooglePayConfiguration(
                    merchantAccount = TEST_GATEWAY_MERCHANT_ID,
                    googlePayEnvironment = null,
                    totalPriceStatus = null,
                    countryCode = null,
                    merchantInfo = null,
                    allowedPaymentMethods = null,
                    isEmailRequired = null,
                    isExistingPaymentMethodRequired = null,
                    isShippingAddressRequired = null,
                    shippingAddressParameters = null,
                    checkoutOption = null,
                    appearance = null,
                ),
        ),
        additionalSessionParams = null,
    )

    private companion object {
        const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnm"
        const val TEST_GATEWAY_MERCHANT_ID = "TEST_GATEWAY_MERCHANT_ID"
    }
}
