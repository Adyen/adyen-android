/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 */

package com.adyen.checkout.core.common

import com.adyen.checkout.core.action.data.RedirectAction
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.paymentmethod.GenericPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredUnsupportedPaymentMethod
import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.internal.data.model.SessionSetupResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Locale

internal class CheckoutContextTest {

    @Test
    fun `when advanced context has payment methods then returns them`() {
        val expected = listOf(GenericPaymentMethod(type = "scheme", name = "Cards"))
        val context = advancedContext(PaymentMethods(paymentMethods = expected))

        val result = context.getPaymentMethods()

        assertEquals(expected, result)
    }

    @Test
    fun `when advanced context has stored payment methods then returns them`() {
        val expected = listOf(
            StoredUnsupportedPaymentMethod(
                type = "scheme",
                name = "Stored card",
                id = "stored-card",
                supportedShopperInteractions = emptyList(),
            ),
        )
        val context = advancedContext(PaymentMethods(storedPaymentMethods = expected))

        val result = context.getStoredPaymentMethods()

        assertEquals(expected, result)
    }

    @Test
    fun `when sessions context has payment methods then returns them in order`() {
        val expected = listOf(
            GenericPaymentMethod(type = "ideal", name = "iDEAL"),
            GenericPaymentMethod(type = "scheme", name = "Cards"),
        )
        val context = sessionsContext(PaymentMethods(paymentMethods = expected))

        val result = context.getPaymentMethods()

        assertEquals(expected, result)
    }

    @Test
    fun `when sessions context has stored payment methods then returns them`() {
        val expected = listOf(
            StoredUnsupportedPaymentMethod(
                type = "scheme",
                name = "Stored card",
                id = "stored-card",
                supportedShopperInteractions = emptyList(),
            ),
        )
        val context = sessionsContext(PaymentMethods(storedPaymentMethods = expected))

        val result = context.getStoredPaymentMethods()

        assertEquals(expected, result)
    }

    @Test
    fun `when sessions context has no payment methods then returns empty lists`() {
        val context = sessionsContext(paymentMethods = null)

        val paymentMethods = context.getPaymentMethods()
        val storedPaymentMethods = context.getStoredPaymentMethods()

        assertTrue(paymentMethods.isEmpty())
        assertTrue(storedPaymentMethods.isEmpty())
    }

    @Test
    fun `when sessions context has empty payment method lists then returns empty lists`() {
        val context = sessionsContext(
            PaymentMethods(
                paymentMethods = emptyList(),
                storedPaymentMethods = emptyList(),
            ),
        )

        val paymentMethods = context.getPaymentMethods()
        val storedPaymentMethods = context.getStoredPaymentMethods()

        assertTrue(paymentMethods.isEmpty())
        assertTrue(storedPaymentMethods.isEmpty())
    }

    @Test
    fun `when advanced context has null payment method lists then returns empty lists`() {
        val context = advancedContext(PaymentMethods())

        val paymentMethods = context.getPaymentMethods()
        val storedPaymentMethods = context.getStoredPaymentMethods()

        assertTrue(paymentMethods.isEmpty())
        assertTrue(storedPaymentMethods.isEmpty())
    }

    @Test
    fun `when action only context is used then returns empty lists`() {
        val context = CheckoutContext.ActionOnly(
            action = RedirectAction(
                type = "redirect",
                paymentData = "payment-data",
                paymentMethodType = "scheme",
                method = null,
                url = null,
                nativeRedirectData = null,
            ),
            checkoutConfiguration = checkoutConfiguration(),
            checkoutAttemptId = "",
            publicKey = null,
        )

        val paymentMethods = context.getPaymentMethods()
        val storedPaymentMethods = context.getStoredPaymentMethods()

        assertTrue(paymentMethods.isEmpty())
        assertTrue(storedPaymentMethods.isEmpty())
    }

    private fun advancedContext(paymentMethods: PaymentMethods) = CheckoutContext.Advanced(
        paymentMethods = paymentMethods,
        checkoutConfiguration = checkoutConfiguration(),
        checkoutAttemptId = "",
        publicKey = null,
    )

    private fun sessionsContext(paymentMethods: PaymentMethods?) = CheckoutContext.Sessions(
        checkoutSession = CheckoutSession(
            sessionSetupResponse = SessionSetupResponse(
                id = "session-id",
                sessionData = "session-data",
                amount = null,
                expiresAt = "2026-12-31T23:59:59Z",
                paymentMethods = paymentMethods,
                returnUrl = null,
                configuration = null,
                shopperLocale = null,
            ),
            order = null,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        ),
        checkoutConfiguration = checkoutConfiguration(),
        checkoutAttemptId = "",
        publicKey = null,
    )

    private fun checkoutConfiguration() = CheckoutConfiguration(
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
        shopperLocale = Locale.US,
    )

    private companion object {
        const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnm"
    }
}
