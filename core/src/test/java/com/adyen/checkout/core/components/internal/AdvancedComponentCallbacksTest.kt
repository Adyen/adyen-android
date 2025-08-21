/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 19/8/2025.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.common.PaymentResult
import com.adyen.checkout.core.components.BeforeSubmitCallback
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.ComponentError
import com.adyen.checkout.core.components.OnAdditionalDetailsCallback
import com.adyen.checkout.core.components.OnErrorCallback
import com.adyen.checkout.core.components.OnFinishedCallback
import com.adyen.checkout.core.components.OnSubmitCallback
import com.adyen.checkout.core.components.paymentmethod.TestPaymentComponentState
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
internal class AdvancedComponentCallbacksTest(
    @Mock private val beforeSubmitCallback: BeforeSubmitCallback,
    @Mock private val onErrorCallback: OnErrorCallback,
    @Mock private val onFinishedCallback: OnFinishedCallback,
    @Mock private val onSubmitCallback: OnSubmitCallback,
    @Mock private val onAdditionalDetailsCallback: OnAdditionalDetailsCallback,
) {

    @ParameterizedTest
    @MethodSource("callbacksSource")
    fun `given checkout callbacks, where a mandatory callback is not set, an error should be thrown `(
        checkoutCallbacks: CheckoutCallbacks,
        expectedExceptionMessage: String,
    ) {
        val exception = assertThrows<IllegalStateException> {
            checkoutCallbacks.toAdvancedComponentCallbacks()
        }
        assertEquals(expectedExceptionMessage, exception.message)
    }

    @Test
    fun `when beforeSubmit is set in checkout callbacks, beforeSubmit in advanced component triggers the set callback`() = runTest {
        val checkoutCallbacks = CheckoutCallbacks(
            beforeSubmit = beforeSubmitCallback,
            onSubmit = onSubmitCallback,
            onAdditionalDetails = onAdditionalDetailsCallback,
            onError = onErrorCallback,
        )

        val actualAdvancedComponentCallbacks = checkoutCallbacks.toAdvancedComponentCallbacks()

        actualAdvancedComponentCallbacks.beforeSubmit(TEST_PAYMENT_COMPONENT_STATE)
        verify(checkoutCallbacks.beforeSubmit)?.beforeSubmit(TEST_PAYMENT_COMPONENT_STATE)
    }

    @Test
    fun `when onSubmit is set in checkout callbacks, onSubmit in advanced component triggers the set callback`() = runTest {
        val checkoutCallbacks = CheckoutCallbacks(
            onSubmit = onSubmitCallback,
            onAdditionalDetails = onAdditionalDetailsCallback,
            onError = onErrorCallback,
        )

        val actualAdvancedComponentCallbacks = checkoutCallbacks.toAdvancedComponentCallbacks()

        actualAdvancedComponentCallbacks.onSubmit(TEST_PAYMENT_COMPONENT_STATE)
        verify(checkoutCallbacks.onSubmit)?.onSubmit(TEST_PAYMENT_COMPONENT_STATE)
    }

    @Test
    fun `when onAdditionalDetails is set in checkout callbacks, onAdditionalDetails in advanced component triggers the set callback`() = runTest {
        val checkoutCallbacks = CheckoutCallbacks(
            onSubmit = onSubmitCallback,
            onAdditionalDetails = onAdditionalDetailsCallback,
            onError = onErrorCallback,
        )

        val actualAdvancedComponentCallbacks = checkoutCallbacks.toAdvancedComponentCallbacks()

        actualAdvancedComponentCallbacks.onAdditionalDetails(TEST_ACTION_COMPONENT_DATA)
        verify(checkoutCallbacks.onAdditionalDetails)?.onAdditionalDetails(TEST_ACTION_COMPONENT_DATA)
    }

    @Test
    fun `when onError is set in checkout callbacks, onError in advanced component triggers it`() = runTest {
        val checkoutCallbacks = CheckoutCallbacks(
            onSubmit = onSubmitCallback,
            onAdditionalDetails = onAdditionalDetailsCallback,
            onError = onErrorCallback,
        )

        val actualAdvancedComponentCallbacks = checkoutCallbacks.toAdvancedComponentCallbacks()

        actualAdvancedComponentCallbacks.onError(TEST_COMPONENT_ERROR)
        verify(checkoutCallbacks.onError)?.onError(TEST_COMPONENT_ERROR)
    }

    @Test
    fun `when onFinished is set in checkout callbacks, onFinished in advanced component triggers it`() = runTest {
        val checkoutCallbacks = CheckoutCallbacks(
            onSubmit = onSubmitCallback,
            onAdditionalDetails = onAdditionalDetailsCallback,
            onError = onErrorCallback,
            onFinished = onFinishedCallback,
        )

        val actualAdvancedComponentCallbacks = checkoutCallbacks.toAdvancedComponentCallbacks()

        actualAdvancedComponentCallbacks.onFinished(TEST_PAYMENT_RESULT)
        verify(checkoutCallbacks.onFinished)?.onFinished(TEST_PAYMENT_RESULT)
    }

    companion object {
        private val TEST_PAYMENT_COMPONENT_STATE = TestPaymentComponentState()
        private val TEST_ACTION_COMPONENT_DATA = ActionComponentData()
        private val TEST_COMPONENT_ERROR = ComponentError(RuntimeException())
        private val TEST_PAYMENT_RESULT = PaymentResult(
            resultCode = "authorised",
            sessionId = null,
            sessionResult = null,
            sessionData = null,
            order = null,
        )

        @JvmStatic
        fun callbacksSource() = listOf(
            arguments(
                CheckoutCallbacks(
                    onAdditionalDetails = { CheckoutResult.Finished() },
                    beforeSubmit = { false },
                    onError = {},
                    onFinished = {},
                ),
                "onSubmit() callback is not set.",
            ),
            arguments(
                CheckoutCallbacks(
                    onSubmit = { CheckoutResult.Finished() },
                    beforeSubmit = { false },
                    onError = {},
                    onFinished = {},
                ),
                "onAdditionalDetails() callback is not set.",
            ),
            arguments(
                CheckoutCallbacks(
                    onSubmit = { CheckoutResult.Finished() },
                    onAdditionalDetails = { CheckoutResult.Finished() },
                    beforeSubmit = { false },
                    onFinished = {},
                ),
                "onError() callback is not set.",
            ),
        )
    }
}
