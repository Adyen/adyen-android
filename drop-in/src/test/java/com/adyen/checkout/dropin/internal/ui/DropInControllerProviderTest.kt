/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 2/9/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.TestCheckoutContext
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DropInControllerProviderTest {

    @Test
    fun `when configuration hides the submit button then context enforces it`() {
        val checkoutContext = createCheckoutContext(showSubmitButton = false)

        val actual = checkoutContext.withSubmitButtonEnforced()

        assertEquals(true, actual.checkoutConfiguration.showSubmitButton)
    }

    @Test
    fun `when configuration does not set the submit button then context enforces it`() {
        val checkoutContext = createCheckoutContext(showSubmitButton = null)

        val actual = checkoutContext.withSubmitButtonEnforced()

        assertEquals(true, actual.checkoutConfiguration.showSubmitButton)
    }

    @Test
    fun `when enforcing the submit button then the rest of the configuration is kept`() {
        val checkoutContext = createCheckoutContext(showSubmitButton = false)

        val actual = checkoutContext.withSubmitButtonEnforced()

        assertEquals(Environment.TEST, actual.checkoutConfiguration.environment)
        assertEquals(TEST_CLIENT_KEY, actual.checkoutConfiguration.clientKey)
    }

    private fun createCheckoutContext(showSubmitButton: Boolean?) = TestCheckoutContext.advanced(
        paymentMethods = PaymentMethods(),
        checkoutConfiguration = CheckoutConfiguration(
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
            showSubmitButton = showSubmitButton,
        ),
    )

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfgh"
    }
}
