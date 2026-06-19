/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 18/6/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.action.data.RedirectAction
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.analytics.internal.TestAnalyticsManager
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.components.AdditionalDetailsResult
import com.adyen.checkout.core.components.AdvancedCheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutAdditionalCallback
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.CheckoutTarget
import com.adyen.checkout.core.components.SubmitResult
import com.adyen.checkout.core.components.data.model.paymentmethod.GenericPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredBLIKPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.UnsupportedPaymentMethod
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.TestPaymentComponent
import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.internal.data.model.SessionSetupResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Locale

internal class PaymentComponentResolverTest {

    @BeforeEach
    fun setUp() {
        PaymentMethodProvider.clear()
    }

    @Test
    fun `when payment methods response has no payment methods, then Failure is returned`() = runTest {
        val result = resolve(
            target = CheckoutTarget.PaymentMethod("scheme"),
            paymentMethods = PaymentMethods(paymentMethods = null),
        )

        assertEquals(PaymentComponentResult.Failure("No payment methods response available."), result)
    }

    @Test
    fun `when target payment method is not in the response, then Failure is returned`() = runTest {
        val result = resolve(
            target = CheckoutTarget.PaymentMethod("scheme"),
            paymentMethods = PaymentMethods(
                paymentMethods = listOf(GenericPaymentMethod(type = "ideal", name = "iDEAL")),
            ),
        )

        assertEquals(
            PaymentComponentResult.Failure(
                "Payment method 'scheme' was not found in the payment methods response.",
            ),
            result,
        )
    }

    @Test
    fun `when payment method is not supported, then Failure is returned`() = runTest {
        val result = resolve(
            target = CheckoutTarget.PaymentMethod("scheme"),
            paymentMethods = PaymentMethods(
                paymentMethods = listOf(UnsupportedPaymentMethod(type = "scheme", name = "Card")),
            ),
        )

        assertEquals(
            PaymentComponentResult.Failure(
                "Payment method 'scheme' is not supported. " +
                    "Ensure the corresponding module is included in your build dependencies.",
            ),
            result,
        )
    }

    @Test
    fun `when payment methods response has no stored payment methods, then Failure is returned`() = runTest {
        val result = resolve(
            target = CheckoutTarget.StoredPaymentMethod("test_id"),
            paymentMethods = PaymentMethods(storedPaymentMethods = null),
        )

        assertEquals(PaymentComponentResult.Failure("No payment methods response available."), result)
    }

    @Test
    fun `when target stored payment method is not in the response, then Failure is returned`() = runTest {
        val result = resolve(
            target = CheckoutTarget.StoredPaymentMethod("test_id"),
            paymentMethods = PaymentMethods(
                storedPaymentMethods = listOf(
                    StoredBLIKPaymentMethod(
                        type = "blik",
                        name = "BLIK",
                        id = "other_id",
                        supportedShopperInteractions = emptyList(),
                    ),
                ),
            ),
        )

        assertEquals(
            PaymentComponentResult.Failure(
                "Stored payment method with id 'test_id' was not found in the payment methods response.",
            ),
            result,
        )
    }

    @Test
    fun `when stored payment method is not supported, then Failure is returned`() = runTest {
        val result = resolve(
            target = CheckoutTarget.StoredPaymentMethod("test_id"),
            paymentMethods = PaymentMethods(
                storedPaymentMethods = listOf(
                    StoredBLIKPaymentMethod(
                        type = "blik",
                        name = "BLIK",
                        id = "test_id",
                        supportedShopperInteractions = emptyList(),
                    ),
                ),
            ),
        )

        assertEquals(
            PaymentComponentResult.Failure(
                "Stored payment method type 'blik' is not supported. " +
                    "Ensure the corresponding module is included in your build dependencies.",
            ),
            result,
        )
    }

    @Test
    fun `when checkout target is unknown, then Failure is returned`() = runTest {
        val result = resolve(
            target = object : CheckoutTarget {},
            paymentMethods = PaymentMethods(),
        )

        assertEquals(PaymentComponentResult.Failure("Unsupported checkout target."), result)
    }

    @Test
    fun `when payment method is supported, then Success with the component is returned`() = runTest {
        val component = TestPaymentComponent()
        registerFactory(type = "scheme", component = component)

        val result = resolve(
            target = CheckoutTarget.PaymentMethod("scheme"),
            paymentMethods = PaymentMethods(
                paymentMethods = listOf(GenericPaymentMethod(type = "scheme", name = "Card")),
            ),
        )

        assertEquals(PaymentComponentResult.Success(component), result)
    }

