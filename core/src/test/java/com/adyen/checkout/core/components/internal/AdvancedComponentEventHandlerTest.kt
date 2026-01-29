/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 7/1/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.action.internal.ActionComponentEvent
import com.adyen.checkout.core.common.exception.ComponentError
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.paymentmethod.TestPaymentComponentState
import com.adyen.checkout.core.error.CheckoutError
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
internal class AdvancedComponentEventHandlerTest(
    @Mock private val componentCallbacks: AdvancedComponentCallbacks,
) {

    private lateinit var advancedComponentEventHandler: AdvancedComponentEventHandler<TestPaymentComponentState>

    @BeforeEach
    fun beforeEach() {
        advancedComponentEventHandler = AdvancedComponentEventHandler(
            componentCallbacks = componentCallbacks,
        )
    }

    @Nested
    @DisplayName("when onPaymentComponentEvent is called")
    inner class OnPaymentComponentEventTest {

        @Test
        fun `with Submit event, then beforeSubmit and onSubmit are called and result is returned`() = runTest {
            val expectedResult = CheckoutResult.Finished()
            whenever(componentCallbacks.beforeSubmit(any())) doReturn true
            whenever(componentCallbacks.onSubmit(any())) doReturn expectedResult

            val state = TestPaymentComponentState()
            val result = advancedComponentEventHandler.onPaymentComponentEvent(PaymentComponentEvent.Submit(state))

            verify(componentCallbacks).beforeSubmit(state)
            verify(componentCallbacks).onSubmit(state)
            assertEquals(expectedResult, result)
        }

        @Test
        fun `with Error event, then onError is called and error result is returned`() = runTest {
            val error = ComponentError(message = "test_error")
            val event = PaymentComponentEvent.Error<TestPaymentComponentState>(error)

            val result = advancedComponentEventHandler.onPaymentComponentEvent(event)

            verify(componentCallbacks).onError(any<CheckoutError>())
            assertEquals(CheckoutResult.Error("test_error"), result)
        }
    }

    @Nested
    @DisplayName("when onActionComponentEvent is called")
    inner class OnActionComponentEventTest {

        @Test
        fun `with ActionDetails event, then onAdditionalDetails is called and result is returned`() = runTest {
            val expectedResult = CheckoutResult.Finished()
            whenever(componentCallbacks.onAdditionalDetails(any())) doReturn expectedResult

            val data = ActionComponentData(paymentData = "test")
            val result = advancedComponentEventHandler.onActionComponentEvent(ActionComponentEvent.ActionDetails(data))

            verify(componentCallbacks).onAdditionalDetails(data)
            assertEquals(expectedResult, result)
        }

        @Test
        fun `with Error event, then onError is called and error result is returned`() = runTest {
            val error = ComponentError(message = "test_error")
            val event = ActionComponentEvent.Error(error)

            val result = advancedComponentEventHandler.onActionComponentEvent(event)

            verify(componentCallbacks).onError(any<CheckoutError>())
            assertEquals(CheckoutResult.Error("test_error"), result)
        }
    }
}
