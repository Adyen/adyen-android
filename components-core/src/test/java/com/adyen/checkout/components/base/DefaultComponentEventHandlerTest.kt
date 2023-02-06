/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/2/2023.
 */

package com.adyen.checkout.components.base

import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.Logger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class DefaultComponentEventHandlerTest {

    private lateinit var componentEventHandler: DefaultComponentEventHandler<PaymentComponentState<*>>

    @BeforeEach
    fun beforeEach() {
        componentEventHandler = DefaultComponentEventHandler()
        Logger.setLogcatLevel(Logger.NONE)
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
            val callback = TestComponentCallback()
            val actionData = ActionComponentData(paymentData = "test")

            componentEventHandler.onPaymentComponentEvent(
                PaymentComponentEvent.ActionDetails(actionData),
                callback
            )

            callback.assertOnAdditionalDetailsEquals(actionData)
        }

        @Test
        fun `is Error, then error should be propagated`() {
            val callback = TestComponentCallback()
            val error = ComponentError(CheckoutException("Test"))

            componentEventHandler.onPaymentComponentEvent(
                PaymentComponentEvent.Error(error),
                callback
            )

            callback.assertOnErrorEquals(error)
        }

        @Test
        fun `is StateChanged, then state should be propagated`() {
            val callback = TestComponentCallback()
            val state = createPaymentComponentState()

            componentEventHandler.onPaymentComponentEvent(
                PaymentComponentEvent.StateChanged(state),
                callback
            )

            callback.assertOnStateChangedEquals(state)
        }

        @Test
        fun `is Submit, then state should be propagated`() {
            val callback = TestComponentCallback()
            val state = createPaymentComponentState()

            componentEventHandler.onPaymentComponentEvent(
                PaymentComponentEvent.Submit(state),
                callback
            )

            callback.assertOnSubmitEquals(state)
        }
    }

    private fun createPaymentComponentState() = PaymentComponentState(
        data = PaymentComponentData(),
        isInputValid = false,
        isReady = false,
    )

    private class TestComponentCallback : ComponentCallback<PaymentComponentState<*>> {

        private var onSubmitValue: PaymentComponentState<*>? = null

        private var onAdditionalDetailsValue: ActionComponentData? = null

        private var onErrorValue: ComponentError? = null

        private var onStateChangedValue: PaymentComponentState<*>? = null

        override fun onSubmit(state: PaymentComponentState<*>) {
            onSubmitValue = state
        }

        override fun onAdditionalDetails(actionComponentData: ActionComponentData) {
            onAdditionalDetailsValue = actionComponentData
        }

        override fun onError(componentError: ComponentError) {
            onErrorValue = componentError
        }

        override fun onStateChanged(state: PaymentComponentState<*>) {
            onStateChangedValue = state
        }

        fun assertOnSubmitEquals(expected: PaymentComponentState<*>?) {
            assertEquals(expected, onSubmitValue)
        }

        fun assertOnAdditionalDetailsEquals(expected: ActionComponentData?) {
            assertEquals(expected, onAdditionalDetailsValue)
        }

        fun assertOnErrorEquals(expected: ComponentError?) {
            assertEquals(expected, onErrorValue)
        }

        fun assertOnStateChangedEquals(expected: PaymentComponentState<*>?) {
            assertEquals(expected, onStateChangedValue)
        }
    }
}
