/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/2/2023.
 */

package com.adyen.checkout.components.core.internal

import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.TestComponentState
import com.adyen.checkout.core.AdyenLogger
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.internal.util.Logger
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

internal class DefaultComponentEventHandlerTest {

    private lateinit var componentEventHandler: DefaultComponentEventHandler<PaymentComponentState<*>>

    @BeforeEach
    fun beforeEach() {
        componentEventHandler = DefaultComponentEventHandler()
        AdyenLogger.setLogLevel(Logger.NONE)
    }

    @Nested
    @DisplayName("when payment component event")
    inner class OnPaymentComponentEventTest {

        @Test
        fun `and component callback is wrongly typed, then an error should be thrown`() {
            assertThrows<CheckoutException> {
                componentEventHandler.onPaymentComponentEvent(
                    PaymentComponentEvent.Submit(createPaymentComponentState()),
                    object : BaseComponentCallback {}
                )
            }
        }

        @Test
        fun `is ActionDetails, then action should be propagated`() {
            val callback = mock<ComponentCallback<PaymentComponentState<*>>>()
            val actionData = ActionComponentData(paymentData = "test")

            componentEventHandler.onPaymentComponentEvent(
                PaymentComponentEvent.ActionDetails(actionData),
                callback
            )

            verify(callback).onAdditionalDetails(actionData)
        }

        @Test
        fun `is Error, then error should be propagated`() {
            val callback = mock<ComponentCallback<PaymentComponentState<*>>>()
            val error = ComponentError(CheckoutException("Test"))

            componentEventHandler.onPaymentComponentEvent(
                PaymentComponentEvent.Error(error),
                callback
            )

            verify(callback).onError(error)
        }

        @Test
        fun `is StateChanged, then state should be propagated`() {
            val callback = mock<ComponentCallback<PaymentComponentState<*>>>()
            val state = createPaymentComponentState()

            componentEventHandler.onPaymentComponentEvent(
                PaymentComponentEvent.StateChanged(state),
                callback
            )

            verify(callback).onStateChanged(state)
        }

        @Test
        fun `is Submit, then state should be propagated`() {
            val callback = mock<ComponentCallback<PaymentComponentState<*>>>()
            val state = createPaymentComponentState()

            componentEventHandler.onPaymentComponentEvent(
                PaymentComponentEvent.Submit(state),
                callback
            )

            verify(callback).onSubmit(state)
        }
    }

    private fun createPaymentComponentState() = TestComponentState(
        data = PaymentComponentData(null, null, null),
        isInputValid = false,
        isReady = false,
    )
}
