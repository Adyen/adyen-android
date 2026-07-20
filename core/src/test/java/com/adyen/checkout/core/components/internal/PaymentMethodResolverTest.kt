/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 23/6/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.action.data.RedirectAction
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.CheckoutTarget
import com.adyen.checkout.core.components.data.model.paymentmethod.GenericPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredUnsupportedPaymentMethod
import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.internal.data.model.SessionSetupResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Locale

internal class PaymentMethodResolverTest {

    @Nested
    inner class PaymentMethodTargetTest {

        @Test
        fun `when payment method is found, then it is returned`() {
            val expected = GenericPaymentMethod(type = "scheme", name = "Card")
            val context = advancedContext(
                PaymentMethods(paymentMethods = listOf(expected)),
            )

            val result = PaymentMethodResolver.resolve(
                target = CheckoutTarget.PaymentMethod("scheme"),
                context = context,
            )

            assertEquals(expected, result)
        }

        @Test
        fun `when payment method is not found, then null is returned`() {
            val context = advancedContext(
                PaymentMethods(
                    paymentMethods = listOf(GenericPaymentMethod(type = "ideal", name = "iDEAL")),
                ),
            )

            val result = PaymentMethodResolver.resolve(
                target = CheckoutTarget.PaymentMethod("scheme"),
                context = context,
            )

            assertNull(result)
        }

        @Test
        fun `when payment methods list is null, then null is returned`() {
            val context = advancedContext(
                PaymentMethods(paymentMethods = null),
            )

            val result = PaymentMethodResolver.resolve(
                target = CheckoutTarget.PaymentMethod("scheme"),
                context = context,
            )

            assertNull(result)
        }

        @Test
        fun `when multiple payment methods exist, then the matching one is returned`() {
            val expected = GenericPaymentMethod(type = "scheme", name = "Card")
            val context = advancedContext(
                PaymentMethods(
                    paymentMethods = listOf(
                        GenericPaymentMethod(type = "ideal", name = "iDEAL"),
                        expected,
                        GenericPaymentMethod(type = "paypal", name = "PayPal"),
                    ),
                ),
            )

            val result = PaymentMethodResolver.resolve(
                target = CheckoutTarget.PaymentMethod("scheme"),
                context = context,
            )

            assertEquals(expected, result)
        }
    }

    @Nested
    inner class StoredPaymentMethodTargetTest {

        @Test
        fun `when stored payment method is found, then it is returned`() {
            val expected = StoredUnsupportedPaymentMethod(
                type = "scheme",
                name = "Card",
                id = "test_id",
                supportedShopperInteractions = emptyList(),
            )
            val context = advancedContext(
                PaymentMethods(storedPaymentMethods = listOf(expected)),
            )

            val result = PaymentMethodResolver.resolve(
                target = CheckoutTarget.StoredPaymentMethod("test_id"),
                context = context,
            )

            assertEquals(expected, result)
        }

        @Test
        fun `when stored payment method is not found, then null is returned`() {
            val context = advancedContext(
                PaymentMethods(
                    storedPaymentMethods = listOf(
                        StoredUnsupportedPaymentMethod(
                            type = "scheme",
                            name = "Card",
                            id = "other_id",
                            supportedShopperInteractions = emptyList(),
                        ),
                    ),
                ),
            )

            val result = PaymentMethodResolver.resolve(
                target = CheckoutTarget.StoredPaymentMethod("test_id"),
                context = context,
            )

            assertNull(result)
        }

        @Test
        fun `when stored payment methods list is null, then null is returned`() {
            val context = advancedContext(
                PaymentMethods(storedPaymentMethods = null),
            )

            val result = PaymentMethodResolver.resolve(
                target = CheckoutTarget.StoredPaymentMethod("test_id"),
                context = context,
            )

            assertNull(result)
        }

        @Test
        fun `when multiple stored payment methods exist, then the matching one is returned`() {
            val expected = StoredUnsupportedPaymentMethod(
                type = "scheme",
                name = "Card",
                id = "test_id",
                supportedShopperInteractions = emptyList(),
            )
            val context = advancedContext(
                PaymentMethods(
                    storedPaymentMethods = listOf(
                        StoredUnsupportedPaymentMethod(
                            type = "blik",
                            name = "BLIK",
                            id = "other_id",
                            supportedShopperInteractions = emptyList(),
                        ),
                        expected,
                    ),
                ),
            )

            val result = PaymentMethodResolver.resolve(
                target = CheckoutTarget.StoredPaymentMethod("test_id"),
                context = context,
            )

            assertEquals(expected, result)
        }
    }

