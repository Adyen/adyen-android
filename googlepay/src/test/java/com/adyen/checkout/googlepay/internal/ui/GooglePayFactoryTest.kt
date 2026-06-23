/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 23/6/2026.
 */

package com.adyen.checkout.googlepay.internal.ui

import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.LoggingExtension
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.components.data.model.paymentmethod.GooglePayPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.internal.AnalyticsParams
import com.adyen.checkout.core.components.internal.AnalyticsParamsLevel
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.googlepay.GooglePayConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.mock
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(LoggingExtension::class)
internal class GooglePayFactoryTest {

    private val factory = GooglePayFactory()

    @Test
    fun `when create is called with a GooglePayPaymentMethod, then a GooglePayComponent is created`() {
        val component = factory.create(
            paymentMethod = createPaymentMethod(),
            coroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
            analyticsManager = mock(),
            params = createCheckoutParams(),
            additionalCallbacks = emptySet(),
        )

        assertNotNull(component)
    }

    @Test
    fun `when create is called with a non GooglePayPaymentMethod, then an error is thrown`() {
        assertThrows<IllegalArgumentException> {
            factory.create(
                paymentMethod = mock<PaymentMethod>(),
                coroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
                analyticsManager = mock(),
                params = createCheckoutParams(),
                additionalCallbacks = emptySet(),
            )
        }
    }

    private fun createCheckoutParams() = CheckoutParams(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
        analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
        amount = null,
        showSubmitButton = false,
        publicKey = null,
        additionalConfigurations = mapOf(GooglePayConfiguration::class.java.name to createGooglePayConfiguration()),
        additionalSessionParams = null,
    )

    private fun createGooglePayConfiguration() = GooglePayConfiguration(
        merchantAccount = TEST_GATEWAY_MERCHANT_ID,
        googlePayEnvironment = null,
        totalPriceStatus = null,
        countryCode = null,
        merchantInfo = null,
        allowedAuthMethods = null,
        allowedCardNetworks = null,
        isAllowPrepaidCards = null,
        isAllowCreditCards = null,
        isAssuranceDetailsRequired = null,
        isEmailRequired = null,
        isExistingPaymentMethodRequired = null,
        isShippingAddressRequired = null,
        shippingAddressParameters = null,
        isBillingAddressRequired = null,
        billingAddressParameters = null,
        checkoutOption = null,
        googlePayButtonStyling = null,
    )

    private fun createPaymentMethod() = GooglePayPaymentMethod(
        type = PaymentMethodTypes.GOOGLE_PAY,
        name = "Google Pay",
        brands = emptyList(),
        configuration = null,
    )

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_GATEWAY_MERCHANT_ID = "TEST_GATEWAY_MERCHANT_ID"
    }
}
