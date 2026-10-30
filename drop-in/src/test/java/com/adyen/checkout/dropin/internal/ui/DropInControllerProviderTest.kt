/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 2/9/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.TestCheckoutContext
import com.adyen.checkout.core.components.AdvancedCheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.SessionCheckoutCallbacks
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods
import com.adyen.checkout.core.components.internal.CheckoutControllerFactory
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.internal.data.model.SessionSetupResponse
import com.adyen.checkout.dropin.DropInService
import com.adyen.checkout.dropin.internal.service.DropInServiceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

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

    @Test
    fun `when a controller is provided, then it is created by the injected controller factory`() {
        val controller = mock<CheckoutController>()
        val controllerFactory = mock<CheckoutControllerFactory> {
            on {
                create(
                    target = any(),
                    context = any<CheckoutContext.Advanced>(),
                    callbacks = any<AdvancedCheckoutCallbacks>(),
                    coroutineScope = any(),
                    savedStateHandle = any(),
                )
            } doReturn controller
        }
        val provider = DefaultDropInControllerProvider(
            checkoutContext = createCheckoutContext(showSubmitButton = null),
            dropInServiceManager = DropInServiceManager(DropInService::class.java),
            controllerFactory = controllerFactory,
        )

        val result = provider.provide(
            paymentFlowType = DropInPaymentFlowType.RegularPaymentMethod(PaymentMethodTypes.SCHEME),
            coroutineScope = CoroutineScope(Dispatchers.Unconfined),
            onAction = {},
        )

        assertSame(controller, result)
    }

    @Test
    fun `when a controller is provided for a session, then it is created by the injected controller factory`() {
        val controller = mock<CheckoutController>()
        val controllerFactory = mock<CheckoutControllerFactory> {
            on {
                create(
                    target = any(),
                    context = any<CheckoutContext.Sessions>(),
                    callbacks = any<SessionCheckoutCallbacks>(),
                    coroutineScope = any(),
                    savedStateHandle = any(),
                )
            } doReturn controller
        }
        val provider = DefaultDropInControllerProvider(
            checkoutContext = TestCheckoutContext.sessions(
                checkoutSession = CHECKOUT_SESSION,
                checkoutConfiguration = CheckoutConfiguration(
                    environment = Environment.TEST,
                    clientKey = TEST_CLIENT_KEY,
                ),
            ),
            dropInServiceManager = DropInServiceManager(DropInService::class.java),
            controllerFactory = controllerFactory,
        )

        val result = provider.provide(
            paymentFlowType = DropInPaymentFlowType.StoredPaymentMethod(STORED_ID),
            coroutineScope = CoroutineScope(Dispatchers.Unconfined),
            onAction = {},
        )

        assertSame(controller, result)
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
        private const val STORED_ID = "stored-id-1"

        private val CHECKOUT_SESSION = CheckoutSession(
            sessionSetupResponse = SessionSetupResponse(
                id = "session-id",
                sessionData = "session-data",
                amount = null,
                expiresAt = "2026-10-16T12:00:00Z",
                paymentMethods = null,
                returnUrl = null,
                configuration = null,
                shopperLocale = null,
            ),
            order = null,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        )
    }
}
