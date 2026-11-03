/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 2/9/2026.
 */

package com.adyen.checkout.core.common.internal

import com.adyen.checkout.core.action.data.RedirectAction
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.paymentmethod.GenericPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods
import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.internal.data.model.SessionSetupResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class CheckoutContextExtTest {

    @Nested
    inner class WithCheckoutConfigurationTest {

        @Test
        fun `when copying advanced context then configuration is replaced and everything else is kept`() {
            val context = CheckoutContext.Advanced(
                paymentMethods = paymentMethods,
                checkoutConfiguration = createConfiguration(showSubmitButton = false),
                checkoutAttemptId = TEST_CHECKOUT_ATTEMPT_ID,
                publicKey = TEST_PUBLIC_KEY,
            )

            val actual = context.withCheckoutConfiguration(createConfiguration(showSubmitButton = true))

            val expected = CheckoutContext.Advanced(
                paymentMethods = paymentMethods,
                checkoutConfiguration = createConfiguration(showSubmitButton = true),
                checkoutAttemptId = TEST_CHECKOUT_ATTEMPT_ID,
                publicKey = TEST_PUBLIC_KEY,
            )
            assertEquals(expected, actual)
        }

        @Test
        fun `when copying sessions context then configuration is replaced and everything else is kept`() {
            val context = CheckoutContext.Sessions(
                checkoutSession = checkoutSession,
                checkoutConfiguration = createConfiguration(showSubmitButton = false),
                checkoutAttemptId = TEST_CHECKOUT_ATTEMPT_ID,
                publicKey = TEST_PUBLIC_KEY,
            )

            val actual = context.withCheckoutConfiguration(createConfiguration(showSubmitButton = true))

            val expected = CheckoutContext.Sessions(
                checkoutSession = checkoutSession,
                checkoutConfiguration = createConfiguration(showSubmitButton = true),
                checkoutAttemptId = TEST_CHECKOUT_ATTEMPT_ID,
                publicKey = TEST_PUBLIC_KEY,
            )
            assertEquals(expected, actual)
        }

        @Test
        fun `when copying action only context then configuration is replaced and everything else is kept`() {
            val context = CheckoutContext.ActionOnly(
                action = action,
                checkoutConfiguration = createConfiguration(showSubmitButton = false),
                checkoutAttemptId = TEST_CHECKOUT_ATTEMPT_ID,
                publicKey = TEST_PUBLIC_KEY,
            )

            val actual = context.withCheckoutConfiguration(createConfiguration(showSubmitButton = true))

            val expected = CheckoutContext.ActionOnly(
                action = action,
                checkoutConfiguration = createConfiguration(showSubmitButton = true),
                checkoutAttemptId = TEST_CHECKOUT_ATTEMPT_ID,
                publicKey = TEST_PUBLIC_KEY,
            )
            assertEquals(expected, actual)
        }
    }

    private val paymentMethods = PaymentMethods(
        paymentMethods = listOf(GenericPaymentMethod(type = "ideal", name = "iDEAL")),
    )

    private val checkoutSession = CheckoutSession(
        sessionSetupResponse = SessionSetupResponse(
            id = "test_session_id",
            sessionData = "test_session_data",
            amount = null,
            expiresAt = "2026-12-31T23:59:59Z",
            paymentMethods = null,
            returnUrl = null,
            configuration = null,
            shopperLocale = null,
        ),
        order = null,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
    )

    private val action = RedirectAction(
        type = RedirectAction.ACTION_TYPE,
        paymentData = null,
        paymentMethodType = "scheme",
        method = null,
        url = null,
        nativeRedirectData = null,
    )

    private fun createConfiguration(showSubmitButton: Boolean?) = CheckoutConfiguration(
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
        showSubmitButton = showSubmitButton,
    )

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfgh"
        private const val TEST_CHECKOUT_ATTEMPT_ID = "test_checkout_attempt_id"
        private const val TEST_PUBLIC_KEY = "test_public_key"
    }
}