    @Nested
    inner class UnknownTargetTest {

        @Test
        fun `when target is unknown, then null is returned`() {
            val context = advancedContext(PaymentMethods())

            val result = PaymentMethodResolver.resolve(
                target = object : CheckoutTarget {},
                context = context,
            )

            assertNull(result)
        }
    }

    @Nested
    inner class CheckoutContextTest {

        @Test
        fun `when context is ActionOnly, then null is returned`() {
            val result = PaymentMethodResolver.resolve(
                target = CheckoutTarget.PaymentMethod("scheme"),
                context = actionOnlyContext(),
            )

            assertNull(result)
        }

        @Test
        fun `when context is Sessions, then payment method is resolved from session setup response`() {
            val expected = GenericPaymentMethod(type = "scheme", name = "Card")
            val context = sessionsContext(
                PaymentMethods(paymentMethods = listOf(expected)),
            )

            val result = PaymentMethodResolver.resolve(
                target = CheckoutTarget.PaymentMethod("scheme"),
                context = context,
            )

            assertEquals(expected, result)
        }

        @Test
        fun `when context is Sessions, then stored payment method is resolved from session setup response`() {
            val expected = StoredUnsupportedPaymentMethod(
                type = "scheme",
                name = "Card",
                id = "test_id",
                supportedShopperInteractions = emptyList(),
            )
            val context = sessionsContext(
                PaymentMethods(storedPaymentMethods = listOf(expected)),
            )

            val result = PaymentMethodResolver.resolve(
                target = CheckoutTarget.StoredPaymentMethod("test_id"),
                context = context,
            )

            assertEquals(expected, result)
        }

        @Test
        fun `when context is Sessions and payment methods are null, then null is returned`() {
            val context = sessionsContext(paymentMethods = null)

            val result = PaymentMethodResolver.resolve(
                target = CheckoutTarget.PaymentMethod("scheme"),
                context = context,
            )

            assertNull(result)
        }
    }

    private fun advancedContext(paymentMethods: PaymentMethods) = CheckoutContext.Advanced(
        paymentMethods = paymentMethods,
        checkoutConfiguration = CheckoutConfiguration(
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
            shopperLocale = Locale.US,
        ),
        checkoutAttemptId = "",
        publicKey = null,
    )

    private fun actionOnlyContext() = CheckoutContext.ActionOnly(
        action = RedirectAction(
            type = "redirect",
            paymentData = "test_data",
            paymentMethodType = "scheme",
            method = null,
            url = null,
            nativeRedirectData = null,
        ),
        checkoutConfiguration = CheckoutConfiguration(
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
            shopperLocale = Locale.US,
        ),
        checkoutAttemptId = "",
        publicKey = null,
    )

    private fun sessionsContext(paymentMethods: PaymentMethods?) = CheckoutContext.Sessions(
        checkoutSession = CheckoutSession(
            sessionSetupResponse = SessionSetupResponse(
                id = "test_session_id",
                sessionData = "test_session_data",
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
        checkoutConfiguration = CheckoutConfiguration(
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
            shopperLocale = Locale.US,
        ),
        checkoutAttemptId = "",
        publicKey = null,
    )

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnm"
    }
}