    @Test
    fun `when stored payment method is supported, then Success with the component is returned`() = runTest {
        val component = TestPaymentComponent()
        registerStoredFactory(type = "blik", component = component)

        val result = resolve(
            target = CheckoutTarget.StoredPaymentMethod("test_id"),
            paymentMethods = PaymentMethods(
                storedPaymentMethods = listOf(
                    StoredBLIKPaymentMethod(
                        type = "blik",
                        name = "BLIK",
                        id = "test_id",
                        supportedShopperInteractions = emptyList(),
                    ),
                ),
            ),
        )

        assertEquals(PaymentComponentResult.Success(component), result)
    }

    @Test
    fun `when context is ActionOnly, then Failure is returned`() = runTest {
        val result = PaymentComponentResolver.resolve(
            target = CheckoutTarget.PaymentMethod("scheme"),
            context = actionOnlyContext(),
            callbacks = advancedCallbacks(),
            coroutineScope = this,
            analyticsManager = TestAnalyticsManager(),
            checkoutParams = checkoutParams(),
        )

        assertEquals(PaymentComponentResult.Failure("No payment methods response available."), result)
    }

    @Test
    fun `when context is Sessions, then payment methods are resolved from the session setup response`() = runTest {
        val component = TestPaymentComponent()
        registerFactory(type = "scheme", component = component)

        val result = PaymentComponentResolver.resolve(
            target = CheckoutTarget.PaymentMethod("scheme"),
            context = sessionsContext(
                PaymentMethods(
                    paymentMethods = listOf(GenericPaymentMethod(type = "scheme", name = "Card")),
                ),
            ),
            callbacks = advancedCallbacks(),
            coroutineScope = this,
            analyticsManager = TestAnalyticsManager(),
            checkoutParams = checkoutParams(),
        )

        assertEquals(PaymentComponentResult.Success(component), result)
    }

    private fun CoroutineScope.resolve(
        target: CheckoutTarget,
        paymentMethods: PaymentMethods,
    ): PaymentComponentResult = PaymentComponentResolver.resolve(
        target = target,
        context = advancedContext(paymentMethods),
        callbacks = advancedCallbacks(),
        coroutineScope = this,
        analyticsManager = TestAnalyticsManager(),
        checkoutParams = checkoutParams(),
    )

    private fun advancedContext(paymentMethods: PaymentMethods) = CheckoutContext.Advanced(
        paymentMethods = paymentMethods,
        checkoutConfiguration = CheckoutConfiguration(
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
            shopperLocale = Locale.US,
        ),
        checkoutAttemptId = null,
        publicKey = null,
    )

    private fun actionOnlyContext() = CheckoutContext.ActionOnly(
        action = RedirectAction(
            type = "redirect",
            paymentData = "test_data",
            paymentMethodType = "scheme",
        ),
        checkoutConfiguration = CheckoutConfiguration(
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
            shopperLocale = Locale.US,
        ),
        checkoutAttemptId = null,
        publicKey = null,
    )

    private fun sessionsContext(paymentMethods: PaymentMethods) = CheckoutContext.Sessions(
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
        checkoutAttemptId = null,
        publicKey = null,
    )

    private fun advancedCallbacks() = AdvancedCheckoutCallbacks(
        onSubmit = { SubmitResult.Completion("") },
        onAdditionalDetails = { AdditionalDetailsResult.Completion("") },
        onFailure = {},
    )

    private fun checkoutParams() = CheckoutParams(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
        analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
        amount = null,
        showSubmitButton = true,
        publicKey = "test_publicKey",
        additionalConfigurations = emptyMap(),
        additionalSessionParams = null,
    )

    private fun registerFactory(type: String, component: PaymentComponent) {
        PaymentMethodProvider.register(
            type,
            object : PaymentComponentFactory<PaymentComponent> {
                override fun create(
                    paymentMethod: PaymentMethod,
                    coroutineScope: CoroutineScope,
                    analyticsManager: AnalyticsManager,
                    params: CheckoutParams,
                    additionalCallbacks: Set<CheckoutAdditionalCallback>,
                ): PaymentComponent = component
            },
        )
    }

    private fun registerStoredFactory(type: String, component: PaymentComponent) {
        PaymentMethodProvider.register(
            type,
            object : StoredPaymentComponentFactory<PaymentComponent> {
                override fun create(
                    storedPaymentMethod: StoredPaymentMethod,
                    coroutineScope: CoroutineScope,
                    analyticsManager: AnalyticsManager,
                    params: CheckoutParams,
                ): PaymentComponent = component
            },
        )
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnm"
    }
}
